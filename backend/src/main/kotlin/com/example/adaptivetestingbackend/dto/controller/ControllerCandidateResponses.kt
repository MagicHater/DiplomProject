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

data class ControllerDashboardResponse(
    val totalCompletedSessions: Int,
    val totalParticipants: Int,
    val averages: ControllerDashboardAveragesResponse,
    val distribution: ControllerDashboardDistributionResponse,
    val weakMetrics: List<ControllerDashboardWeakMetricResponse>,
    val topCandidates: List<ControllerDashboardCandidateRankResponse>,
)

data class ControllerDashboardAveragesResponse(
    val attention: Double,
    val stressResistance: Double,
    val responsibility: Double,
    val adaptability: Double,
    val decisionSpeedAccuracy: Double,
    val overall: Double,
)

data class ControllerDashboardDistributionResponse(
    val low: Int,
    val medium: Int,
    val high: Int,
)

data class ControllerDashboardWeakMetricResponse(
    val metricCode: String,
    val title: String,
    val average: Double,
)

data class ControllerDashboardCandidateRankResponse(
    val participantId: String,
    val participantType: String,
    val displayName: String,
    val email: String? = null,
    val sessionsCount: Int,
    val averageScore: Double,
    val attention: Double,
    val stressResistance: Double,
    val responsibility: Double,
    val adaptability: Double,
    val decisionSpeedAccuracy: Double,
    val lastCompletedAt: OffsetDateTime?,
)
