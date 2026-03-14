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
data class SubmitAnswerRequestDto(
    @SerialName("snapshotId") val snapshotId: String,
    @SerialName("selectedOptionId") val selectedOptionId: String,
)

@Serializable
data class SubmitAnswerResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("sessionStatus") val sessionStatus: String,
    @SerialName("canContinue") val canContinue: Boolean,
    @SerialName("progress") val progress: SessionProgressDto,
)

@Serializable
data class SessionProgressDto(
    @SerialName("answeredQuestions") val answeredQuestions: Int,
    @SerialName("issuedQuestions") val issuedQuestions: Int,
    @SerialName("totalAvailableQuestions") val totalAvailableQuestions: Int,
    @SerialName("completionPercent") val completionPercent: Int,
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
    @SerialName("attention") val attention: Double,
    @SerialName("stressResistance") val stressResistance: Double,
    @SerialName("responsibility") val responsibility: Double,
    @SerialName("adaptability") val adaptability: Double,
    @SerialName("decisionSpeedAccuracy") val decisionSpeedAccuracy: Double,
)

@Serializable
data class FinishSessionResponseDto(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("completedAt") val completedAt: String,
    @SerialName("scores") val scores: ScaleScoresDto,
    @SerialName("interpretations") val interpretations: ScaleInterpretationsDto,
    @SerialName("overallSummary") val overallSummary: String,
)

@Serializable
data class ScaleInterpretationsDto(
    @SerialName("attention") val attention: String,
    @SerialName("stressResistance") val stressResistance: String,
    @SerialName("responsibility") val responsibility: String,
    @SerialName("adaptability") val adaptability: String,
    @SerialName("decisionSpeedAccuracy") val decisionSpeedAccuracy: String,
)
