package com.example.adaptivetestingbackend.dto.ai

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class GenerateCustomTestDraftRequest(
    @field:NotBlank(message = "prompt must not be blank")
    @field:Size(max = 2_000, message = "prompt is too long")
    val prompt: String,
    @field:Size(max = 255, message = "audience is too long")
    val audience: String? = null,
    @field:Min(value = 1, message = "desiredQuestionCount must be >= 1")
    val desiredQuestionCount: Int? = null,
    @field:Size(max = 64, message = "language is too long")
    val language: String? = null,
)

data class GenerateCustomTestDraftResponse(
    val title: String,
    val description: String,
    val questions: List<GeneratedCustomTestQuestionDto>,
    val providerMode: String,
    val requestId: String,
)

data class GeneratedCustomTestQuestionDto(
    val text: String,
    val options: List<String>,
)

data class InterpretCustomTestResultRequest(
    @field:NotBlank(message = "testTitle must not be blank")
    @field:Size(max = 255, message = "testTitle is too long")
    val testTitle: String,
    @field:Size(max = 3_000, message = "testDescription is too long")
    val testDescription: String? = null,
    @field:NotEmpty(message = "answers must not be empty")
    val answers: List<InterpretCustomTestAnswerDto>,
    @field:Size(max = 2_000, message = "analysisPrompt is too long")
    val analysisPrompt: String? = null,
)

data class InterpretCustomTestAnswerDto(
    @field:NotBlank(message = "question must not be blank")
    @field:Size(max = 1_000, message = "question is too long")
    val question: String,
    @field:NotBlank(message = "answer must not be blank")
    @field:Size(max = 1_000, message = "answer is too long")
    val answer: String,
)

data class InterpretCustomTestResultResponse(
    val summary: String,
    val observations: List<String>,
    val recommendations: List<String>,
    val disclaimer: String,
    val providerMode: String,
    val requestId: String,
)
