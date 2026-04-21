package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ControllerCreateTestScreen(
    uiState: ControllerCreateTestUiState,
    onBackClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAddQuestion: () -> Unit,
    onRemoveQuestion: (Int) -> Unit,
    onQuestionTextChanged: (Int, String) -> Unit,
    onAddOption: (Int) -> Unit,
    onRemoveOption: (Int, Int) -> Unit,
    onOptionTextChanged: (Int, Int, String) -> Unit,
    onOptionOrderChanged: (Int, Int, String) -> Unit,
    onOptionContributionChanged: (Int, Int, String) -> Unit,
    onOptionScaleChanged: (Int, Int, ControllerMetricInput, String) -> Unit,
    onSaveClick: () -> Unit,
) {
    AppScreenScaffold(title = "Новый тест", onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название теста") },
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Описание") },
                minLines = 2,
            )

            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            uiState.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            uiState.questions.forEachIndexed { questionIndex, question ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Вопрос ${questionIndex + 1}", style = MaterialTheme.typography.titleMedium)
                            if (uiState.questions.size > 1) {
                                IconButton(onClick = { onRemoveQuestion(questionIndex) }) { Text("✕") }
                            }
                        }

                        OutlinedTextField(
                            value = question.text,
                            onValueChange = { onQuestionTextChanged(questionIndex, it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Текст вопроса") },
                        )

                        question.options.forEachIndexed { optionIndex, option ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Вариант ${optionIndex + 1}")
                                        if (question.options.size > 2) {
                                            IconButton(onClick = { onRemoveOption(questionIndex, optionIndex) }) { Text("✕") }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = option.text,
                                        onValueChange = { onOptionTextChanged(questionIndex, optionIndex, it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Текст варианта") },
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = option.order,
                                            onValueChange = { onOptionOrderChanged(questionIndex, optionIndex, it) },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("Порядок") },
                                        )
                                        OutlinedTextField(
                                            value = option.contributionValue,
                                            onValueChange = { onOptionContributionChanged(questionIndex, optionIndex, it) },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("Баллы") },
                                        )
                                    }

                                    ControllerMetricInput.entries.forEach { metric ->
                                        val value = when (metric) {
                                            ControllerMetricInput.Attention -> option.attention
                                            ControllerMetricInput.StressResistance -> option.stressResistance
                                            ControllerMetricInput.Responsibility -> option.responsibility
                                            ControllerMetricInput.Adaptability -> option.adaptability
                                            ControllerMetricInput.DecisionSpeedAccuracy -> option.decisionSpeedAccuracy
                                        }
                                        OutlinedTextField(
                                            value = value,
                                            onValueChange = { onOptionScaleChanged(questionIndex, optionIndex, metric, it) },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text(metric.title) },
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(onClick = { onAddOption(questionIndex) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Добавить вариант")
                        }
                    }
                }
            }

            OutlinedButton(onClick = onAddQuestion, modifier = Modifier.fillMaxWidth()) {
                Text("Добавить вопрос")
            }

            Button(onClick = onSaveClick, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("Сохранить тест")
            }
        }
    }
}
