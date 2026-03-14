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

        val normalizedScores = scales.associateWith { scale ->
            normalize(
                rawScore = rawScores.getValue(scale),
                minScore = minScores.getValue(scale),
                maxScore = maxScores.getValue(scale),
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
            return BigDecimal("50.00")
        }

        val normalized = rawScore
            .subtract(minScore)
            .multiply(HUNDRED)
            .divide(maxScore.subtract(minScore), 8, RoundingMode.HALF_UP)

        return normalized
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
    }
}
