package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.entity.AnswerEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.sqrt

@Service
class ResultCalculationService(
    private val objectMapper: ObjectMapper,
) {
    private val scales = listOf(
        "attention",
        "stress_resistance",
        "responsibility",
        "adaptability",
        "decision_speed_accuracy",
    )

    fun calculate(answers: List<AnswerEntity>): CalculatedProfile {
        val rawScores = scales.associateWith { BigDecimal.ZERO }.toMutableMap()
        val minScores = scales.associateWith { BigDecimal.ZERO }.toMutableMap()
        val maxScores = scales.associateWith { BigDecimal.ZERO }.toMutableMap()
        val scaleContributions = scales.associateWith { mutableListOf<BigDecimal>() }.toMutableMap()

        answers.forEach { answer ->
            val answerValue = answer.answerValue ?: BigDecimal.ZERO
            val weightsByScale = parseWeights(answer.questionSnapshot.scaleWeightsJson)

            scales.forEach { scale ->
                val weight = weightsByScale[scale] ?: BigDecimal.ZERO
                rawScores[scale] = rawScores.getValue(scale).add(answerValue.multiply(weight))
                minScores[scale] = minScores.getValue(scale).add(MIN_ANSWER_VALUE.multiply(weight))
                maxScores[scale] = maxScores.getValue(scale).add(MAX_ANSWER_VALUE.multiply(weight))

                if (weight.abs() > BigDecimal.ZERO) {
                    scaleContributions.getValue(scale).add(answerValue.multiply(weight).setScale(4, RoundingMode.HALF_UP))
                }
            }
        }

        val reliability = analyzeReliability(answers)

        val normalizedScores = scales.associateWith { scale ->
            val baseNormalized = normalize(
                rawScore = rawScores.getValue(scale),
                minScore = minScores.getValue(scale),
                maxScore = maxScores.getValue(scale),
            )

            val normalized = if (scale == DECISION_SPEED_ACCURACY_SCALE) {
                combineDecisionQualityWithResponseTime(
                    decisionQualityScore = baseNormalized,
                    responseTimeScore = calculateDecisionTimeScore(answers),
                )
            } else {
                baseNormalized
            }

            applyReliabilityDamping(
                normalized = normalized,
                reliabilityFactor = reliability.factor,
            )
        }

        val explanations = buildScaleExplanations(normalizedScores, scaleContributions)

        return CalculatedProfile(
            attention = normalizedScores.getValue("attention"),
            stressResistance = normalizedScores.getValue("stress_resistance"),
            responsibility = normalizedScores.getValue("responsibility"),
            adaptability = normalizedScores.getValue("adaptability"),
            decisionSpeedAccuracy = normalizedScores.getValue(DECISION_SPEED_ACCURACY_SCALE),
            reliabilityFactor = reliability.factor,
            reliabilityFlags = reliability.flags,
            scaleExplanations = explanations,
        )
    }

    private fun parseWeights(scaleWeightsJson: String): Map<String, BigDecimal> {
        val node = objectMapper.readTree(scaleWeightsJson)
        return scales.associateWith { scale ->
            node.get(scale)?.decimalValue() ?: BigDecimal.ZERO
        }
    }

    private fun normalize(rawScore: BigDecimal, minScore: BigDecimal, maxScore: BigDecimal): BigDecimal {
        if (maxScore.compareTo(minScore) == 0) {
            return NEUTRAL_SCORE
        }

        val normalized = rawScore
            .subtract(minScore)
            .multiply(HUNDRED)
            .divide(maxScore.subtract(minScore), 8, RoundingMode.HALF_UP)

        return normalized
            .coerceIn(BigDecimal.ZERO, HUNDRED)
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun combineDecisionQualityWithResponseTime(
        decisionQualityScore: BigDecimal,
        responseTimeScore: BigDecimal,
    ): BigDecimal {
        return decisionQualityScore.multiply(DECISION_QUALITY_WEIGHT)
            .add(responseTimeScore.multiply(DECISION_TIME_WEIGHT))
            .coerceIn(BigDecimal.ZERO, HUNDRED)
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun calculateDecisionTimeScore(answers: List<AnswerEntity>): BigDecimal {
        val responseTimes = answers.mapNotNull { it.responseTimeMs }.filter { it > 0 }

        if (responseTimes.size < MIN_TIMED_ANSWERS_FOR_ANALYSIS) {
            return NEUTRAL_SCORE
        }

        val scores = responseTimes.map { responseTimeMs ->
            when {
                responseTimeMs < VERY_FAST_RESPONSE_MS -> BigDecimal("20")
                responseTimeMs < FAST_BUT_ACCEPTABLE_RESPONSE_MS -> BigDecimal("55")
                responseTimeMs <= OPTIMAL_RESPONSE_TIME_MAX_MS -> BigDecimal("100")
                responseTimeMs <= SLOW_RESPONSE_MS -> BigDecimal("75")
                else -> BigDecimal("45")
            }
        }

        return scores
            .reduce(BigDecimal::add)
            .divide(scores.size.toBigDecimal(), 2, RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.ZERO, HUNDRED)
    }

    private fun buildScaleExplanations(
        normalizedScores: Map<String, BigDecimal>,
        scaleContributions: Map<String, List<BigDecimal>>,
    ): Map<String, String> {
        return scales.associateWith { scale ->
            val contributions = scaleContributions[scale].orEmpty()
            if (contributions.isEmpty()) {
                "Недостаточно ответов, связанных с этой метрикой, чтобы объяснить результат."
            } else {
                val lowCount = contributions.count { it < BigDecimal.ZERO }
                val highCount = contributions.count { it > BigDecimal.ZERO }
                val neutralCount = contributions.size - lowCount - highCount
                val variability = standardDeviation(contributions)
                val variabilityText = when {
                    variability >= 1.40 -> "высокая вариативность ответов"
                    variability >= 0.75 -> "умеренная вариативность ответов"
                    else -> "ответы достаточно последовательны"
                }
                val score = normalizedScores.getValue(scale)
                val dominantReason = when {
                    score < BigDecimal("50") -> "низкий показатель связан с тем, что в $lowCount из ${contributions.size} релевантных вопросов выбраны ответы с отрицательным вкладом"
                    score > BigDecimal("75") -> "высокий показатель связан с тем, что в $highCount из ${contributions.size} релевантных вопросов выбраны ответы с положительным вкладом"
                    else -> "средний показатель сформирован смешанным профилем ответов: положительных — $highCount, отрицательных — $lowCount, нейтральных — $neutralCount"
                }

                val decisionTimeExplanation = if (scale == DECISION_SPEED_ACCURACY_SCALE) {
                    " Показатель дополнительно учитывает временную адекватность ответов: 70% оценки формируется качеством выбранных решений, 30% — временем реакции."
                } else {
                    ""
                }

                "$dominantReason; $variabilityText.$decisionTimeExplanation"
            }
        }
    }

    private fun standardDeviation(values: List<BigDecimal>): Double {
        if (values.size < 2) return 0.0
        val doubles = values.map { it.toDouble() }
        val mean = doubles.average()
        val variance = doubles.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    private fun analyzeReliability(answers: List<AnswerEntity>): ReliabilityAnalysis {
        if (answers.isEmpty()) {
            return ReliabilityAnalysis(BigDecimal("0.70"), listOf("Недостаточно ответов для оценки достоверности."))
        }

        var factor = if (answers.size < MIN_ANSWERS_FOR_FULL_CONFIDENCE) BigDecimal("0.70") else BigDecimal.ONE
        val flags = mutableListOf<String>()

        if (answers.size < MIN_ANSWERS_FOR_FULL_CONFIDENCE) {
            flags += "Мало ответов: результат следует интерпретировать осторожно."
        }

        val values = answers.map { it.answerValue ?: BigDecimal.ZERO }
        val distinctValues = values.map { it.setScale(2, RoundingMode.HALF_UP) }.toSet()
        val allSameExtreme = distinctValues.size == 1 && distinctValues.first().abs() >= MAX_ANSWER_VALUE

        if (allSameExtreme) {
            factor = factor.min(BigDecimal("0.70"))
            flags += "Обнаружен однотипный крайний паттерн ответов. Возможна социально желательная или механическая стратегия прохождения."
        }

        val positiveExtremeRatio = values.count { it >= MAX_ANSWER_VALUE }.toBigDecimal()
            .divide(values.size.toBigDecimal(), 8, RoundingMode.HALF_UP)
        val negativeExtremeRatio = values.count { it <= MIN_ANSWER_VALUE }.toBigDecimal()
            .divide(values.size.toBigDecimal(), 8, RoundingMode.HALF_UP)

        when {
            positiveExtremeRatio >= BigDecimal("0.80") -> {
                factor = factor.min(BigDecimal("0.78"))
                flags += "Очень высокая доля максимально положительных ответов. Возможен эффект социальной желательности."
            }
            negativeExtremeRatio >= BigDecimal("0.80") -> {
                factor = factor.min(BigDecimal("0.82"))
                flags += "Очень высокая доля максимально отрицательных ответов. Возможна незаинтересованность или намеренное искажение результата."
            }
            distinctValues.size <= 2 && answers.size >= MIN_ANSWERS_FOR_FULL_CONFIDENCE -> {
                factor = factor.min(BigDecimal("0.90"))
                flags += "Низкая вариативность ответов: кандидат использовал ограниченный набор вариантов."
            }
        }

        val responseTimes = answers.mapNotNull { it.responseTimeMs }.filter { it > 0 }
        if (responseTimes.size >= MIN_TIMED_ANSWERS_FOR_ANALYSIS) {
            val fastRatio = responseTimes.count { it < FAST_RESPONSE_MS }.toBigDecimal()
                .divide(responseTimes.size.toBigDecimal(), 8, RoundingMode.HALF_UP)
            val slowRatio = responseTimes.count { it > SLOW_RESPONSE_MS }.toBigDecimal()
                .divide(responseTimes.size.toBigDecimal(), 8, RoundingMode.HALF_UP)

            if (fastRatio >= BigDecimal("0.60")) {
                factor = factor.min(BigDecimal("0.82"))
                flags += "Слишком высокая доля быстрых ответов: возможны невнимательное прохождение или угадывание."
            }
            if (slowRatio >= BigDecimal("0.60")) {
                factor = factor.min(BigDecimal("0.88"))
                flags += "Большая доля чрезмерно долгих ответов: возможны сомнения, внешние паузы или нестабильность стратегии."
            }
        }

        return ReliabilityAnalysis(
            factor = factor.coerceIn(BigDecimal("0.60"), BigDecimal.ONE).setScale(2, RoundingMode.HALF_UP),
            flags = flags,
        )
    }

    private fun applyReliabilityDamping(normalized: BigDecimal, reliabilityFactor: BigDecimal): BigDecimal {
        if (reliabilityFactor == BigDecimal.ONE) {
            return normalized.setScale(2, RoundingMode.HALF_UP)
        }

        return NEUTRAL_SCORE
            .add(normalized.subtract(NEUTRAL_SCORE).multiply(reliabilityFactor))
            .coerceIn(BigDecimal.ZERO, HUNDRED)
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun BigDecimal.coerceIn(min: BigDecimal, max: BigDecimal): BigDecimal {
        if (this < min) return min
        if (this > max) return max
        return this
    }

    data class CalculatedProfile(
        val attention: BigDecimal,
        val stressResistance: BigDecimal,
        val responsibility: BigDecimal,
        val adaptability: BigDecimal,
        val decisionSpeedAccuracy: BigDecimal,
        val reliabilityFactor: BigDecimal = BigDecimal.ONE,
        val reliabilityFlags: List<String> = emptyList(),
        val scaleExplanations: Map<String, String> = emptyMap(),
    )

    private data class ReliabilityAnalysis(
        val factor: BigDecimal,
        val flags: List<String>,
    )

    private companion object {
        const val DECISION_SPEED_ACCURACY_SCALE: String = "decision_speed_accuracy"

        val MIN_ANSWER_VALUE: BigDecimal = BigDecimal("-2")
        val MAX_ANSWER_VALUE: BigDecimal = BigDecimal("2")
        val HUNDRED: BigDecimal = BigDecimal("100")
        val NEUTRAL_SCORE: BigDecimal = BigDecimal("50.00")
        val DECISION_QUALITY_WEIGHT: BigDecimal = BigDecimal("0.70")
        val DECISION_TIME_WEIGHT: BigDecimal = BigDecimal("0.30")

        const val MIN_ANSWERS_FOR_FULL_CONFIDENCE: Int = 3
        const val MIN_TIMED_ANSWERS_FOR_ANALYSIS: Int = 3
        const val VERY_FAST_RESPONSE_MS: Long = 1200
        const val FAST_RESPONSE_MS: Long = VERY_FAST_RESPONSE_MS
        const val FAST_BUT_ACCEPTABLE_RESPONSE_MS: Long = 4000
        const val OPTIMAL_RESPONSE_TIME_MAX_MS: Long = 20000
        const val SLOW_RESPONSE_MS: Long = 45000
    }
}
