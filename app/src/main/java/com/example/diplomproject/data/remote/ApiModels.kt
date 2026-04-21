package com.example.diplomproject.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    @SerialName("status") val status: String,
)

@Serializable
data class TestCategoryDto(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
)

@Serializable
data class CreateTestSessionRequestDto(
    @SerialName("categoryId") val categoryId: String,
)

@Serializable
data class CreateTestSessionResponseDto(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("status") val status: String,
    @SerialName("category") val category: TestCategoryDto,
    @SerialName("guestSession") val guestSession: Boolean = false,
    @SerialName("guestSessionKey") val guestSessionKey: String? = null,
)

@Serializable
data class TokenPreviewRequestDto(@SerialName("token") val token: String)

@Serializable
data class TokenPreviewResponseDto(
    @SerialName("valid") val valid: Boolean,
    @SerialName("used") val used: Boolean,
    @SerialName("category") val category: TestCategoryDto? = null,
    @SerialName("requiresAuth") val requiresAuth: Boolean,
)

@Serializable
data class StartGuestByTokenRequestDto(
    @SerialName("token") val token: String,
    @SerialName("guestName") val guestName: String,
)

@Serializable
data class StartCandidateByTokenRequestDto(@SerialName("token") val token: String)

@Serializable
data class ControllerTokenRequestDto(@SerialName("categoryId") val categoryId: String)

@Serializable
data class ControllerTokenResponseDto(
    @SerialName("token") val token: String,
    @SerialName("category") val category: TestCategoryDto,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("isUsed") val isUsed: Boolean,
)


@Serializable
data class CreateControllerTestRequestDto(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("questions") val questions: List<CreateControllerQuestionRequestDto>,
)

@Serializable
data class CreateControllerQuestionRequestDto(
    @SerialName("text") val text: String,
    @SerialName("difficulty") val difficulty: Int,
    @SerialName("priority") val priority: Int,
    @SerialName("options") val options: List<CreateControllerQuestionOptionRequestDto>,
)

@Serializable
data class CreateControllerQuestionOptionRequestDto(
    @SerialName("text") val text: String,
    @SerialName("order") val order: Int,
    @SerialName("contributionValue") val contributionValue: Double,
    @SerialName("scaleContributions") val scaleContributions: CreateControllerScaleValuesDto,
)

@Serializable
data class CreateControllerScaleValuesDto(
    @SerialName("attention") val attention: Double,
    @SerialName("stressResistance") val stressResistance: Double,
    @SerialName("responsibility") val responsibility: Double,
    @SerialName("adaptability") val adaptability: Double,
    @SerialName("decisionSpeedAccuracy") val decisionSpeedAccuracy: Double,
)


@Serializable
data class CreateControllerTestResponseDto(
    @SerialName("categoryId") val categoryId: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("questionsCount") val questionsCount: Int,
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


@Serializable
data class ControllerTokenResultListItemResponseDto(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("completedAt") val completedAt: String,
    @SerialName("category") val category: TestCategoryDto,
    @SerialName("participantType") val participantType: String,
    @SerialName("participantDisplayName") val participantDisplayName: String? = null,
    @SerialName("summary") val summary: String,
    @SerialName("scores") val scores: ScaleScoresDto,
)

@Serializable
data class ControllerParticipantListItemResponseDto(
    @SerialName("participantId") val participantId: String,
    @SerialName("participantType") val participantType: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("email") val email: String? = null,
    @SerialName("completedSessionsCount") val completedSessionsCount: Long,
    @SerialName("lastCompletedAt") val lastCompletedAt: String? = null,
)

@Serializable
data class ControllerParticipantResultsResponseDto(
    @SerialName("participantId") val participantId: String,
    @SerialName("participantType") val participantType: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("email") val email: String? = null,
    @SerialName("sessions") val sessions: List<MyResultListItemResponseDto>,
    @SerialName("statistics") val statistics: ControllerParticipantStatisticsResponseDto? = null,
)

@Serializable
data class ControllerParticipantStatisticsResponseDto(
    @SerialName("participantId") val participantId: String,
    @SerialName("sessions") val sessions: List<ControllerParticipantStatisticsSessionResponseDto>,
)

@Serializable
data class ControllerParticipantStatisticsSessionResponseDto(
    @SerialName("sessionOrder") val sessionOrder: Int,
    @SerialName("sessionId") val sessionId: String,
    @SerialName("completedAt") val completedAt: String,
    @SerialName("attention") val attention: Double,
    @SerialName("stressResistance") val stressResistance: Double,
    @SerialName("responsibility") val responsibility: Double,
    @SerialName("adaptability") val adaptability: Double,
    @SerialName("decisionSpeedAccuracy") val decisionSpeedAccuracy: Double,
)

@Serializable
data class CreateCustomTestRequestDto(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("allowedEmailsInput") val allowedEmailsInput: String,
    @SerialName("questions") val questions: List<CreateCustomTestQuestionRequestDto>,
)

@Serializable
data class CreateCustomTestQuestionRequestDto(
    @SerialName("text") val text: String,
    @SerialName("options") val options: List<CreateCustomTestOptionRequestDto>,
)

@Serializable
data class CreateCustomTestOptionRequestDto(
    @SerialName("text") val text: String,
)

@Serializable
data class CreateCustomTestResponseDto(
    @SerialName("testId") val testId: String,
)

@Serializable
data class CustomTestListItemDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("questionsCount") val questionsCount: Int,
    @SerialName("allowedEmailsCount") val allowedEmailsCount: Int,
    @SerialName("submissionsCount") val submissionsCount: Long,
    @SerialName("createdAt") val createdAt: String,
)

@Serializable
data class CustomTestDetailsDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("allowedEmails") val allowedEmails: List<String>,
    @SerialName("questions") val questions: List<CustomTestQuestionDetailsDto>,
)

@Serializable
data class CustomTestQuestionDetailsDto(
    @SerialName("id") val id: String,
    @SerialName("order") val order: Int,
    @SerialName("text") val text: String,
    @SerialName("options") val options: List<CustomTestOptionDetailsDto>,
)

@Serializable
data class CustomTestOptionDetailsDto(
    @SerialName("id") val id: String,
    @SerialName("order") val order: Int,
    @SerialName("text") val text: String,
)

@Serializable
data class CustomTestSubmissionRequestDto(
    @SerialName("answers") val answers: List<CustomTestAnswerRequestDto>,
)

@Serializable
data class CustomTestAnswerRequestDto(
    @SerialName("questionId") val questionId: String,
    @SerialName("optionId") val optionId: String,
)

@Serializable
data class CustomTestResultItemDto(
    @SerialName("submissionId") val submissionId: String,
    @SerialName("userId") val userId: String,
    @SerialName("userName") val userName: String,
    @SerialName("userEmail") val userEmail: String,
    @SerialName("submittedAt") val submittedAt: String,
    @SerialName("answers") val answers: List<CustomTestResultAnswerDto>,
)

@Serializable
data class CustomTestResultAnswerDto(
    @SerialName("questionId") val questionId: String,
    @SerialName("questionText") val questionText: String,
    @SerialName("selectedOptionId") val selectedOptionId: String,
    @SerialName("selectedOptionText") val selectedOptionText: String,
)

@Serializable
data class CustomTestStatisticsDto(
    @SerialName("testId") val testId: String,
    @SerialName("totalSubmissions") val totalSubmissions: Long,
    @SerialName("questions") val questions: List<CustomTestQuestionStatisticsDto>,
)

@Serializable
data class CustomTestQuestionStatisticsDto(
    @SerialName("questionId") val questionId: String,
    @SerialName("questionText") val questionText: String,
    @SerialName("options") val options: List<CustomTestOptionStatisticsDto>,
)

@Serializable
data class CustomTestOptionStatisticsDto(
    @SerialName("optionId") val optionId: String,
    @SerialName("optionText") val optionText: String,
    @SerialName("selectionsCount") val selectionsCount: Long,
    @SerialName("selectionsPercent") val selectionsPercent: Double,
)
