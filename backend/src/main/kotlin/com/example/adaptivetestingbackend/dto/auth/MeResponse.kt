package com.example.adaptivetestingbackend.dto.auth

import java.util.UUID

data class MeResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val displayName: String?,
    val role: String,
)
