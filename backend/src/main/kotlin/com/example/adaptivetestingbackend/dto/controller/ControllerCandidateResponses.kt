package com.example.adaptivetestingbackend.dto.controller

import com.example.adaptivetestingbackend.dto.testsession.MyResultListItemResponse
import java.time.OffsetDateTime
import java.util.UUID

data class ControllerCandidateListItemResponse(
    val candidateId: UUID,
    val fullName: String,
    val email: String,
    val completedSessionsCount: Long,
    val lastCompletedAt: OffsetDateTime?,
)

data class ControllerCandidateResultsResponse(
    val candidateId: UUID,
    val fullName: String,
    val sessions: List<MyResultListItemResponse>,
)
