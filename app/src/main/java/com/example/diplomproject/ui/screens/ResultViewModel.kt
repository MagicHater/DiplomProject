package com.example.diplomproject.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.usecase.GetCandidateResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCandidateResultUseCase: GetCandidateResultUseCase,
) : ViewModel() {

    private val sessionId: String? = savedStateHandle["sessionId"]
    private val _uiState = MutableStateFlow(
        ResultUiState(
            sessionId = sessionId,
            isLoading = true,
        ),
    )
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val id = sessionId
        if (id.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = null,
                    errorMessage = null,
                    emptyMessage = "Не удалось определить результат для просмотра.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, emptyMessage = null) }
            runCatching { getCandidateResultUseCase(id) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = result,
                            errorMessage = null,
                            emptyMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUiMessage(),
                            result = null,
                            emptyMessage = null,
                        )
                    }
                }
        }
    }
}

data class ResultUiState(
    val sessionId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val result: FinishedSessionResult? = null,
)

private fun Throwable.toUiMessage(): String = when (this) {
    is IOException -> "Не удалось загрузить результат: проблема с сетью."
    else -> message ?: "Не удалось загрузить результат"
}
