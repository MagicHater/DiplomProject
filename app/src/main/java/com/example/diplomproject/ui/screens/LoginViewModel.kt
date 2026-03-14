package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import com.example.diplomproject.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginChanged(value: String) {
        _uiState.update { it.copy(login = value, loginError = null) }
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

        val loginError = if (state.login.isBlank()) {
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
                loginError = loginError,
                passwordError = passwordError,
            )
        }

        return isValid
    }
}
