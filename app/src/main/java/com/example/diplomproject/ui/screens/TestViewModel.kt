package com.example.diplomproject.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.AnswerProgress
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.domain.model.TestQuestion
import com.example.diplomproject.domain.usecase.FinishCandidateTestUseCase
import com.example.diplomproject.domain.usecase.GetNextCandidateQuestionUseCase
import com.example.diplomproject.domain.usecase.SubmitCandidateAnswerUseCase
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
    savedStateHandle: SavedStateHandle,
    private val submitCandidateAnswerUseCase: SubmitCandidateAnswerUseCase,
    private val getNextCandidateQuestionUseCase: GetNextCandidateQuestionUseCase,
    private val finishCandidateTestUseCase: FinishCandidateTestUseCase,
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"]) {
        "sessionId argument is required"
    }

    private val _uiState = MutableStateFlow(TestUiState(sessionId = sessionId))
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    init {
        loadQuestionIfNeeded()
    }


    fun setInitialQuestion(question: TestQuestion) {
        val hadQuestion = _uiState.value.question != null
        if (hadQuestion) return
        _uiState.update { it.copy(question = question, errorMessage = null, isInitialLoading = false) }
    }

    fun onOptionSelected(optionId: String) {
        _uiState.update { state ->
            if (state.isSubmitting || state.isInitialLoading || state.question == null) state
            else state.copy(selectedOptionId = optionId, errorMessage = null)
        }
    }

    fun onNextClick() {
        val state = _uiState.value
        val question = state.question ?: return
        val selectedOptionId = state.selectedOptionId ?: return
        if (state.isSubmitting || state.isInitialLoading || state.isFinishing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                val submitResult = submitCandidateAnswerUseCase(
                    sessionId = state.sessionId,
                    snapshotId = question.snapshotId,
                    selectedOptionId = selectedOptionId,
                )

                val answeredProgress = submitResult.progress

                if (!submitResult.canContinue) {
                    NextStep.Finished(finishCandidateTestUseCase(state.sessionId), answeredProgress)
                } else {
                    val nextPayload = getNextCandidateQuestionUseCase(state.sessionId)
                    if (!nextPayload.hasNextQuestion || nextPayload.question == null) {
                        NextStep.Finished(finishCandidateTestUseCase(state.sessionId), answeredProgress)
                    } else {
                        NextStep.NextQuestion(nextPayload.question, answeredProgress)
                    }
                }
            }.onSuccess { nextStep ->
                when (nextStep) {
                    is NextStep.Finished -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                finishResult = nextStep.result,
                                progress = nextStep.progress,
                                navigateToResult = true,
                            )
                        }
                    }

                    is NextStep.NextQuestion -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                question = nextStep.question,
                                selectedOptionId = null,
                                progress = nextStep.progress,
                            )
                        }
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.toUiMessage(),
                    )
                }
            }
        }
    }

    fun consumeResultNavigation() {
        _uiState.update { it.copy(navigateToResult = false) }
    }

    private fun loadQuestionIfNeeded() {
        if (_uiState.value.question != null || _uiState.value.isInitialLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isInitialLoading = true, errorMessage = null) }
            runCatching { getNextCandidateQuestionUseCase(sessionId) }
                .onSuccess { payload ->
                    if (payload.hasNextQuestion && payload.question != null) {
                        _uiState.update {
                            it.copy(
                                isInitialLoading = false,
                                question = payload.question,
                                selectedOptionId = null,
                            )
                        }
                    } else {
                        finishSessionAfterUnexpectedEnd()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            errorMessage = error.toUiMessage(),
                        )
                    }
                }
        }
    }

    fun retryLoad() {
        loadQuestionIfNeeded()
    }

    private suspend fun finishSessionAfterUnexpectedEnd() {
        _uiState.update { it.copy(isFinishing = true) }
        runCatching { finishCandidateTestUseCase(sessionId) }
            .onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isFinishing = false,
                        finishResult = result,
                        navigateToResult = true,
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isFinishing = false,
                        errorMessage = error.toUiMessage(),
                    )
                }
            }
    }
}

data class TestUiState(
    val sessionId: String = "",
    val question: TestQuestion? = null,
    val selectedOptionId: String? = null,
    val progress: AnswerProgress = AnswerProgress(0, 0, 0, 0),
    val finishResult: FinishedSessionResult? = null,
    val isInitialLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isFinishing: Boolean = false,
    val errorMessage: String? = null,
    val navigateToResult: Boolean = false,
) {
    val isBusy: Boolean get() = isInitialLoading || isSubmitting || isFinishing
}


private fun Throwable.toUiMessage(): String = when (this) {
    is IOException -> "Сеть недоступна. Проверьте подключение и попробуйте снова."
    else -> message ?: "Не удалось выполнить действие"
}

private sealed interface NextStep {
    data class NextQuestion(val question: TestQuestion, val progress: AnswerProgress) : NextStep
    data class Finished(val result: FinishedSessionResult, val progress: AnswerProgress) : NextStep
}
