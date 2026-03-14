package com.example.adaptivetestingbackend.dto.testsession

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class ResultProfileResponse(
    val sessionId: UUID,
    val completedAt: OffsetDateTime,
    val scores: ScaleScoresDto,
    val interpretations: ScaleInterpretationsDto,
    val overallSummary: String,
)

data class ScaleScoresDto(
    val attention: BigDecimal,
    val stressResistance: BigDecimal,
    val responsibility: BigDecimal,
    val adaptability: BigDecimal,
    val decisionSpeedAccuracy: BigDecimal,
)

data class ScaleInterpretationsDto(
    val attention: String,
    val stressResistance: String,
    val responsibility: String,
    val adaptability: String,
    val decisionSpeedAccuracy: String,
)
