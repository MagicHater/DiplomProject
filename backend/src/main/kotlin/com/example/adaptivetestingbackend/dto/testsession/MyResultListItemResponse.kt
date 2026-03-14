package com.example.adaptivetestingbackend.dto.testsession

import java.time.OffsetDateTime
import java.util.UUID

data class MyResultListItemResponse(
    val sessionId: UUID,
    val completedAt: OffsetDateTime,
    val summary: String,
    val scores: ScaleScoresDto,
)
