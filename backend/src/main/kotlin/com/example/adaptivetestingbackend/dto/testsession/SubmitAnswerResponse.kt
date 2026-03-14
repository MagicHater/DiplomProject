package com.example.adaptivetestingbackend.dto.testsession

import java.math.BigDecimal
import java.util.UUID

data class SubmitAnswerResponse(
    val success: Boolean,
    val sessionId: UUID,
    val sessionStatus: String,
    val canContinue: Boolean,
    val progress: SessionProgressDto,
)

data class SessionProgressDto(
    val answeredQuestions: Int,
    val issuedQuestions: Int,
    val totalAvailableQuestions: Int,
    val completionPercent: Int,
    val cumulativeScore: BigDecimal,
    val scaleCoverage: Map<String, BigDecimal>,
)
