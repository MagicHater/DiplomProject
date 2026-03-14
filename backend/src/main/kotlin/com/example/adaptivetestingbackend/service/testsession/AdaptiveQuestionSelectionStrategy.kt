package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.entity.QuestionEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

/**
 * Простой explainable алгоритм выбора следующего вопроса:
 * 1) не повторяем уже заданные вопросы;
 * 2) учитываем дефицит coverage по 5 шкалам;
 * 3) добавляем штраф/бонус за неопределенность (variance в простом виде);
 * 4) ограничиваем количество вопросов лимитом сессии.
 */
@Service
class AdaptiveQuestionSelectionStrategy(
    private val objectMapper: ObjectMapper,
    @Value("\${adaptive-testing.session.max-questions:10}")
    val maxQuestionsPerSession: Int,
) {
    private val scales = listOf(
        "attention",
        "stress_resistance",
        "responsibility",
        "adaptability",
        "decision_speed_accuracy",
    )
    private val targetCoveragePerScale = BigDecimal("1.00")

    fun selectNextQuestion(
        allActiveQuestions: List<QuestionEntity>,
        askedQuestionIds: Set<java.util.UUID>,
        state: AdaptiveSessionState,
    ): QuestionEntity? {
        if (state.answeredQuestions >= maxQuestionsPerSession) {
            return null
        }

        val availableQuestions = allActiveQuestions.filter { it.id !in askedQuestionIds }
        if (availableQuestions.isEmpty()) {
            return null
        }

        val scalePriorities = calculateScalePriorities(state)

        return availableQuestions
            .sortedWith(
                compareByDescending<QuestionEntity> { question -> questionScore(question, scalePriorities) }
                    .thenBy { it.difficulty }
                    .thenByDescending { it.priority },
            )
            .firstOrNull()
    }

    private fun questionScore(question: QuestionEntity, scalePriorities: Map<String, BigDecimal>): BigDecimal {
        val weights = parseWeights(question.scaleWeightsJson)
        val adaptiveComponent = scales.fold(BigDecimal.ZERO) { acc, scale ->
            val weight = weights[scale] ?: BigDecimal.ZERO
            acc.add((scalePriorities[scale] ?: BigDecimal.ZERO).multiply(weight))
        }

        val priorityBonus = BigDecimal(question.priority).multiply(BigDecimal("0.01"))
        return adaptiveComponent.add(priorityBonus).setScale(6, RoundingMode.HALF_UP)
    }

    private fun calculateScalePriorities(state: AdaptiveSessionState): Map<String, BigDecimal> {
        return scales.associateWith { scale ->
            val coverage = state.scaleCoverage[scale] ?: BigDecimal.ZERO
            val gap = targetCoveragePerScale.subtract(coverage).max(BigDecimal.ZERO)
            val uncertainty = estimateUncertainty(state, scale)
            // gap — основной фактор, uncertainty — дополнительный уточняющий фактор.
            gap.add(uncertainty.multiply(BigDecimal("0.50"))).setScale(6, RoundingMode.HALF_UP)
        }
    }

    private fun estimateUncertainty(state: AdaptiveSessionState, scale: String): BigDecimal {
        if (state.answeredQuestions == 0) {
            return BigDecimal.ONE
        }

        val n = BigDecimal(state.answeredQuestions)
        val sum = state.scaleScoreSums[scale] ?: BigDecimal.ZERO
        val sumSquares = state.scaleScoreSquares[scale] ?: BigDecimal.ZERO

        val mean = sum.divide(n, 8, RoundingMode.HALF_UP)
        val meanSquares = sumSquares.divide(n, 8, RoundingMode.HALF_UP)
        val variance = max(
            meanSquares.subtract(mean.multiply(mean)).toDouble(),
            0.0,
        )

        // Чем меньше coverage и чем выше variance, тем выше неопределенность.
        val coverage = state.scaleCoverage[scale] ?: BigDecimal.ZERO
        val coverageFactor = BigDecimal.ONE.divide(BigDecimal.ONE.add(coverage), 8, RoundingMode.HALF_UP)
        return BigDecimal.valueOf(variance).multiply(coverageFactor).setScale(6, RoundingMode.HALF_UP)
    }

    private fun parseWeights(scaleWeightsJson: String): Map<String, BigDecimal> {
        return try {
            objectMapper.readValue(scaleWeightsJson, object : TypeReference<Map<String, BigDecimal>>() {})
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
