package com.example.diplomproject.domain.model

data class CustomTestDraft(
    val title: String,
    val description: String?,
    val allowedEmailsInput: String,
    val questions: List<CustomTestQuestionDraft>,
)

data class CustomTestQuestionDraft(
    val text: String,
    val options: List<CustomTestOptionDraft>,
)

data class CustomTestOptionDraft(
    val text: String,
)

data class CustomTestListItem(
    val id: String,
    val title: String,
    val description: String?,
    val questionsCount: Int,
    val allowedEmailsCount: Int,
    val submissionsCount: Long,
    val createdAt: String,
)

data class CustomTestDetails(
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val allowedEmails: List<String>,
    val questions: List<CustomTestQuestionDetails>,
)

data class CustomTestQuestionDetails(
    val id: String,
    val order: Int,
    val text: String,
    val options: List<CustomTestOptionDetails>,
)

data class CustomTestOptionDetails(
    val id: String,
    val order: Int,
    val text: String,
)

data class CustomTestSubmissionDraft(
    val answers: List<CustomTestAnswerDraft>,
)

data class CustomTestAnswerDraft(
    val questionId: String,
    val optionId: String,
)

data class CustomTestResultItem(
    val submissionId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val submittedAt: String,
    val answers: List<CustomTestResultAnswer>,
)

data class CustomTestResultAnswer(
    val questionId: String,
    val questionText: String,
    val selectedOptionId: String,
    val selectedOptionText: String,
)

data class CustomTestStatistics(
    val testId: String,
    val totalSubmissions: Long,
    val questions: List<CustomTestQuestionStatistics>,
)

data class CustomTestQuestionStatistics(
    val questionId: String,
    val questionText: String,
    val options: List<CustomTestOptionStatistics>,
)

data class CustomTestOptionStatistics(
    val optionId: String,
    val optionText: String,
    val selectionsCount: Long,
    val selectionsPercent: Double,
)
