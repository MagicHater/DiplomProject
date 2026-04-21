package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.CustomTestAnswerDraft
import com.example.diplomproject.domain.model.CustomTestDraft
import com.example.diplomproject.domain.model.CustomTestListItem
import com.example.diplomproject.domain.model.CustomTestQuestionDraft
import com.example.diplomproject.domain.model.CustomTestSubmissionDraft
import com.example.diplomproject.domain.repository.TestSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@HiltViewModel
class CustomTestsViewModel @Inject constructor(
    private val repository: TestSessionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CustomTestsUiState())
    val state: StateFlow<CustomTestsUiState> = _state.asStateFlow()

    fun onTitleChanged(value: String) = _state.update { it.copy(title = value) }
    fun onDescriptionChanged(value: String) = _state.update { it.copy(description = value) }
    fun onEmailsChanged(value: String) = _state.update { it.copy(allowedEmailsInput = value) }

    fun addQuestion() = _state.update { it.copy(questions = it.questions + EditableCustomQuestion()) }
    fun removeQuestion(index: Int) = _state.update { it.copy(questions = it.questions.filterIndexed { i, _ -> i != index }) }
    fun onQuestionChanged(index: Int, value: String) = _state.update {
        it.copy(questions = it.questions.mapIndexed { i, q -> if (i == index) q.copy(text = value) else q })
    }

    fun addOption(questionIndex: Int) = _state.update {
        it.copy(
            questions = it.questions.mapIndexed { i, q ->
                if (i == questionIndex) q.copy(options = q.options + "") else q
            },
        )
    }

    fun removeOption(questionIndex: Int, optionIndex: Int) = _state.update {
        it.copy(
            questions = it.questions.mapIndexed { i, q ->
                if (i != questionIndex) q else q.copy(options = q.options.filterIndexed { idx, _ -> idx != optionIndex })
            },
        )
    }

    fun onOptionChanged(questionIndex: Int, optionIndex: Int, value: String) = _state.update {
        it.copy(
            questions = it.questions.mapIndexed { i, q ->
                if (i != questionIndex) q
                else q.copy(options = q.options.mapIndexed { idx, option -> if (idx == optionIndex) value else option })
            },
        )
    }

    fun createCustomTest(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(error = "Введите название") }
            return
        }
        if (current.questions.isEmpty() || current.questions.any { it.text.isBlank() || it.options.size < 2 || it.options.any(String::isBlank) }) {
            _state.update { it.copy(error = "Проверьте вопросы и варианты") }
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.createCustomTest(
                    CustomTestDraft(
                        title = current.title,
                        description = current.description.ifBlank { null },
                        allowedEmailsInput = current.allowedEmailsInput,
                        questions = current.questions.map { q ->
                            CustomTestQuestionDraft(
                                text = q.text,
                                options = q.options.map { com.example.diplomproject.domain.model.CustomTestOptionDraft(it) },
                            )
                        },
                    ),
                )
            }.onSuccess {
                _state.value = CustomTestsUiState(success = "Тест создан")
                onSuccess()
            }.onFailure { e -> _state.update { it.copy(error = e.toUiMessage()) } }
        }
    }

    fun loadMyTests() {
        viewModelScope.launch {
            runCatching { repository.getMyCustomTests() }
                .onSuccess { _state.update { st -> st.copy(myTests = it, error = null) } }
                .onFailure { _state.update { st -> st.copy(error = it.toUiMessage(defaultMessage = "Ошибка загрузки")) } }
        }
    }

    fun loadAvailableTests() {
        viewModelScope.launch {
            runCatching { repository.getAvailableCustomTests() }
                .onSuccess { _state.update { st -> st.copy(availableTests = it, error = null) } }
                .onFailure { _state.update { st -> st.copy(error = it.toUiMessage(defaultMessage = "Ошибка загрузки")) } }
        }
    }

    fun loadDetails(testId: String) {
        viewModelScope.launch {
            runCatching { repository.getCustomTestDetails(testId) }
                .onSuccess { _state.update { st -> st.copy(details = it, error = null) } }
                .onFailure { _state.update { st -> st.copy(error = it.toUiMessage(defaultMessage = "Ошибка загрузки")) } }
        }
    }

    fun loadResults(testId: String) {
        viewModelScope.launch {
            runCatching { repository.getCustomTestResults(testId) }
                .onSuccess { _state.update { st -> st.copy(results = it, error = null) } }
                .onFailure { _state.update { st -> st.copy(error = it.toUiMessage(defaultMessage = "Ошибка загрузки")) } }
        }
    }

    fun loadStatistics(testId: String) {
        viewModelScope.launch {
            runCatching { repository.getCustomTestStatistics(testId) }
                .onSuccess { _state.update { st -> st.copy(statistics = it, error = null) } }
                .onFailure { _state.update { st -> st.copy(error = it.toUiMessage(defaultMessage = "Ошибка загрузки")) } }
        }
    }

    fun submitAnswers(testId: String, answers: Map<String, String>, onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                repository.submitCustomTest(
                    testId,
                    CustomTestSubmissionDraft(answers.map { CustomTestAnswerDraft(questionId = it.key, optionId = it.value) }),
                )
            }.onSuccess { onDone() }
                .onFailure { _state.update { st -> st.copy(error = it.toUiMessage(defaultMessage = "Ошибка отправки")) } }
        }
    }
}

data class EditableCustomQuestion(
    val text: String = "",
    val options: List<String> = listOf("", ""),
)

data class CustomTestsUiState(
    val title: String = "",
    val description: String = "",
    val allowedEmailsInput: String = "",
    val questions: List<EditableCustomQuestion> = listOf(EditableCustomQuestion()),
    val myTests: List<CustomTestListItem> = emptyList(),
    val availableTests: List<CustomTestListItem> = emptyList(),
    val details: com.example.diplomproject.domain.model.CustomTestDetails? = null,
    val results: List<com.example.diplomproject.domain.model.CustomTestResultItem> = emptyList(),
    val statistics: com.example.diplomproject.domain.model.CustomTestStatistics? = null,
    val error: String? = null,
    val success: String? = null,
)

@Composable
fun ControllerCustomTestsScreen(
    onBackClick: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onOpenResults: (String) -> Unit,
    onOpenStatistics: (String) -> Unit,
    viewModel: CustomTestsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadMyTests() }

    AppScreenScaffold(title = "Мои пользовательские тесты", onBackClick = onBackClick) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.myTests) { test ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(test.title, style = MaterialTheme.typography.titleMedium)
                        Text(test.description ?: "Без описания")
                        Text("Вопросов: ${test.questionsCount}; Email: ${test.allowedEmailsCount}; Прохождений: ${test.submissionsCount}")
                        Text("Создан: ${test.createdAt}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onOpenDetails(test.id) }) { Text("Детали") }
                            OutlinedButton(onClick = { onOpenResults(test.id) }) { Text("Результаты") }
                            OutlinedButton(onClick = { onOpenStatistics(test.id) }) { Text("Статистика") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateCustomTestsScreen(
    onBackClick: () -> Unit,
    onOpenTest: (String) -> Unit,
    viewModel: CustomTestsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadAvailableTests() }
    AppScreenScaffold(title = "Доступные пользовательские тесты", onBackClick = onBackClick) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.availableTests) { test ->
                ActionCard(title = test.title, subtitle = test.description ?: "Без описания", onClick = { onOpenTest(test.id) })
            }
        }
    }
}

@Composable
fun ControllerCustomTestDetailsScreen(testId: String, onBackClick: () -> Unit, viewModel: CustomTestsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    androidx.compose.runtime.LaunchedEffect(testId) { viewModel.loadDetails(testId) }
    val details = state.details
    AppScreenScaffold(title = "Детали теста", onBackClick = onBackClick) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (details == null) Text("Загрузка...") else {
                Text(details.title, style = MaterialTheme.typography.titleLarge)
                Text(details.description ?: "Без описания")
                Text("Доступ по email:")
                details.allowedEmails.forEach { Text("• $it") }
                details.questions.forEach { q ->
                    Text("${q.order}. ${q.text}", style = MaterialTheme.typography.titleSmall)
                    q.options.forEach { Text("   - ${it.text}") }
                }
            }
        }
    }
}

@Composable
fun CandidateCustomTestPassScreen(testId: String, onBackClick: () -> Unit, onSubmitted: () -> Unit, viewModel: CustomTestsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    androidx.compose.runtime.LaunchedEffect(testId) { viewModel.loadDetails(testId) }
    val answers = remember { mutableStateMapOf<String, String>() }
    val details = state.details
    AppScreenScaffold(title = "Прохождение теста", onBackClick = onBackClick) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (details == null) Text("Загрузка...") else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(details.questions) { question ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(question.text)
                                question.options.forEach { option ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = answers[question.id] == option.id,
                                            onClick = { answers[question.id] = option.id },
                                        )
                                        Text(option.text)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Button(onClick = { viewModel.submitAnswers(testId, answers, onSubmitted) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Завершить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControllerCustomTestResultsScreen(testId: String, onBackClick: () -> Unit, viewModel: CustomTestsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    androidx.compose.runtime.LaunchedEffect(testId) { viewModel.loadResults(testId) }
    AppScreenScaffold(title = "Результаты теста", onBackClick = onBackClick) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.results) { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${result.userName} (${result.userEmail})")
                        Text(result.submittedAt)
                        result.answers.forEach { Text("${it.questionText} → ${it.selectedOptionText}") }
                    }
                }
            }
        }
    }
}

@Composable
fun ControllerCustomTestStatisticsScreen(testId: String, onBackClick: () -> Unit, viewModel: CustomTestsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    androidx.compose.runtime.LaunchedEffect(testId) { viewModel.loadStatistics(testId) }
    val stats = state.statistics
    AppScreenScaffold(title = "Статистика теста", onBackClick = onBackClick) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (stats == null) {
                item { Text("Загрузка...") }
            } else {
                item { Text("Всего прохождений: ${stats.totalSubmissions}") }
                items(stats.questions) { question ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(question.questionText, style = MaterialTheme.typography.titleSmall)
                            question.options.forEach { option ->
                                Text("${option.optionText}: ${option.selectionsCount} (${String.format("%.1f", option.selectionsPercent)}%)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControllerCreateCustomTestScreen(onBackClick: () -> Unit, onSuccess: () -> Unit, viewModel: CustomTestsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    AppScreenScaffold(title = "Создать пользовательский тест", onBackClick = onBackClick) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChanged,
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.allowedEmailsInput,
                        onValueChange = viewModel::onEmailsChanged,
                        label = { Text("Разрешенные email (по строкам)") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                itemsIndexed(state.questions) { index, q ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Вопрос ${index + 1}", modifier = Modifier.weight(1f))
                                if (state.questions.size > 1) IconButton(onClick = { viewModel.removeQuestion(index) }) { Text("✕") }
                            }
                            OutlinedTextField(
                                value = q.text,
                                onValueChange = { viewModel.onQuestionChanged(index, it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Текст вопроса") },
                            )
                            q.options.forEachIndexed { optionIndex, option ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = option,
                                        onValueChange = { viewModel.onOptionChanged(index, optionIndex, it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Вариант ${optionIndex + 1}") },
                                    )
                                    if (q.options.size > 2) IconButton(onClick = { viewModel.removeOption(index, optionIndex) }) { Text("✕") }
                                }
                            }
                            OutlinedButton(onClick = { viewModel.addOption(index) }) { Text("Добавить вариант") }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = viewModel::addQuestion, modifier = Modifier.fillMaxWidth()) { Text("Добавить вопрос") }
                }
                item {
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                item {
                    state.success?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                }
            }
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { viewModel.createCustomTest(onSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) { Text("Сохранить") }
        }
    }
}

@Composable
private fun <T> StateFlow<T>.collectAsStateWithLifecycleCompat(): State<T> = this.collectAsState(initial = value)

private fun Throwable.toUiMessage(defaultMessage: String = "Ошибка"): String {
    val httpException = this as? HttpException ?: return message ?: defaultMessage
    val payload = httpException.response()?.errorBody()?.string().orEmpty()
    val parsedMessage = runCatching {
        val json = JSONObject(payload)
        when {
            json.optString("message").isNotBlank() -> json.optString("message")
            json.optString("error").isNotBlank() -> json.optString("error")
            else -> null
        }
    }.getOrNull()
    return parsedMessage ?: "HTTP ${httpException.code()}"
}
