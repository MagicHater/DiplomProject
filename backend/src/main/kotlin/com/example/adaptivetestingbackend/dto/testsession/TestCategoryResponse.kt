package com.example.adaptivetestingbackend.dto.testsession

import java.util.UUID

data class TestCategoryResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String?,
)
