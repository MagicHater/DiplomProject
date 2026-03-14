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
                text = "Вопрос ${question.order}. Отвечено ${uiState.progress.answeredQuestions}",
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

@Composable
fun ResultScreen(
    result: com.example.diplomproject.domain.model.FinishedSessionResult?,
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

        if (result == null) {
            Text(text = "Нет данных по завершённому тесту.")
        } else {
            Text(text = result.overallSummary, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Внимание: ${result.scores.attention}")
            Text(text = "Стрессоустойчивость: ${result.scores.stressResistance}")
            Text(text = "Ответственность: ${result.scores.responsibility}")
            Text(text = "Адаптивность: ${result.scores.adaptability}")
            Text(text = "Скорость/точность решений: ${result.scores.decisionSpeedAccuracy}")
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
fun HistoryScreen(
    uiState: HistoryUiState,
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items, key = { it.sessionId }) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.summary,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "Завершено: ${item.completedAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Внимание ${item.scores.attention}, Стресс ${item.scores.stressResistance}, " +
                                        "Ответственность ${item.scores.responsibility}, Адаптивность ${item.scores.adaptability}, " +
                                        "Решения ${item.scores.decisionSpeedAccuracy}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBackToHomeClick, modifier = Modifier.fillMaxWidth()) {
            Text("Назад в домашний кабинет")
        }
    }
}

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
