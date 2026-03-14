package com.example.adaptivetestingbackend.dto.testsession

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class SubmitAnswerRequest(
    @field:NotNull
    val snapshotId: UUID?,

    @field:NotNull
    val selectedOptionId: UUID?,
)
