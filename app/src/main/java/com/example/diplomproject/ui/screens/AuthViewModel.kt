package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.StartedTestSession
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.domain.repository.AuthRepository
import com.example.diplomproject.domain.repository.TestSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
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
    private val testSessionRepository: TestSessionRepository,
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

    fun onGuestTokenChanged(value: String) {
        _uiState.update { it.copy(guestTokenInput = value, guestTokenError = null, guestStartError = null) }
    }

    fun onGuestNameChanged(value: String) {
        _uiState.update { it.copy(guestNameInput = value, guestNameError = null, guestStartError = null) }
    }

    fun checkSavedSession() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    authError = null,
                    appSessionState = AppSessionState.Initializing,
                )
            }

            val token = authRepository.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        appSessionState = AppSessionState.Unauthenticated,
                    )
                }
                return@launch
            }

            runCatching { authRepository.me() }
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appSessionState = AppSessionState.Authenticated(user.role),
                        )
                    }
                }
                .onFailure {
                    authRepository.logout()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appSessionState = AppSessionState.Unauthenticated,
                            authError = "Сессия истекла. Войдите снова.",
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
                        appSessionState = AppSessionState.Authenticated(user.role),
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
                    appSessionState = AppSessionState.Unauthenticated,
                )
            }
        }
    }

    fun consumeRegistrationState() {
        _uiState.update { it.copy(isRegistered = false) }
    }

    fun consumeGuestNavigation() {
        _uiState.update { it.copy(guestStartedSession = null) }
    }

    fun startGuestByToken() {
        val state = _uiState.value
        val token = state.guestTokenInput.trim()
        val guestName = state.guestNameInput.trim()

        val tokenError = if (token.isBlank()) "Введите токен" else null
        val guestNameError = if (guestName.isBlank()) "Введите имя гостя" else null
        if (tokenError != null || guestNameError != null) {
            _uiState.update { it.copy(guestTokenError = tokenError, guestNameError = guestNameError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, guestStartError = null) }
            runCatching {
                val preview = testSessionRepository.previewToken(token)
                when {
                    !preview.valid -> error("Токен не найден")
                    preview.used -> error("Токен уже использован")
                    preview.requiresAuth -> error("Этот токен доступен только авторизованному кандидату")
                }

                val startedSession = testSessionRepository.startGuestByToken(token, guestName)
                val next = testSessionRepository.getNextQuestion(startedSession.sessionId)
                StartedTestSession(
                    sessionId = startedSession.sessionId,
                    category = preview.category ?: startedSession.category,
                    firstQuestion = next.question ?: error("Не удалось загрузить первый вопрос"),
                    guestSession = startedSession.guestSession,
                    guestSessionKey = startedSession.guestSessionKey,
                )
            }.onSuccess { startedSession ->
                _uiState.update { it.copy(isLoading = false, guestStartedSession = startedSession) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, guestStartError = error.toGuestUiMessage()) }
            }
        }
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

private fun Throwable.toGuestUiMessage(): String = when (this) {
    is IOException -> "Ошибка сети. Проверьте подключение и попробуйте снова."
    else -> message ?: "Не удалось начать тест"
}
