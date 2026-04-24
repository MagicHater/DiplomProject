package com.example.adaptivetestingbackend.dto.testsession

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class SubmitAnswerRequest(
    @field:NotNull
    val snapshotId: UUID?,

    @field:NotNull
    val selectedOptionId: UUID?,

    @field:PositiveOrZero
    val responseTimeMs: Long? = null,
)
