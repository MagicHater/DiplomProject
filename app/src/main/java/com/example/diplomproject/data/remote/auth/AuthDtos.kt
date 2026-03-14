package com.example.diplomproject.data.remote.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
)

@Serializable
data class RegisterRequestDto(
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("role") val role: String,
)

@Serializable
data class LoginResponseDto(
    @SerialName("token") val token: String,
)

@Serializable
data class RegisterResponseDto(
    @SerialName("id") val id: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("role") val role: String,
)

@Serializable
data class MeResponseDto(
    @SerialName("id") val id: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("role") val role: String,
)
