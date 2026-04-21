package com.example.adaptivetestingbackend.dto.token

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime
import java.util.UUID

data class CreateControllerTokenRequest @JsonCreator constructor(
    @param:JsonProperty("categoryId")
    val categoryId: UUID,
)

data class CreateControllerTokenResponse(
    val token: String,
    val category: TestCategoryResponse,
    val createdAt: OffsetDateTime,
    @get:JsonProperty("isUsed")
    val isUsed: Boolean,
)

data class ControllerTokenListItemResponse(
    val token: String,
    val category: TestCategoryResponse,
    val createdAt: OffsetDateTime,
    @get:JsonProperty("isUsed")
    val isUsed: Boolean,
    val usedAt: OffsetDateTime?,
)

data class TokenPreviewRequest @JsonCreator constructor(
    @param:JsonProperty("token")
    @field:NotBlank
    val token: String,
)

data class TokenPreviewResponse(
    val valid: Boolean,
    val used: Boolean,
    val category: TestCategoryResponse?,
    val requiresAuth: Boolean,
)

data class StartGuestByTokenRequest @JsonCreator constructor(
    @param:JsonProperty("token")
    @field:NotBlank
    val token: String,
    @param:JsonProperty("guestName")
    @field:NotBlank
    val guestName: String,
)

data class StartCandidateByTokenRequest @JsonCreator constructor(
    @param:JsonProperty("token")
    @field:NotBlank
    val token: String,
)

data class TokenSessionStartResponse(
    val sessionId: UUID,
    val status: String,
    val createdAt: OffsetDateTime,
    val startedAt: OffsetDateTime?,
    val category: TestCategoryResponse,
    val guestSession: Boolean,
    val guestSessionKey: String? = null,
)