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
