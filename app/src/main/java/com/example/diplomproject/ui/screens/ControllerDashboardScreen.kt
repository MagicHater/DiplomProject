package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomproject.data.remote.ControllerDashboardCandidateRankResponseDto
import com.example.diplomproject.data.remote.ControllerDashboardCategoryStatisticsResponseDto
import kotlin.math.roundToInt

@Composable
fun ControllerDashboardScreen(
    onBack: () -> Unit,
) {
    val vm: ControllerDashboardViewModel = hiltViewModel()
    val data by vm.state.collectAsState()
    val loading by vm.loading.collectAsState()

    AppScreenScaffold(title = "Дашборд аналитики", onBackClick = onBack) { innerPadding ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            data == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Не удалось загрузить данные дашборда.", color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = vm::load, modifier = Modifier.fillMaxWidth()) {
                        Text("Повторить")
                    }
                }
            }

            else -> {
                val dashboard = data!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DashboardStatCard(
                                title = "Сессий",
                                value = dashboard.totalCompletedSessions.toString(),
                                modifier = Modifier.weight(1f),
                            )
                            DashboardStatCard(
                                title = "Участников",
                                value = dashboard.totalParticipants.toString(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    item {
                        Text("Статистика по тестам", style = MaterialTheme.typography.titleMedium)
                    }

                    if (dashboard.categoryStatistics.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Пока нет данных по категориям тестов.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    } else {
                        items(dashboard.categoryStatistics, key = { it.categoryId }) { category ->
                            CategoryAnalyticsCard(category = category)
                        }
                    }

                    item {
                        CandidateComparisonChartCard(candidates = dashboard.topCandidates)
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("Распределение результатов", style = MaterialTheme.typography.titleMedium)
                                Text("Низкий уровень: ${dashboard.distribution.low}")
                                Text("Средний уровень: ${dashboard.distribution.medium}")
                                Text("Высокий уровень: ${dashboard.distribution.high}")
                            }
                        }
                    }

                    item {
                        Text("Слабые метрики", style = MaterialTheme.typography.titleMedium)
                    }

                    items(dashboard.weakMetrics, key = { it.metricCode }) { metric ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(metric.title, fontWeight = FontWeight.SemiBold)
                                DashboardMetricBar("Среднее значение", metric.average)
                            }
                        }
                    }

                    item {
                        Text("Рейтинг кандидатов", style = MaterialTheme.typography.titleMedium)
                    }

                    items(dashboard.topCandidates, key = { it.participantId }) { candidate ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(candidate.displayName, style = MaterialTheme.typography.titleMedium)
                                candidate.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                Text("Сессий: ${candidate.sessionsCount}", style = MaterialTheme.typography.bodySmall)
                                DashboardMetricBar("Средний балл", candidate.averageScore)
                            }
                        }
                    }

                    item {
                        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                            Text("Назад")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryAnalyticsCard(category: ControllerDashboardCategoryStatisticsResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardStatCard(
                    title = "Сессий",
                    value = category.sessionsCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                DashboardStatCard(
                    title = "Средний балл",
                    value = category.averageScore.formatScore(),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "Слабая зона: ${category.weakestMetricTitle} (${category.weakestMetricAverage.formatScore()})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
            )
            DashboardMetricBar("Стрессоустойчивость", category.stressResistance)
            DashboardMetricBar("Внимание", category.attention)
            DashboardMetricBar("Ответственность", category.responsibility)
            DashboardMetricBar("Адаптивность", category.adaptability)
            DashboardMetricBar("Скорость решений", category.decisionSpeedAccuracy)
        }
    }
}

@Composable
private fun DashboardMetricBar(title: String, value: Double) {
    val progress = (value / 100.0).toFloat().coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(value.formatScore(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CandidateComparisonChartCard(candidates: List<ControllerDashboardCandidateRankResponseDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Сравнение кандидатов", style = MaterialTheme.typography.titleMedium)
            Text(
                "Средний итоговый балл по участникам. Чем длиннее полоса, тем выше результат.",
                style = MaterialTheme.typography.bodySmall,
            )

            if (candidates.isEmpty()) {
                Text("Пока нет данных для сравнения.", style = MaterialTheme.typography.bodyMedium)
            } else {
                candidates.take(5).forEachIndexed { index, candidate ->
                    CandidateComparisonRow(index = index, candidate = candidate)
                }
            }
        }
    }
}

@Composable
private fun CandidateComparisonRow(index: Int, candidate: ControllerDashboardCandidateRankResponseDto) {
    val score = candidate.averageScore.coerceIn(0.0, 100.0)
    val progress = (score / 100.0).toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${index + 1}. ${candidate.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = score.formatScore(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Сессий: ${candidate.sessionsCount}" + (candidate.email?.let { " · $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Double.formatScore(): String = "${roundToInt()}"
