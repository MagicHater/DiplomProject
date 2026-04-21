package com.example.adaptivetestingbackend.dto.custom

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.OffsetDateTime
import java.util.UUID

data class CreateCustomTestRequest(
    @field:NotBlank
    val title: String,
    val description: String? = null,
    @field:NotBlank
    val allowedEmailsInput: String,
    @field:NotEmpty
    @field:Valid
    val questions: List<CreateCustomTestQuestionRequest>,
)

data class CreateCustomTestQuestionRequest(
    @field:NotBlank
    val text: String,
    @field:NotEmpty
    @field:Valid
    val options: List<CreateCustomTestOptionRequest>,
)

data class CreateCustomTestOptionRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @param:JsonProperty("text")
    @field:NotBlank
    val text: String,
)

data class CreateCustomTestResponse(
    val testId: UUID,
)

data class CustomTestListItemResponse(
    val id: UUID,
    val title: String,
    val description: String? = null,
    val questionsCount: Int,
    val allowedEmailsCount: Int,
    val submissionsCount: Long,
    val createdAt: OffsetDateTime,
)

data class CustomTestDetailsResponse(
    val id: UUID,
    val title: String,
    val description: String? = null,
    val createdAt: OffsetDateTime,
    val questions: List<CustomTestQuestionDetailsResponse>,
    val allowedEmails: List<String>,
)

data class CustomTestQuestionDetailsResponse(
    val id: UUID,
    val order: Int,
    val text: String,
    val options: List<CustomTestOptionDetailsResponse>,
)

data class CustomTestOptionDetailsResponse(
    val id: UUID,
    val order: Int,
    val text: String,
)

data class CustomTestSubmissionRequest(
    @field:NotEmpty
    val answers: List<CustomTestAnswerRequest>,
)

data class CustomTestAnswerRequest(
    val questionId: UUID,
    val optionId: UUID,
)

data class CustomTestSubmissionResponse(
    val submissionId: UUID,
    val submittedAt: OffsetDateTime,
)

data class CustomTestResultItemResponse(
    val submissionId: UUID,
    val userId: UUID,
    val userName: String,
    val userEmail: String,
    val submittedAt: OffsetDateTime,
    val answers: List<CustomTestResultAnswerResponse>,
)

data class CustomTestResultAnswerResponse(
    val questionId: UUID,
    val questionText: String,
    val selectedOptionId: UUID,
    val selectedOptionText: String,
)

data class CustomTestStatisticsResponse(
    val testId: UUID,
    val totalSubmissions: Long,
    val questions: List<CustomTestQuestionStatisticsResponse>,
)

data class CustomTestQuestionStatisticsResponse(
    val questionId: UUID,
    val questionText: String,
    val options: List<CustomTestOptionStatisticsResponse>,
)

data class CustomTestOptionStatisticsResponse(
    val optionId: UUID,
    val optionText: String,
    val selectionsCount: Long,
    val selectionsPercent: Double,
)
