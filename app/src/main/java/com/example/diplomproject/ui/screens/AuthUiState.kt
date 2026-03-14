package com.example.diplomproject.ui.screens

import com.example.diplomproject.domain.model.UserRole

data class AuthUiState(
    val login: String = "",
    val password: String = "",
    val name: String = "",
    val emailOrLogin: String = "",
    val selectedRole: UserRole = UserRole.Candidate,
    val loginError: String? = null,
    val nameError: String? = null,
    val emailOrLoginError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null,
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val appSessionState: AppSessionState = AppSessionState.Initializing,
)

sealed interface AppSessionState {
    data object Initializing : AppSessionState

    data object Unauthenticated : AppSessionState

    data class Authenticated(val role: UserRole) : AppSessionState
}

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.Candidate,
    val loginError: String? = null,
    val passwordError: String? = null,
)

data class RegisterUiState(
    val name: String = "",
    val emailOrLogin: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.Candidate,
    val nameError: String? = null,
    val emailOrLoginError: String? = null,
    val passwordError: String? = null,
)
