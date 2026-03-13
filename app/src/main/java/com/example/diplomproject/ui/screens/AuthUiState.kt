package com.example.diplomproject.ui.screens

import com.example.diplomproject.domain.model.UserRole

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
