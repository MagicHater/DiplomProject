package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SessionAnswersScreen(
    uiState: SessionAnswersUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    AppScreenScaffold(title = "История ответов", onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onRetryClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Повторить")
                    }
                }
                uiState.answers.isEmpty() -> Text("Ответы по этой сессии не найдены.")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.answers, key = { it.questionOrder }) { answer ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("Вопрос ${answer.questionOrder}", style = MaterialTheme.typography.titleMedium)
                                Text(answer.questionText, style = MaterialTheme.typography.bodyMedium)
                                HorizontalDivider()
                                Text("Выбранный ответ", fontWeight = FontWeight.SemiBold)
                                Text(answer.selectedAnswerText, style = MaterialTheme.typography.bodyMedium)
                                answer.answerValue?.let { Text("Вклад ответа: $it", style = MaterialTheme.typography.bodySmall) }
                                answer.responseTimeMs?.let { Text("Время ответа: ${it / 1000.0} сек", style = MaterialTheme.typography.bodySmall) }
                                Text("Сложность: ${answer.difficulty}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
