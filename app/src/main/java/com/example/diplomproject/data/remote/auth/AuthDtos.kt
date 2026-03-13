package com.example.diplomproject.data.remote.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("login") val login: String,
    @SerialName("password") val password: String,
)

@Serializable
data class RegisterRequestDto(
    @SerialName("name") val name: String,
    @SerialName("emailOrLogin") val emailOrLogin: String,
    @SerialName("password") val password: String,
    @SerialName("role") val role: String,
)

@Serializable
data class AuthResponseDto(
    @SerialName("token") val token: String,
    @SerialName("user") val user: MeResponseDto,
)

@Serializable
data class MeResponseDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("role") val role: String,
)
