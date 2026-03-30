package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diplomproject.domain.model.FinishedSessionResult
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
private fun ScreenStub(
    title: String,
    actions: List<Pair<String, () -> Unit>>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        actions.forEach { (label, onClick) ->
            Button(onClick = onClick) {
                Text(text = label)
            }
        }
    }
}

@Composable
fun CandidateHomeScreen(
    uiState: CandidateHomeUiState,
    onStartTestClick: () -> Unit,
    onResultClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Кабинет кандидата", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Пройдите адаптивный тест. После старта вы сразу получите первый вопрос.",
            style = MaterialTheme.typography.bodyMedium,
        )

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onStartTestClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Начать тест")
            }
        }

        Button(onClick = onHistoryClick, modifier = Modifier.fillMaxWidth()) {
            Text("История результатов")
        }

        Button(onClick = onResultClick, modifier = Modifier.fillMaxWidth()) {
            Text("Текущий результат (заглушка)")
        }

        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
            Text("Выйти")
        }
    }
}

@Composable
fun ControllerHomeScreen(
    onCandidateListClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    ScreenStub(
        title = "Кабинет контроллера",
        actions = listOf(
            "Открыть CandidateList" to onCandidateListClick,
            "Открыть History" to onHistoryClick,
            "Выйти" to onLogoutClick,
        ),
    )
}

@Composable
fun TestQuestionScreen(
    uiState: TestUiState,
    onOptionSelected: (String) -> Unit,
    onNextClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Прохождение теста", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Session: ${uiState.sessionId}", style = MaterialTheme.typography.bodySmall)

        if (uiState.isInitialLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            if (uiState.question == null) {
                Button(onClick = onRetryClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Повторить")
                }
            }
        }

        uiState.question?.let { question ->
            val progress = if (uiState.progress.totalAvailableQuestions > 0) {
                (uiState.progress.answeredQuestions.toFloat() / uiState.progress.totalAvailableQuestions.toFloat())
                    .coerceIn(0f, 1f)
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = formatQuestionCounterText(
                    questionOrder = question.order,
                    totalAvailableQuestions = uiState.progress.totalAvailableQuestions,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Отвечено ${uiState.progress.answeredQuestions}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(text = question.text, style = MaterialTheme.typography.titleMedium)

            question.options.sortedBy { it.order }.forEach { option ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onOptionSelected(option.optionId) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isBusy,
                    ) {
                        RadioButton(
                            selected = option.optionId == uiState.selectedOptionId,
                            onClick = null,
                        )
                        Text(option.text, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNextClick,
            enabled = uiState.selectedOptionId != null && !uiState.isBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSubmitting || uiState.isFinishing) {
                CircularProgressIndicator()
            } else {
                Text("Далее")
            }
        }
    }
}

internal fun formatQuestionCounterText(
    questionOrder: Int,
    totalAvailableQuestions: Int,
): String {
    // We use backend question.order as a single source of truth; backend already sends 1-based numbering.
    return if (totalAvailableQuestions > 0) {
        "Вопрос $questionOrder из $totalAvailableQuestions"
    } else {
        "Вопрос $questionOrder"
    }
}

@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onRetryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBackToCandidateHomeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Результат", style = MaterialTheme.typography.headlineMedium)

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetryClick) {
                    Text("Повторить")
                }
            }

            uiState.emptyMessage != null -> {
                Text(text = uiState.emptyMessage, style = MaterialTheme.typography.bodyMedium)
            }

            uiState.result != null -> {
                ResultContent(result = uiState.result, isChartPlaceholderVisible = uiState.isChartPlaceholderVisible)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onHistoryClick, modifier = Modifier.fillMaxWidth()) {
            Text("Открыть History")
        }
        Button(onClick = onBackToCandidateHomeClick, modifier = Modifier.fillMaxWidth()) {
            Text("Назад в CandidateHome")
        }
    }
}

@Composable
private fun ResultContent(
    result: FinishedSessionResult,
    isChartPlaceholderVisible: Boolean,
) {
    Text(
        text = "Дата/время: ${result.completedAt.formatIsoDateTime()}",
        style = MaterialTheme.typography.bodySmall,
    )

    if (isChartPlaceholderVisible) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Radar chart будет добавлен на этом месте в следующих итерациях.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }
    }

    val scales = scaleItems(result.scores, result.interpretations)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        item {
            Text(text = "Summary", style = MaterialTheme.typography.titleMedium)
            Text(text = result.overallSummary, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Шкалы", style = MaterialTheme.typography.titleMedium)
        }
        items(scales) { scale ->
            ScaleItemCard(item = scale)
        }
    }
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onResultClick: (String) -> Unit,
    onBackToHomeClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "История результатов", style = MaterialTheme.typography.headlineMedium)

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> {
                Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetryClick) {
                    Text("Повторить")
                }
            }

            uiState.infoMessage != null -> {
                Text(text = uiState.infoMessage, style = MaterialTheme.typography.bodyMedium)
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.items, key = { it.sessionId }) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = item.completedAt.formatIsoDateTime(),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    text = item.summary,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                compactScoreItems(item.scores).forEach { (title, value) ->
                                    CompactScoreRow(title = title, value = value)
                                }
                                Button(
                                    onClick = { onResultClick(item.sessionId) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Открыть полный результат")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!uiState.items.any() || uiState.isLoading || uiState.errorMessage != null || uiState.infoMessage != null) {
            Spacer(modifier = Modifier.weight(1f))
        }

        Button(onClick = onBackToHomeClick, modifier = Modifier.fillMaxWidth()) {
            Text("Назад в домашний кабинет")
        }
    }
}

private fun String.formatIsoDateTime(): String = runCatching {
    OffsetDateTime.parse(this).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
}.getOrDefault(this)

@Composable
fun CandidateListScreen(
    onCandidateDetailsClick: () -> Unit,
    onBackToControllerHomeClick: () -> Unit,
) {
    ScreenStub(
        title = "CandidateListScreen",
        actions = listOf(
            "Открыть CandidateDetails" to onCandidateDetailsClick,
            "Назад в ControllerHome" to onBackToControllerHomeClick,
        ),
    )
}

@Composable
fun CandidateDetailsScreen(onBackToCandidateListClick: () -> Unit) {
    ScreenStub(
        title = "CandidateDetailsScreen",
        actions = listOf("Назад к CandidateList" to onBackToCandidateListClick),
    )
}
