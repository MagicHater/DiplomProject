package com.example.adaptivetestingbackend.dto.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

data class CreateControllerTestRequest(
    @field:NotBlank(message = "name must not be blank")
    val name: String,
    val description: String? = null,
    @field:Valid
    @field:Size(min = 1, message = "test must contain at least 1 question")
    val questions: List<CreateControllerQuestionRequest>,
)

data class CreateControllerQuestionRequest(
    @field:NotBlank(message = "question text must not be blank")
    val text: String,
    @field:NotNull
    val difficulty: Short? = 1,
    @field:NotNull
    val priority: Int? = 0,
    @field:Valid
    @field:Size(min = 2, message = "question must contain at least 2 options")
    val options: List<CreateControllerQuestionOptionRequest>,
)

data class CreateControllerQuestionOptionRequest(
    @field:NotBlank(message = "option text must not be blank")
    val text: String,
    @field:NotNull
    val order: Short?,
    @field:NotNull
    val contributionValue: BigDecimal?,
    @field:Valid
    @field:NotNull
    val scaleContributions: ControllerScaleValuesRequest?,
)

data class ControllerScaleValuesRequest(
    @field:NotNull @field:DecimalMin("0.00")
    val attention: BigDecimal?,
    @field:NotNull @field:DecimalMin("0.00")
    val stressResistance: BigDecimal?,
    @field:NotNull @field:DecimalMin("0.00")
    val responsibility: BigDecimal?,
    @field:NotNull @field:DecimalMin("0.00")
    val adaptability: BigDecimal?,
    @field:NotNull @field:DecimalMin("0.00")
    val decisionSpeedAccuracy: BigDecimal?,
)

data class CreateControllerTestResponse(
    val categoryId: UUID,
    val code: String,
    val name: String,
    val questionsCount: Int,
)
