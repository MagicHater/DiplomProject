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
    val statistics: ControllerParticipantStatisticsResponse? = null,
)

data class ControllerParticipantStatisticsResponse(
    val participantId: String,
    val sessions: List<ControllerParticipantStatisticsSessionResponse>,
)

data class ControllerParticipantStatisticsSessionResponse(
    val sessionOrder: Int,
    val sessionId: String,
    val completedAt: OffsetDateTime,
    val attention: Double,
    val stressResistance: Double,
    val responsibility: Double,
    val adaptability: Double,
    val decisionSpeedAccuracy: Double,
)
