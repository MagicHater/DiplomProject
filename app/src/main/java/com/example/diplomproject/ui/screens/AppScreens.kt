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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diplomproject.domain.model.FinishedSessionResult
import com.example.diplomproject.ui.components.ProfileRadarChart
import com.example.diplomproject.ui.components.RadarMetric
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
@OptIn(ExperimentalMaterial3Api::class)
fun ControllerHomeScreen(
    uiState: ControllerHomeUiState,
    onCategorySelected: (String) -> Unit,
    onGenerateTokenClick: () -> Unit,
    onCandidateListClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    var categoryExpanded by remember { mutableStateOf(false) }
    var copyStatus by remember(uiState.generatedToken) { mutableStateOf<String?>(null) }
    val selectedCategoryName = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }?.name.orEmpty()
    val tokenValue = uiState.generatedToken.ifBlank { "Сначала нажмите «Сгенерировать токен»" }

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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Генерация токена", style = MaterialTheme.typography.titleMedium)

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    uiState.infoMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Категория") },
                            placeholder = { Text("Выберите категорию") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                        ) {
                            uiState.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        onCategorySelected(category.id)
                                        categoryExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onGenerateTokenClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Сгенерировать токен")
                    }

                    OutlinedTextField(
                        value = tokenValue,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        minLines = 2,
                        label = { Text("Сгенерированный токен") },
                    )

                    OutlinedButton(
                        onClick = {
                            if (uiState.generatedToken.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(uiState.generatedToken))
                                copyStatus = "Токен скопирован"
                            }
                        },
                        enabled = uiState.generatedToken.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Копировать токен")
                    }

                    Text(
                        text = copyStatus ?: "После генерации токен можно сразу скопировать из поля выше.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
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
    val scrollState = rememberScrollState()

    AppScreenScaffold(title = "Итоговый отчёт") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    uiState.isLoading -> LoadingState(message = "Загружаем результат...")
                    uiState.errorMessage != null -> ErrorState(
                        message = uiState.errorMessage,
                        actionLabel = "Повторить",
                        onActionClick = onRetryClick,
                    )
                    uiState.emptyMessage != null -> EmptyState(uiState.emptyMessage)
                    uiState.result != null -> ResultContent(
                        result = uiState.result,
                    )
                }
            }

            OutlinedButton(
                onClick = onHistoryClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("К истории результатов")
            }

            TextButton(
                onClick = onBackToCandidateHomeClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("В кабинет кандидата")
            }
        }
    }
}

@Composable
fun GuestCompletionScreen(
    onExitClick: () -> Unit,
) {
    AppScreenScaffold(title = "Тестирование завершено") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Спасибо за прохождение тестирования",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Ваши ответы успешно сохранены. Теперь вы можете завершить работу в приложении.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Button(
                onClick = onExitClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Выйти")
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResultContent(
    result: FinishedSessionResult,
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Профиль шкал", style = MaterialTheme.typography.titleMedium)

            ProfileRadarChart(
                metrics = radarMetrics(result),
                maxValue = 100f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    SectionHeader(title = "Шкалы и интерпретации")
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        scaleItems(result.scores, result.interpretations).forEach { scale ->
            ScaleItemCard(item = scale)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = "Итоговый профиль используется как рекомендательный инструмент и требует экспертной интерпретации.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun radarMetrics(result: FinishedSessionResult): List<RadarMetric> {
    return listOf(
        RadarMetric("Стресс", result.scores.stressResistance.toFloat()),
        RadarMetric("Внимание", result.scores.attention.toFloat()),
        RadarMetric("Ответств.", result.scores.responsibility.toFloat()),
        RadarMetric("Адаптивн.", result.scores.adaptability.toFloat()),
        RadarMetric("Скор./точн.", result.scores.decisionSpeedAccuracy.toFloat()),
    )
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
                    val isController = uiState.role == com.example.diplomproject.domain.model.UserRole.Controller
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (isController) {
                            items(uiState.controllerItems, key = { it.sessionId }) { item ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(item.completedAt.formatIsoDateTime(), style = MaterialTheme.typography.labelMedium)
                                        Text(item.category.name, style = MaterialTheme.typography.labelMedium)
                                        Text(
                                            text = "Проходил: ${item.participantDisplayName ?: "Неизвестно"} (${item.participantType})",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
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
                        } else {
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
            }

            if (uiState.items.isEmpty() && uiState.controllerItems.isEmpty()) {
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
    uiState: CandidateListUiState,
    onCandidateDetailsClick: (participantType: String, participantKey: String) -> Unit,
    onBackToControllerHomeClick: () -> Unit,
    onRetryClick: () -> Unit,
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

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onRetryClick) { Text("Повторить") }
                }
                uiState.participants.isEmpty() -> {
                    Text("Пока нет завершённых тестов по вашим токенам.", style = MaterialTheme.typography.bodyMedium)
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.participants, key = { it.participantId }) { participant ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(participant.displayName, style = MaterialTheme.typography.titleMedium)
                                    participant.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                    val typeLabel = if (participant.participantType == "guest") "Гость" else "Кандидат"
                                    Text(
                                        "$typeLabel • Завершённых сессий: ${participant.completedSessionsCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            onCandidateDetailsClick(participant.participantType, participant.participantKey)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Открыть карточку")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateDetailsScreen(
    uiState: CandidateDetailsUiState,
    onBackToCandidateListClick: () -> Unit,
) {
    AppScreenScaffold(title = "Карточка кандидата", onBackClick = onBackToCandidateListClick) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                ) {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.participant == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                ) { Text("Нет данных по участнику") }
            }
            else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val participant = uiState.participant
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Профиль участника", style = MaterialTheme.typography.titleMedium)
                        Text("Имя: ${participant.displayName}", style = MaterialTheme.typography.bodyMedium)
                        participant.email?.let { Text("Email: $it", style = MaterialTheme.typography.bodyMedium) }
                        Text(
                            "Тип: ${if (participant.participantType == "guest") "Гость" else "Кандидат"}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            items(participant.sessions, key = { it.sessionId }) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(session.completedAt.formatIsoDateTime(), style = MaterialTheme.typography.labelMedium)
                        Text(session.summary, style = MaterialTheme.typography.titleMedium)
                        compactScoreItems(session.scores).forEach { (title, value) ->
                            CompactScoreRow(title = title, value = value)
                        }
                    }
                }
            }
        }
        }
    }
}
