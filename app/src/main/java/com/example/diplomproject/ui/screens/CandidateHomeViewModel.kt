package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.StartedTestSession
import com.example.diplomproject.domain.usecase.StartCandidateTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

@HiltViewModel
class CandidateHomeViewModel @Inject constructor(
    private val startCandidateTestUseCase: StartCandidateTestUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CandidateHomeUiState())
    val uiState: StateFlow<CandidateHomeUiState> = _uiState.asStateFlow()

    fun startTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { startCandidateTestUseCase() }
                .onSuccess { startedSession ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            startedSession = startedSession,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUiMessage(),
                        )
                    }
                }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(startedSession = null) }
    }
}

data class CandidateHomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val startedSession: StartedTestSession? = null,
)

private fun Throwable.toUiMessage(): String = when (this) {
    is IOException -> "Проблема с сетью. Проверьте подключение и попробуйте снова."
    is IllegalArgumentException, is IllegalStateException -> message ?: "Сервер вернул некорректные данные"
    else -> message ?: "Не удалось начать тест"
}
