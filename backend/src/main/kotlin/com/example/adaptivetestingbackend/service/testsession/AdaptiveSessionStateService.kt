package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.entity.QuestionSnapshotEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class AdaptiveSessionStateService(
    private val objectMapper: ObjectMapper,
) {
    fun readState(stateJson: String?): AdaptiveSessionState {
        if (stateJson.isNullOrBlank() || stateJson == "{}") {
            return AdaptiveSessionState()
        }

        return try {
            objectMapper.readValue(stateJson, AdaptiveSessionState::class.java)
        } catch (_: Exception) {
            AdaptiveSessionState()
        }
    }

    fun applyAnswer(
        currentState: AdaptiveSessionState,
        snapshot: QuestionSnapshotEntity,
        answerValue: BigDecimal,
    ): AdaptiveSessionState {
        val weightMap = objectMapper.readValue(snapshot.scaleWeightsJson, object : TypeReference<Map<String, BigDecimal>>() {})
        val updatedCoverage = currentState.scaleCoverage.toMutableMap()

        weightMap.forEach { (scale, weight) ->
            val normalizedWeight = weight.setScale(4, RoundingMode.HALF_UP)
            updatedCoverage[scale] = (updatedCoverage[scale] ?: BigDecimal.ZERO).add(normalizedWeight)
        }

        return currentState.copy(
            answeredQuestions = currentState.answeredQuestions + 1,
            cumulativeScore = currentState.cumulativeScore.add(answerValue).setScale(2, RoundingMode.HALF_UP),
            scaleCoverage = updatedCoverage,
        )
    }

    fun writeState(state: AdaptiveSessionState): String = objectMapper.writeValueAsString(state)
}

data class AdaptiveSessionState(
    val answeredQuestions: Int = 0,
    val cumulativeScore: BigDecimal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
    val scaleCoverage: Map<String, BigDecimal> = emptyMap(),
)
