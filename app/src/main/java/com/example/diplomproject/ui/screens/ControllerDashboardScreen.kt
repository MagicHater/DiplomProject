package com.example.diplomproject.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomproject.data.remote.ControllerDashboardCandidateRankResponseDto
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
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text("Средние показатели", style = MaterialTheme.typography.titleMedium)
                                DashboardMetricBar("Стрессоустойчивость", dashboard.averages.stressResistance)
                                DashboardMetricBar("Внимание", dashboard.averages.attention)
                                DashboardMetricBar("Ответственность", dashboard.averages.responsibility)
                                DashboardMetricBar("Адаптивность", dashboard.averages.adaptability)
                                DashboardMetricBar("Скорость решений", dashboard.averages.decisionSpeedAccuracy)
                                Text(
                                    text = "Общий средний балл: ${dashboard.averages.overall.formatScore()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Сравнение кандидатов", style = MaterialTheme.typography.titleMedium)
            Text(
                "График показывает средний итоговый балл по каждому участнику.",
                style = MaterialTheme.typography.bodySmall,
            )

            if (candidates.isEmpty()) {
                Text("Пока нет данных для сравнения.", style = MaterialTheme.typography.bodyMedium)
            } else {
                CandidateComparisonBarChart(
                    candidates = candidates,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                )
            }
        }
    }
}

@Composable
private fun CandidateComparisonBarChart(
    candidates: List<ControllerDashboardCandidateRankResponseDto>,
    modifier: Modifier = Modifier,
) {
    val barColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val gridColor = axisColor.copy(alpha = 0.20f)
    val labelColor = MaterialTheme.colorScheme.onSurface
    val items = candidates.take(5)

    Canvas(modifier = modifier) {
        val leftPadding = 44.dp.toPx()
        val rightPadding = 12.dp.toPx()
        val topPadding = 18.dp.toPx()
        val bottomPadding = 58.dp.toPx()
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding
        val zeroY = topPadding + chartHeight

        val labelPaint = android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = 11.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        val yLabelPaint = android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = 10.dp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        listOf(0, 25, 50, 75, 100).forEach { tick ->
            val y = zeroY - (tick / 100f) * chartHeight
            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
            )
            drawIntoCanvas {
                it.nativeCanvas.drawText(tick.toString(), leftPadding - 8.dp.toPx(), y + 3.dp.toPx(), yLabelPaint)
            }
        }

        drawLine(
            color = axisColor,
            start = Offset(leftPadding, topPadding),
            end = Offset(leftPadding, zeroY),
            strokeWidth = 1.2.dp.toPx(),
        )
        drawLine(
            color = axisColor,
            start = Offset(leftPadding, zeroY),
            end = Offset(leftPadding + chartWidth, zeroY),
            strokeWidth = 1.2.dp.toPx(),
        )

        val slotWidth = chartWidth / items.size.coerceAtLeast(1)
        val barWidth = slotWidth * 0.48f

        items.forEachIndexed { index, candidate ->
            val value = candidate.averageScore.toFloat().coerceIn(0f, 100f)
            val barHeight = (value / 100f) * chartHeight
            val centerX = leftPadding + slotWidth * index + slotWidth / 2f
            val y = zeroY - barHeight

            drawLine(
                color = barColor,
                start = Offset(centerX, zeroY),
                end = Offset(centerX, y),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )

            drawIntoCanvas {
                it.nativeCanvas.drawText(value.roundToInt().toString(), centerX, y - 8.dp.toPx(), labelPaint)
                it.nativeCanvas.drawText(
                    candidate.displayName.take(10),
                    centerX,
                    zeroY + 20.dp.toPx(),
                    labelPaint,
                )
                it.nativeCanvas.drawText(
                    "№${index + 1}",
                    centerX,
                    zeroY + 38.dp.toPx(),
                    labelPaint,
                )
            }
        }
    }
}

private fun Double.formatScore(): String = "${roundToInt()}"
