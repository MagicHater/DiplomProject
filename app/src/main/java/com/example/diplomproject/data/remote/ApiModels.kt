package com.example.diplomproject.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    @SerialName("status") val status: String,
)

@Serializable
data class CreateTestSessionResponseDto(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("status") val status: String,
)

@Serializable
data class NextQuestionResponseDto(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("status") val status: String,
    @SerialName("hasNextQuestion") val hasNextQuestion: Boolean,
    @SerialName("question") val question: SessionQuestionDto? = null,
)

@Serializable
data class SessionQuestionDto(
    @SerialName("snapshotId") val snapshotId: String,
    @SerialName("order") val order: Int,
    @SerialName("text") val text: String,
    @SerialName("difficulty") val difficulty: Int,
    @SerialName("options") val options: List<SessionQuestionOptionDto>,
)

@Serializable
data class SessionQuestionOptionDto(
    @SerialName("optionId") val optionId: String,
    @SerialName("order") val order: Int,
    @SerialName("text") val text: String,
)

@Serializable
data class MyResultListItemResponseDto(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("completedAt") val completedAt: String,
    @SerialName("summary") val summary: String,
    @SerialName("scores") val scores: ScaleScoresDto,
)

@Serializable
data class ScaleScoresDto(
    @SerialName("attention") val attention: Int,
    @SerialName("stressResistance") val stressResistance: Int,
    @SerialName("responsibility") val responsibility: Int,
    @SerialName("adaptability") val adaptability: Int,
    @SerialName("decisionSpeedAccuracy") val decisionSpeedAccuracy: Int,
)

@Serializable
data class FinishSessionResponseDto(
    @SerialName("summary") val summary: String,
)
