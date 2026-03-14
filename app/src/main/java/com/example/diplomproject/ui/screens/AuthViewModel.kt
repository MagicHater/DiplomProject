package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginChanged(value: String) {
        _uiState.update { it.copy(login = value, loginError = null, authError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, authError = null) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, authError = null) }
    }

    fun onEmailOrLoginChanged(value: String) {
        _uiState.update { it.copy(emailOrLogin = value, emailOrLoginError = null, authError = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun checkSavedSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }

            val token = authRepository.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSessionChecked = true,
                        authorizedRole = null,
                    )
                }
                return@launch
            }

            runCatching { authRepository.me() }
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSessionChecked = true,
                            authorizedRole = user.role,
                        )
                    }
                }
                .onFailure {
                    authRepository.logout()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSessionChecked = true,
                            authorizedRole = null,
                        )
                    }
                }
        }
    }

    fun login() {
        if (!validateLogin()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            runCatching {
                authRepository.login(login = state.login, password = state.password)
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authorizedRole = user.role,
                        isSessionChecked = true,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = error.message ?: "Ошибка входа",
                    )
                }
            }
        }
    }

    fun register() {
        if (!validateRegister()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            runCatching {
                authRepository.register(
                    name = state.name,
                    emailOrLogin = state.emailOrLogin,
                    password = state.password,
                    role = state.selectedRole,
                )
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isRegistered = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = error.message ?: "Ошибка регистрации",
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    authorizedRole = null,
                    isSessionChecked = true,
                )
            }
        }
    }

    fun consumeNavigationState() {
        _uiState.update { it.copy(authorizedRole = null, isRegistered = false) }
    }

    private fun validateLogin(): Boolean {
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

    private fun validateRegister(): Boolean {
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
