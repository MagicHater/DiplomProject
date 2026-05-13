package com.example.diplomproject.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.ControllerSessionAnswer
import com.example.diplomproject.domain.repository.TestSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SessionAnswersViewModel @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sessionId: String = savedStateHandle["sessionId"] ?: ""

    private val _uiState = MutableStateFlow(SessionAnswersUiState())
    val uiState: StateFlow<SessionAnswersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (sessionId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Не указана сессия") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { testSessionRepository.getControllerSessionAnswers(sessionId) }
                .onSuccess { answers ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            answers = answers,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Не удалось загрузить историю ответов",
                        )
                    }
                }
        }
    }
}

data class SessionAnswersUiState(
    val isLoading: Boolean = false,
    val answers: List<ControllerSessionAnswer> = emptyList(),
    val errorMessage: String? = null,
)
