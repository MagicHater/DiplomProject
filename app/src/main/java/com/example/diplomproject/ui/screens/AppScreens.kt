package com.example.diplomproject.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diplomproject.domain.model.FinishedSessionResult
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CandidateHomeScreen(
    uiState: CandidateHomeUiState,
    onStartTestClick: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onTokenInputChanged: (String) -> Unit,
    onStartByTokenClick: () -> Unit,
    onResultClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    AppScreenScaffold(title = "Кабинет кандидата") { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Профессиональное психологическое тестирование", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Ответьте на вопросы, чтобы получить профиль с интерпретацией ключевых шкал.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                item {
                    ErrorState(
                        message = error,
                        actionLabel = "Повторить",
                        onActionClick = onStartTestClick,
                    )
                }
            }

            item {
                Text("Категория теста", style = MaterialTheme.typography.titleSmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.categories.forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.selectedCategoryId == category.id,
                                onClick = { onCategorySelected(category.id) },
                            )
                            Text(category.name)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.tokenInput,
                    onValueChange = onTokenInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Токен доступа") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onStartByTokenClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Пройти по токену")
                }
            }

            item {
                Button(
                    onClick = onStartTestClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Начать тестирование")
                }
            }

            item {
                OutlinedButton(onClick = onHistoryClick, modifier = Modifier.fillMaxWidth()) {
                    Text("История результатов")
                }
            }

            item {
                TextButton(onClick = onResultClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Открыть последний результат")
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        text = "Результаты предназначены для рекомендаций и не являются медицинским диагнозом.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            item {
                TextButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Выйти из аккаунта")
                }
            }
        }
    }
}

@Composable
fun ControllerHomeScreen(
    uiState: ControllerHomeUiState,
    onCategorySelected: (String) -> Unit,
    onGenerateTokenClick: () -> Unit,
    onCandidateListClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    AppScreenScaffold(title = "Кабинет экзаменатора") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Управляйте списком кандидатов и анализируйте результаты тестирования.",
                style = MaterialTheme.typography.bodyMedium,
            )

            ActionCard(
                title = "Список кандидатов",
                subtitle = "Просмотр профилей и переход к детальной карточке кандидата",
                onClick = onCandidateListClick,
            )

            ActionCard(
                title = "История и результаты",
                subtitle = "Сводка завершённых сессий и доступ к полным отчётам",
                onClick = onHistoryClick,
            )

            Text("Генерация токена", style = MaterialTheme.typography.titleMedium)
            uiState.categories.forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                    )
                    Text(category.name)
                }
            }
            Button(onClick = onGenerateTokenClick, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading) {
                Text("Сгенерировать токен")
            }
            if (uiState.generatedToken.isNotBlank()) {
                Text("Токен: ${uiState.generatedToken}")
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                Text("Выйти из аккаунта")
            }
        }
    }
}

@Composable
fun TestQuestionScreen(
    uiState: TestUiState,
    onOptionSelected: (String) -> Unit,
    onNextClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    AppScreenScaffold(title = "Тестирование") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val question = uiState.question

            if (question != null) {
                val progress = if (uiState.progress.totalAvailableQuestions > 0) {
                    (uiState.progress.answeredQuestions.toFloat() / uiState.progress.totalAvailableQuestions.toFloat())
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }

                Text(
                    text = formatQuestionCounterText(
                        questionOrder = question.order,
                        totalAvailableQuestions = uiState.progress.totalAvailableQuestions,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text(
                    text = "Отвечено: ${uiState.progress.answeredQuestions}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (uiState.isInitialLoading) {
                LoadingState(message = "Загружаем вопрос...")
            }

            uiState.errorMessage?.let { error ->
                ErrorState(
                    message = error,
                    actionLabel = if (question == null) "Повторить загрузку" else "Повторить",
                    onActionClick = onRetryClick,
                )
            }

            if (question != null) {
                Text(text = question.text, style = MaterialTheme.typography.titleLarge)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.sortedBy { it.order }.forEach { option ->
                        SelectableOptionCard(
                            text = option.text,
                            selected = option.optionId == uiState.selectedOptionId,
                            enabled = !uiState.isBusy,
                            onClick = { onOptionSelected(option.optionId) },
                        )
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
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Ответить и продолжить")
            }
        }
    }
}

internal fun formatQuestionCounterText(
    questionOrder: Int,
    totalAvailableQuestions: Int,
): String {
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
    AppScreenScaffold(title = "Итоговый отчёт") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isLoading -> LoadingState(message = "Загружаем результат...")
                uiState.errorMessage != null -> ErrorState(uiState.errorMessage, "Повторить", onRetryClick)
                uiState.emptyMessage != null -> EmptyState(uiState.emptyMessage)
                uiState.result != null -> ResultContent(result = uiState.result, isChartPlaceholderVisible = uiState.isChartPlaceholderVisible)
            }

            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onHistoryClick, modifier = Modifier.fillMaxWidth()) {
                Text("К истории результатов")
            }
            TextButton(onClick = onBackToCandidateHomeClick, modifier = Modifier.fillMaxWidth()) {
                Text("В кабинет кандидата")
            }
        }
    }
}

@Composable
private fun ResultContent(
    result: FinishedSessionResult,
    isChartPlaceholderVisible: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Дата прохождения", style = MaterialTheme.typography.labelLarge)
            Text(result.completedAt.formatIsoDateTime(), style = MaterialTheme.typography.bodyMedium)
            Text("Общий вывод", style = MaterialTheme.typography.labelLarge)
            Text(result.overallSummary, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (isChartPlaceholderVisible) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Профиль шкал", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Визуализация профиля", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    SectionHeader(title = "Шкалы и интерпретации")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        scaleItems(result.scores, result.interpretations).forEach { scale ->
            ScaleItemCard(item = scale)
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = "Итоговый профиль используется как рекомендательный инструмент и требует экспертной интерпретации.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onResultClick: (String) -> Unit,
    onBackToHomeClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    AppScreenScaffold(title = "История результатов") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isLoading -> LoadingState(message = "Загружаем историю...")
                uiState.errorMessage != null -> ErrorState(uiState.errorMessage, "Повторить", onRetryClick)
                uiState.infoMessage != null -> EmptyState(uiState.infoMessage)
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(uiState.items, key = { it.sessionId }) { item ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(item.completedAt.formatIsoDateTime(), style = MaterialTheme.typography.labelMedium)
                                    Text(item.summary, style = MaterialTheme.typography.titleMedium)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        compactScoreItems(item.scores).forEach { (title, value) ->
                                            CompactScoreRow(title = title, value = value)
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { onResultClick(item.sessionId) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Полный отчёт")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.items.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Button(onClick = onBackToHomeClick, modifier = Modifier.fillMaxWidth()) {
                Text("Вернуться в кабинет")
            }
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
    AppScreenScaffold(title = "Кандидаты", onBackClick = onBackToControllerHomeClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Выберите кандидата для просмотра профиля и аналитики прохождений.",
                style = MaterialTheme.typography.bodyMedium,
            )

            listOf(
                "Иван Петров" to "3 завершённых сессии",
                "Анна Смирнова" to "1 завершённая сессия",
                "Дмитрий Волков" to "Сессии в обработке",
            ).forEach { (name, status) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        Text(status, style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = onCandidateDetailsClick, modifier = Modifier.fillMaxWidth()) {
                            Text("Открыть карточку")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateDetailsScreen(onBackToCandidateListClick: () -> Unit) {
    AppScreenScaffold(title = "Карточка кандидата", onBackClick = onBackToCandidateListClick) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Профиль кандидата", style = MaterialTheme.typography.titleMedium)
                        Text("ФИО: Иван Петров", style = MaterialTheme.typography.bodyMedium)
                        Text("Статус: активный участник", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Результаты и сессии", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Динамика по шкалам, ключевые выводы и история прохождений отображаются в этом разделе.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
