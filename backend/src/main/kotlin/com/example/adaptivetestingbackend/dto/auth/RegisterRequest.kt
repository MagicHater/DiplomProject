package com.example.adaptivetestingbackend.dto.auth

import com.example.adaptivetestingbackend.entity.RoleName
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val fullName: String,

    @field:NotBlank
    @field:Email
    @field:Size(max = 255)
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 255)
    val password: String,

    @field:NotNull
    val role: RoleName,
)
