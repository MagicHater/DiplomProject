package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onCandidateHomeClick: () -> Unit,
    onControllerHomeClick: () -> Unit,
) {
    ScreenStub(
        title = "LoginScreen",
        actions = listOf(
            "Перейти к Register" to onRegisterClick,
            "Войти как Candidate" to onCandidateHomeClick,
            "Войти как Controller" to onControllerHomeClick,
        ),
    )
}

@Composable
fun RegisterScreen(onLoginClick: () -> Unit) {
    ScreenStub(
        title = "RegisterScreen",
        actions = listOf("Назад к Login" to onLoginClick),
    )
}

@Composable
fun CandidateHomeScreen(
    onStartTestClick: () -> Unit,
    onResultClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    ScreenStub(
        title = "CandidateHomeScreen",
        actions = listOf(
            "Открыть Test" to onStartTestClick,
            "Открыть Result" to onResultClick,
            "Открыть History" to onHistoryClick,
            "Выйти" to onLogoutClick,
        ),
    )
}

@Composable
fun ControllerHomeScreen(
    onCandidateListClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    ScreenStub(
        title = "ControllerHomeScreen",
        actions = listOf(
            "Открыть CandidateList" to onCandidateListClick,
            "Открыть History" to onHistoryClick,
            "Выйти" to onLogoutClick,
        ),
    )
}

@Composable
fun TestScreen(
    onFinishTestClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
) {
    ScreenStub(
        title = "TestScreen",
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
    onCandidateHomeClick: () -> Unit,
    onControllerHomeClick: () -> Unit,
) {
    ScreenStub(
        title = "HistoryScreen",
        actions = listOf(
            "К CandidateHome" to onCandidateHomeClick,
            "К ControllerHome" to onControllerHomeClick,
        ),
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
