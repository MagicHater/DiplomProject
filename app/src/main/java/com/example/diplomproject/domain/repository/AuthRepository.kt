package com.example.diplomproject.domain.repository

import com.example.diplomproject.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
)

interface AuthRepository {
    val tokenFlow: Flow<String?>

    suspend fun login(login: String, password: String): AuthUser
    suspend fun register(
        name: String,
        emailOrLogin: String,
        password: String,
        role: UserRole,
    ): AuthUser

    suspend fun me(): AuthUser
    suspend fun logout()
}
