package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.usecase.FinishCandidateTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TestViewModel @Inject constructor(
    private val finishCandidateTestUseCase: FinishCandidateTestUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    fun finishTest(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { finishCandidateTestUseCase(sessionId) }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, finished = true) }
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

    fun consumeFinished() {
        _uiState.update { it.copy(finished = false) }
    }
}

data class TestUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val finished: Boolean = false,
)

private fun Throwable.toUiMessage(): String = when (this) {
    is IOException -> "Сеть недоступна. Не удалось завершить тест."
    else -> message ?: "Не удалось завершить тест"
}
