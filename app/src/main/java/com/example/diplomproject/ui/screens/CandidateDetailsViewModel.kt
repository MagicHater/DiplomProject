package com.example.diplomproject.ui.screens

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.ControllerParticipantResults
import com.example.diplomproject.domain.repository.TestSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CandidateDetailsViewModel @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val participantType: String = savedStateHandle["participantType"] ?: "candidate"
    private val participantKey: String = Uri.decode(savedStateHandle["participantKey"] ?: "")

    private val _uiState = MutableStateFlow(CandidateDetailsUiState())
    val uiState: StateFlow<CandidateDetailsUiState> = _uiState.asStateFlow()

    fun load() {
        if (participantKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Не указан участник") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                testSessionRepository.getControllerParticipantResults(
                    participantType = participantType,
                    participantKey = participantKey,
                )
            }
                .onSuccess { payload ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            participant = payload,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            participant = null,
                            errorMessage = error.message ?: "Не удалось загрузить результаты",
                        )
                    }
                }
        }
    }
}

data class CandidateDetailsUiState(
    val isLoading: Boolean = false,
    val participant: ControllerParticipantResults? = null,
    val errorMessage: String? = null,
)
