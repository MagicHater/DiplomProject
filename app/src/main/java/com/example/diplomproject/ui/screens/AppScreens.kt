package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun TestScreen(
    sessionId: String,
    onFinishTestClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
) {
    ScreenStub(
        title = "TestScreen\nSession: $sessionId",
        actions = listOf(
            "Завершить тест" to onFinishTestClick,
            "Назад в CandidateHome" to onBackToHomeClick,
        ),
    )
}

@Composable
fun ResultScreen(
    onHistoryClick: () -> Unit,
    onBackToCandidateHomeClick: () -> Unit,
) {
    ScreenStub(
        title = "ResultScreen",
        actions = listOf(
            "Открыть History" to onHistoryClick,
            "Назад в CandidateHome" to onBackToCandidateHomeClick,
        ),
    )
}

@Composable
fun HistoryScreen(
    onBackToHomeClick: () -> Unit,
) {
    ScreenStub(
        title = "HistoryScreen",
        actions = listOf("Назад в домашний кабинет" to onBackToHomeClick),
    )
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
