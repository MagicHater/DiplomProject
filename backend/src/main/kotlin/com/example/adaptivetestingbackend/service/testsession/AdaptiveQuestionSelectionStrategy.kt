package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.entity.QuestionEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.max

/**
 * Explainable adaptive selection algorithm:
 * 1) do not repeat issued source questions;
 * 2) prioritize scales with low coverage, high uncertainty and weak current performance;
 * 3) adapt target difficulty using current average answer score;
 * 4) prefer questions close to target difficulty while still respecting scale priorities.
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
        val targetDifficulty = targetDifficulty(state, availableQuestions)

        return availableQuestions
            .sortedWith(
                compareByDescending<QuestionEntity> { question ->
                    questionScore(
                        question = question,
                        scalePriorities = scalePriorities,
                        targetDifficulty = targetDifficulty,
                    )
                }
                    .thenBy { abs(it.difficulty.toInt() - targetDifficulty) }
                    .thenByDescending { it.priority },
            )
            .firstOrNull()
    }

    private fun questionScore(
        question: QuestionEntity,
        scalePriorities: Map<String, BigDecimal>,
        targetDifficulty: Int,
    ): BigDecimal {
        val weights = parseWeights(question.scaleWeightsJson)
        val adaptiveComponent = scales.fold(BigDecimal.ZERO) { acc, scale ->
            val weight = weights[scale] ?: BigDecimal.ZERO
            acc.add((scalePriorities[scale] ?: BigDecimal.ZERO).multiply(weight))
        }

        val priorityBonus = BigDecimal(question.priority).multiply(BigDecimal("0.01"))
        val difficultyDistance = abs(question.difficulty.toInt() - targetDifficulty)
        val difficultyBonus = when (difficultyDistance) {
            0 -> BigDecimal("0.30")
            1 -> BigDecimal("0.12")
            else -> BigDecimal.ZERO
        }

        return adaptiveComponent
            .add(priorityBonus)
            .add(difficultyBonus)
            .setScale(6, RoundingMode.HALF_UP)
    }

    private fun targetDifficulty(state: AdaptiveSessionState, availableQuestions: List<QuestionEntity>): Int {
        val minDifficulty = availableQuestions.minOf { it.difficulty.toInt() }
        val maxDifficulty = availableQuestions.maxOf { it.difficulty.toInt() }

        if (state.answeredQuestions == 0) {
            return minDifficulty.coerceAtLeast(1)
        }

        val averageAnswer = state.cumulativeScore
            .divide(BigDecimal(state.answeredQuestions), 4, RoundingMode.HALF_UP)
            .toDouble()

        val target = when {
            averageAnswer >= 1.0 -> maxDifficulty
            averageAnswer <= -0.75 -> minDifficulty
            averageAnswer >= 0.35 -> (minDifficulty + 1).coerceAtMost(maxDifficulty)
            averageAnswer <= -0.25 -> minDifficulty
            else -> ((minDifficulty + maxDifficulty) / 2.0).toInt().coerceIn(minDifficulty, maxDifficulty)
        }

        return target.coerceIn(minDifficulty, maxDifficulty)
    }

    private fun calculateScalePriorities(state: AdaptiveSessionState): Map<String, BigDecimal> {
        return scales.associateWith { scale ->
            val coverage = state.scaleCoverage[scale] ?: BigDecimal.ZERO
            val gap = targetCoveragePerScale.subtract(coverage).max(BigDecimal.ZERO)
            val uncertainty = estimateUncertainty(state, scale)
            val weakScaleBonus = estimateWeakScaleBonus(state, scale)

            gap
                .add(uncertainty.multiply(BigDecimal("0.50")))
                .add(weakScaleBonus)
                .setScale(6, RoundingMode.HALF_UP)
        }
    }

    private fun estimateWeakScaleBonus(state: AdaptiveSessionState, scale: String): BigDecimal {
        if (state.answeredQuestions < MIN_ANSWERS_BEFORE_WEAK_SCALE_ADAPTATION) {
            return BigDecimal.ZERO
        }

        val coverage = state.scaleCoverage[scale] ?: BigDecimal.ZERO
        if (coverage <= BigDecimal.ZERO) {
            return BigDecimal.ZERO
        }

        val scoreSum = state.scaleScoreSums[scale] ?: BigDecimal.ZERO
        val averageByCoverage = scoreSum.divide(coverage, 8, RoundingMode.HALF_UP)

        return when {
            averageByCoverage <= BigDecimal("-1.00") -> BigDecimal("1.20")
            averageByCoverage <= BigDecimal("-0.50") -> BigDecimal("0.80")
            averageByCoverage <= BigDecimal("-0.20") -> BigDecimal("0.45")
            else -> BigDecimal.ZERO
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

    private companion object {
        const val MIN_ANSWERS_BEFORE_WEAK_SCALE_ADAPTATION = 3
    }
}
