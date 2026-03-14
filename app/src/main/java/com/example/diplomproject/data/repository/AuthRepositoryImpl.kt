package com.example.diplomproject.data.repository

import com.example.diplomproject.data.local.SessionManager
import com.example.diplomproject.data.remote.auth.AuthApi
import com.example.diplomproject.data.remote.auth.LoginRequestDto
import com.example.diplomproject.data.remote.auth.MeResponseDto
import com.example.diplomproject.data.remote.auth.RegisterRequestDto
import com.example.diplomproject.data.remote.auth.RegisterResponseDto
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.domain.repository.AuthRepository
import com.example.diplomproject.domain.repository.AuthUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
) : AuthRepository {

    override val tokenFlow: Flow<String?> = sessionManager.tokenFlow

    override suspend fun login(login: String, password: String): AuthUser {
        val response = authApi.login(LoginRequestDto(email = login, password = password))
        sessionManager.saveToken(response.token)
        return authApi.me().toDomain()
    }

    override suspend fun register(
        name: String,
        emailOrLogin: String,
        password: String,
        role: UserRole,
    ): AuthUser {
        val response = authApi.register(
            RegisterRequestDto(
                fullName = name,
                email = emailOrLogin,
                password = password,
                role = role.toApiRole(),
            ),
        )
        return response.toDomain()
    }

    override suspend fun me(): AuthUser = authApi.me().toDomain()

    override suspend fun logout() {
        sessionManager.clearSession()
    }
}

private fun RegisterResponseDto.toDomain(): AuthUser = AuthUser(
    id = id,
    name = fullName,
    email = email,
    role = role.toUserRole(),
)

private fun MeResponseDto.toDomain(): AuthUser = AuthUser(
    id = id,
    name = displayName ?: username,
    email = email,
    role = role.toUserRole(),
)

private fun String.toUserRole(): UserRole =
    if (equals("controller", ignoreCase = true)) UserRole.Controller else UserRole.Candidate

private fun UserRole.toApiRole(): String =
    if (this == UserRole.Controller) "controller" else "candidate"
