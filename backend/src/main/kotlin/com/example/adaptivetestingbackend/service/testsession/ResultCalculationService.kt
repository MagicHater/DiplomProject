package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.entity.AnswerEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

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

        answers.forEach { answer ->
            val answerValue = answer.answerValue ?: BigDecimal.ZERO
            val weightsByScale = parseWeights(answer.questionSnapshot.scaleWeightsJson)

            scales.forEach { scale ->
                val weight = weightsByScale[scale] ?: BigDecimal.ZERO
                rawScores[scale] = rawScores.getValue(scale).add(answerValue.multiply(weight))
                minScores[scale] = minScores.getValue(scale).add(MIN_ANSWER_VALUE.multiply(weight))
                maxScores[scale] = maxScores.getValue(scale).add(MAX_ANSWER_VALUE.multiply(weight))
            }
        }

        val reliabilityFactor = responsePatternReliabilityFactor(answers)

        val normalizedScores = scales.associateWith { scale ->
            applyReliabilityDamping(
                normalized = normalize(
                    rawScore = rawScores.getValue(scale),
                    minScore = minScores.getValue(scale),
                    maxScore = maxScores.getValue(scale),
                ),
                reliabilityFactor = reliabilityFactor,
            )
        }

        return CalculatedProfile(
            attention = normalizedScores.getValue("attention"),
            stressResistance = normalizedScores.getValue("stress_resistance"),
            responsibility = normalizedScores.getValue("responsibility"),
            adaptability = normalizedScores.getValue("adaptability"),
            decisionSpeedAccuracy = normalizedScores.getValue("decision_speed_accuracy"),
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

    private fun responsePatternReliabilityFactor(answers: List<AnswerEntity>): BigDecimal {
        if (answers.size < MIN_ANSWERS_FOR_FULL_CONFIDENCE) {
            return BigDecimal("0.70")
        }

        val values = answers.map { it.answerValue ?: BigDecimal.ZERO }
        val distinctValues = values.map { it.setScale(2, RoundingMode.HALF_UP) }.toSet()
        val allSameExtreme = distinctValues.size == 1 && distinctValues.first().abs() >= MAX_ANSWER_VALUE

        if (allSameExtreme) {
            return BigDecimal("0.75")
        }

        val positiveExtremeRatio = values.count { it >= MAX_ANSWER_VALUE }.toBigDecimal()
            .divide(values.size.toBigDecimal(), 8, RoundingMode.HALF_UP)
        val negativeExtremeRatio = values.count { it <= MIN_ANSWER_VALUE }.toBigDecimal()
            .divide(values.size.toBigDecimal(), 8, RoundingMode.HALF_UP)

        return when {
            positiveExtremeRatio >= BigDecimal("0.80") || negativeExtremeRatio >= BigDecimal("0.80") -> BigDecimal("0.85")
            distinctValues.size <= 2 && answers.size >= MIN_ANSWERS_FOR_FULL_CONFIDENCE -> BigDecimal("0.90")
            else -> BigDecimal.ONE
        }
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
    )

    private companion object {
        val MIN_ANSWER_VALUE: BigDecimal = BigDecimal("-2")
        val MAX_ANSWER_VALUE: BigDecimal = BigDecimal("2")
        val HUNDRED: BigDecimal = BigDecimal("100")
        val NEUTRAL_SCORE: BigDecimal = BigDecimal("50.00")
        const val MIN_ANSWERS_FOR_FULL_CONFIDENCE: Int = 3
    }
}
