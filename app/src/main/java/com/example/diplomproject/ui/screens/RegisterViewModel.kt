package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import com.example.diplomproject.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onEmailOrLoginChanged(value: String) {
        _uiState.update { it.copy(emailOrLogin = value, emailOrLoginError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true

        val nameError = if (state.name.isBlank()) {
            isValid = false
            "Введите имя"
        } else {
            null
        }

        val emailOrLoginError = if (state.emailOrLogin.isBlank()) {
            isValid = false
            "Введите email"
        } else {
            null
        }

        val passwordError = if (state.password.length < 8) {
            isValid = false
            "Пароль должен быть не короче 8 символов"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                nameError = nameError,
                emailOrLoginError = emailOrLoginError,
                passwordError = passwordError,
            )
        }

        return isValid
    }
}
