package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.StartedTestSession
import com.example.diplomproject.domain.model.TestCategory
import com.example.diplomproject.domain.repository.TestSessionRepository
import com.example.diplomproject.domain.usecase.StartCandidateTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CandidateHomeViewModel @Inject constructor(
    private val startCandidateTestUseCase: StartCandidateTestUseCase,
    private val testSessionRepository: TestSessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CandidateHomeUiState())
    val uiState: StateFlow<CandidateHomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            runCatching { testSessionRepository.getCategories() }
                .onSuccess { categories ->
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id,
                        )
                    }
                }
        }
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun startTest() {
        val categoryId = _uiState.value.selectedCategoryId ?: run {
            _uiState.update { it.copy(errorMessage = "Выберите категорию") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { startCandidateTestUseCase(categoryId) }
                .onSuccess { startedSession ->
                    _uiState.update { it.copy(isLoading = false, startedSession = startedSession) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUiMessage()) }
                }
        }
    }

    fun startByToken() {
        val token = _uiState.value.tokenInput.trim()
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите токен") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val sessionId = testSessionRepository.startCandidateByToken(token)
                val preview = testSessionRepository.previewToken(token)
                val next = testSessionRepository.getNextQuestion(sessionId)
                StartedTestSession(
                    sessionId = sessionId,
                    category = preview.category ?: TestCategory("", "UNKNOWN", "Неизвестно"),
                    firstQuestion = next.question ?: error("Нет первого вопроса"),
                )
            }.onSuccess { started ->
                _uiState.update { it.copy(isLoading = false, startedSession = started) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.toUiMessage()) }
            }
        }
    }

    fun onTokenChanged(value: String) {
        _uiState.update { it.copy(tokenInput = value) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(startedSession = null) }
    }
}

data class CandidateHomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val categories: List<TestCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val tokenInput: String = "",
    val startedSession: StartedTestSession? = null,
)

private fun Throwable.toUiMessage(): String = when (this) {
    is IOException -> "Проблема с сетью. Проверьте подключение и попробуйте снова."
    is IllegalArgumentException, is IllegalStateException -> message ?: "Сервер вернул некорректные данные"
    else -> message ?: "Не удалось начать тест"
}
