package com.example.adaptivetestingbackend.dto.controller

import com.example.adaptivetestingbackend.dto.testsession.MyResultListItemResponse
import java.time.OffsetDateTime

data class ControllerParticipantListItemResponse(
    val participantId: String,
    val participantType: String,
    val displayName: String,
    val email: String? = null,
    val completedSessionsCount: Long,
    val lastCompletedAt: OffsetDateTime?,
)

data class ControllerParticipantResultsResponse(
    val participantId: String,
    val participantType: String,
    val displayName: String,
    val email: String? = null,
    val sessions: List<MyResultListItemResponse>,
)
