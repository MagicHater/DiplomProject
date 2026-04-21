package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.ControllerQuestionDraft
import com.example.diplomproject.domain.model.ControllerQuestionOptionDraft
import com.example.diplomproject.domain.model.ControllerScaleValues
import com.example.diplomproject.domain.model.ControllerTestDraft
import com.example.diplomproject.domain.repository.TestSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ControllerCreateTestViewModel @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ControllerCreateTestUiState())
    val uiState: StateFlow<ControllerCreateTestUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value, errorMessage = null) }

    fun addQuestion() = _uiState.update {
        it.copy(questions = it.questions + EditableQuestion.default(it.questions.size + 1), errorMessage = null)
    }

    fun removeQuestion(index: Int) = _uiState.update {
        if (it.questions.size <= 1) return@update it
        it.copy(questions = it.questions.filterIndexed { i, _ -> i != index }, errorMessage = null)
    }

    fun onQuestionTextChanged(index: Int, value: String) = updateQuestion(index) { it.copy(text = value) }

    fun addOption(questionIndex: Int) = updateQuestion(questionIndex) { question ->
        question.copy(options = question.options + EditableOption.default(question.options.size + 1))
    }

    fun removeOption(questionIndex: Int, optionIndex: Int) = updateQuestion(questionIndex) { question ->
        if (question.options.size <= 2) question else question.copy(options = question.options.filterIndexed { i, _ -> i != optionIndex })
    }

    fun onOptionTextChanged(questionIndex: Int, optionIndex: Int, value: String) =
        updateOption(questionIndex, optionIndex) { it.copy(text = value) }

    fun onOptionOrderChanged(questionIndex: Int, optionIndex: Int, value: String) =
        updateOption(questionIndex, optionIndex) { it.copy(order = value) }

    fun onOptionContributionChanged(questionIndex: Int, optionIndex: Int, value: String) =
        updateOption(questionIndex, optionIndex) { it.copy(contributionValue = value) }

    fun onOptionScaleChanged(questionIndex: Int, optionIndex: Int, metric: ControllerMetricInput, value: String) =
        updateOption(questionIndex, optionIndex) { option ->
            when (metric) {
                ControllerMetricInput.Attention -> option.copy(attention = value)
                ControllerMetricInput.StressResistance -> option.copy(stressResistance = value)
                ControllerMetricInput.Responsibility -> option.copy(responsibility = value)
                ControllerMetricInput.Adaptability -> option.copy(adaptability = value)
                ControllerMetricInput.DecisionSpeedAccuracy -> option.copy(decisionSpeedAccuracy = value)
            }
        }

    fun saveTest(onSuccess: () -> Unit) {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val draft = state.toDraft() ?: run {
            _uiState.update { it.copy(errorMessage = "Проверьте числовые значения в вариантах ответов") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            runCatching { testSessionRepository.createControllerTest(draft) }
                .onSuccess { category ->
                    _uiState.value = ControllerCreateTestUiState(
                        successMessage = "Тест «${category.name}» успешно создан",
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Не удалось сохранить тест",
                        )
                    }
                }
        }
    }

    private fun updateQuestion(index: Int, transform: (EditableQuestion) -> EditableQuestion) = _uiState.update { state ->
        state.copy(
            questions = state.questions.mapIndexed { i, question -> if (i == index) transform(question) else question },
            errorMessage = null,
        )
    }

    private fun updateOption(questionIndex: Int, optionIndex: Int, transform: (EditableOption) -> EditableOption) =
        updateQuestion(questionIndex) { question ->
            question.copy(options = question.options.mapIndexed { i, option -> if (i == optionIndex) transform(option) else option })
        }

    private fun validate(state: ControllerCreateTestUiState): String? {
        if (state.name.isBlank()) return "Введите название теста"
        if (state.questions.isEmpty()) return "Добавьте хотя бы один вопрос"
        state.questions.forEachIndexed { questionIndex, question ->
            if (question.text.isBlank()) return "Заполните текст вопроса №${questionIndex + 1}"
            if (question.options.size < 2) return "В вопросе №${questionIndex + 1} должно быть минимум 2 варианта"
            question.options.forEachIndexed { optionIndex, option ->
                if (option.text.isBlank()) return "Заполните текст варианта №${optionIndex + 1} в вопросе №${questionIndex + 1}"
            }
        }
        return null
    }
}

enum class ControllerMetricInput(val title: String) {
    Attention("Attention"),
    StressResistance("StressResistance"),
    Responsibility("Responsibility"),
    Adaptability("Adaptability"),
    DecisionSpeedAccuracy("DecisionSpeedAccuracy"),
}

data class ControllerCreateTestUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val description: String = "",
    val questions: List<EditableQuestion> = listOf(EditableQuestion.default(1)),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

data class EditableQuestion(
    val text: String,
    val options: List<EditableOption>,
) {
    companion object {
        fun default(order: Int): EditableQuestion = EditableQuestion(
            text = "",
            options = listOf(EditableOption.default(1), EditableOption.default(2)),
        )
    }
}

data class EditableOption(
    val text: String,
    val order: String,
    val contributionValue: String,
    val attention: String,
    val stressResistance: String,
    val responsibility: String,
    val adaptability: String,
    val decisionSpeedAccuracy: String,
) {
    companion object {
        fun default(order: Int): EditableOption = EditableOption(
            text = "",
            order = order.toString(),
            contributionValue = "0",
            attention = "0",
            stressResistance = "0",
            responsibility = "0",
            adaptability = "0",
            decisionSpeedAccuracy = "0",
        )
    }
}

private fun ControllerCreateTestUiState.toDraft(): ControllerTestDraft? {
    val questions = questions.map { question ->
        ControllerQuestionDraft(
            text = question.text,
            options = question.options.map { option ->
                val order = option.order.toIntOrNull() ?: return null
                val contributionValue = option.contributionValue.toDoubleOrNull() ?: return null
                val attention = option.attention.toDoubleOrNull() ?: return null
                val stressResistance = option.stressResistance.toDoubleOrNull() ?: return null
                val responsibility = option.responsibility.toDoubleOrNull() ?: return null
                val adaptability = option.adaptability.toDoubleOrNull() ?: return null
                val decisionSpeedAccuracy = option.decisionSpeedAccuracy.toDoubleOrNull() ?: return null

                ControllerQuestionOptionDraft(
                    text = option.text,
                    order = order,
                    contributionValue = contributionValue,
                    scaleContributions = ControllerScaleValues(
                        attention = attention,
                        stressResistance = stressResistance,
                        responsibility = responsibility,
                        adaptability = adaptability,
                        decisionSpeedAccuracy = decisionSpeedAccuracy,
                    ),
                )
            },
        )
    }

    return ControllerTestDraft(
        name = name,
        description = description.takeIf { it.isNotBlank() },
        questions = questions,
    )
}
