package com.example.adaptivetestingbackend.dto.auth

import java.util.UUID

data class MeResponse(
    val id: UUID,
    val fullName: String,
    val email: String,
    val role: String,
)
