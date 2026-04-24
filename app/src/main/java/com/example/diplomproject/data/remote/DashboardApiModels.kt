package com.example.diplomproject.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ControllerDashboardResponseDto(
    @SerialName("totalCompletedSessions") val totalCompletedSessions: Int,
    @SerialName("totalParticipants") val totalParticipants: Int,
    @SerialName("averages") val averages: ControllerDashboardAveragesResponseDto,
    @SerialName("distribution") val distribution: ControllerDashboardDistributionResponseDto,
    @SerialName("weakMetrics") val weakMetrics: List<ControllerDashboardWeakMetricResponseDto>,
    @SerialName("topCandidates") val topCandidates: List<ControllerDashboardCandidateRankResponseDto>,
)

@Serializable
data class ControllerDashboardAveragesResponseDto(
    @SerialName("attention") val attention: Double,
    @SerialName("stressResistance") val stressResistance: Double,
    @SerialName("responsibility") val responsibility: Double,
    @SerialName("adaptability") val adaptability: Double,
    @SerialName("decisionSpeedAccuracy") val decisionSpeedAccuracy: Double,
    @SerialName("overall") val overall: Double,
)

@Serializable
data class ControllerDashboardDistributionResponseDto(
    @SerialName("low") val low: Int,
    @SerialName("medium") val medium: Int,
    @SerialName("high") val high: Int,
)

@Serializable
data class ControllerDashboardWeakMetricResponseDto(
    @SerialName("metricCode") val metricCode: String,
    @SerialName("title") val title: String,
    @SerialName("average") val average: Double,
)

@Serializable
data class ControllerDashboardCandidateRankResponseDto(
    @SerialName("participantId") val participantId: String,
    @SerialName("participantType") val participantType: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("email") val email: String? = null,
    @SerialName("sessionsCount") val sessionsCount: Int,
    @SerialName("averageScore") val averageScore: Double,
    @SerialName("lastCompletedAt") val lastCompletedAt: String? = null,
)
