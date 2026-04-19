package com.example.diplomproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.diplomproject.domain.model.ScaleInterpretations
import com.example.diplomproject.domain.model.ScaleScores
import com.example.diplomproject.ui.components.RadarMetric

data class ScaleItemUi(
    val title: String,
    val value: Double,
    val interpretation: String,
)

fun scaleItems(scores: ScaleScores, interpretations: ScaleInterpretations): List<ScaleItemUi> = listOf(
    ScaleItemUi("Внимание", scores.attention, interpretations.attention),
    ScaleItemUi("Стрессоустойчивость", scores.stressResistance, interpretations.stressResistance),
    ScaleItemUi("Ответственность", scores.responsibility, interpretations.responsibility),
    ScaleItemUi("Адаптивность", scores.adaptability, interpretations.adaptability),
    ScaleItemUi("Скорость/точность решений", scores.decisionSpeedAccuracy, interpretations.decisionSpeedAccuracy),
)


fun radarMetrics(scores: ScaleScores): List<RadarMetric> = listOf(
    RadarMetric(label = "Внимание", value = scores.attention.toFloat()),
    RadarMetric(label = "Стрессоустойчивость", value = scores.stressResistance.toFloat()),
    RadarMetric(label = "Ответственность", value = scores.responsibility.toFloat()),
    RadarMetric(label = "Адаптивность", value = scores.adaptability.toFloat()),
    RadarMetric(label = "Скорость/точность решений", value = scores.decisionSpeedAccuracy.toFloat()),
)

fun compactScoreItems(scores: ScaleScores): List<Pair<String, Double>> = listOf(
    "Внимание" to scores.attention,
    "Стрессоуст." to scores.stressResistance,
    "Ответств." to scores.responsibility,
    "Адаптивн." to scores.adaptability,
    "Скорость/точн." to scores.decisionSpeedAccuracy,
)

@Composable
fun CompactScoreRow(
    title: String,
    value: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
fun ScaleItemCard(
    item: ScaleItemUi,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = String.format("%.2f", item.value),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                text = item.interpretation,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
