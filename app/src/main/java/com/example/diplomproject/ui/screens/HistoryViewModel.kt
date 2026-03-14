package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.CandidateResultHistoryItem
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.domain.usecase.GetCandidateHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getCandidateHistoryUseCase: GetCandidateHistoryUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun load(role: UserRole?) {
        if (role == UserRole.Controller) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    items = emptyList(),
                    infoMessage = "Для контроллера история кандидата пока не реализована.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching { getCandidateHistoryUseCase() }
                .onSuccess { history ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = history,
                            errorMessage = null,
                            infoMessage = if (history.isEmpty()) "История пока пуста." else null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUiMessage(),
                            items = emptyList(),
                        )
                    }
                }
        }
    }
}

data class HistoryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val items: List<CandidateResultHistoryItem> = emptyList(),
)

private fun Throwable.toUiMessage(): String = when (this) {
    is IOException -> "Не удалось загрузить историю: проблема с сетью."
    else -> message ?: "Не удалось загрузить историю результатов"
}
