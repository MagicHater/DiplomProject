package com.example.adaptivetestingbackend.dto.testsession

import java.time.OffsetDateTime
import java.util.UUID

data class CreateTestSessionResponse(
    val sessionId: UUID,
    val status: String,
    val createdAt: OffsetDateTime,
    val startedAt: OffsetDateTime?,
)
