package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ControllerDashboardScreen(
    onBack: () -> Unit
) {
    val vm: ControllerDashboardViewModel = hiltViewModel()
    val data by vm.state.collectAsState()
    val loading by vm.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Дашборд аналитики", style = MaterialTheme.typography.titleLarge)

        if (loading) {
            CircularProgressIndicator()
            return
        }

        data?.let {
            Text("Сессий: ${it.totalCompletedSessions}")
            Text("Участников: ${it.totalParticipants}")

            Spacer(Modifier.height(12.dp))

            Text("Средние показатели:")
            Text("Стресс: ${it.averages.stressResistance}")
            Text("Внимание: ${it.averages.attention}")

            Spacer(Modifier.height(12.dp))

            Text("Топ кандидаты:")
            LazyColumn {
                items(it.topCandidates) { c ->
                    Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        Column(Modifier.padding(8.dp)) {
                            Text(c.displayName)
                            Text("Score: ${c.averageScore}")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Назад")
        }
    }
}
