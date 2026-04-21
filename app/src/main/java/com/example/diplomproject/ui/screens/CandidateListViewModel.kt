package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.ControllerParticipantListItem
import com.example.diplomproject.domain.repository.TestSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CandidateListViewModel @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CandidateListUiState())
    val uiState: StateFlow<CandidateListUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { testSessionRepository.getControllerParticipants() }
                .onSuccess { participants ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            participants = participants,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            participants = emptyList(),
                            errorMessage = error.message ?: "Не удалось загрузить кандидатов",
                        )
                    }
                }
        }
    }
}

data class CandidateListUiState(
    val isLoading: Boolean = false,
    val participants: List<ControllerParticipantListItem> = emptyList(),
    val errorMessage: String? = null,
)
