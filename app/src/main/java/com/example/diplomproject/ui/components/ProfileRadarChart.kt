package com.example.diplomproject.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class RadarMetric(
    val label: String,
    val value: Float,
)

private const val ExpectedAxisCount = 5
private const val GridLevels = 4

@Composable
fun ProfileRadarChart(
    metrics: List<RadarMetric>,
    modifier: Modifier = Modifier,
    maxValue: Float = 1f,
) {
    val chartModifier = modifier
        .fillMaxWidth()
        .aspectRatio(1f)

    if (metrics.size != ExpectedAxisCount || maxValue <= 0f) {
        Box(modifier = chartModifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Недостаточно данных для построения диаграммы",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current

    Canvas(modifier = chartModifier) {
        val chartPadding = 36.dp.toPx()
        val axisLabelDistance = 16.dp.toPx()
        val centerPointRadius = 3.dp.toPx()
        val chartCenter = Offset(x = size.width / 2f, y = size.height / 2f)
        val availableRadius = (min(size.width, size.height) / 2f) - chartPadding
        if (availableRadius <= 0f) return@Canvas

        val outerPolygonPoints = polygonPoints(
            center = chartCenter,
            radius = availableRadius,
            pointsCount = ExpectedAxisCount,
        )

        repeat(GridLevels) { levelIndex ->
            val levelRatio = (levelIndex + 1) / GridLevels.toFloat()
            val gridPoints = polygonPoints(
                center = chartCenter,
                radius = availableRadius * levelRatio,
                pointsCount = ExpectedAxisCount,
            )
            drawPath(
                path = pointsToPath(gridPoints, close = true),
                color = colorScheme.outlineVariant,
                style = Stroke(width = if (levelIndex == GridLevels - 1) 1.4.dp.toPx() else 1.dp.toPx()),
            )
        }

        outerPolygonPoints.forEach { point ->
            drawLine(
                color = colorScheme.outlineVariant,
                start = chartCenter,
                end = point,
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val profilePoints = outerPolygonPoints.mapIndexed { index, point ->
            val ratio = (metrics[index].value / maxValue).coerceIn(0f, 1f)
            Offset(
                x = chartCenter.x + (point.x - chartCenter.x) * ratio,
                y = chartCenter.y + (point.y - chartCenter.y) * ratio,
            )
        }

        val profileFill = colorScheme.primary.copy(alpha = 0.22f)
        val profileStroke = colorScheme.primary

        drawPath(
            path = pointsToPath(profilePoints, close = true),
            color = profileFill,
        )
        drawPath(
            path = pointsToPath(profilePoints, close = true),
            color = profileStroke,
            style = Stroke(width = 2.dp.toPx()),
        )

        profilePoints.forEach { point ->
            drawCircle(
                color = profileStroke,
                radius = 3.dp.toPx(),
                center = point,
            )
        }

        drawCircle(
            color = colorScheme.primary,
            radius = centerPointRadius,
            center = chartCenter,
        )

        val labelPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            color = colorScheme.onSurfaceVariant.toArgb()
            textSize = with(density) { 12.sp.toPx() }
        }

        val maxLabelWidth = size.width * 0.32f
        val verticalTextOffset = 4.dp.toPx()
        outerPolygonPoints.forEachIndexed { index, point ->
            val label = ellipsizeLabel(
                text = metrics[index].label,
                paint = labelPaint,
                maxWidth = maxLabelWidth,
            )
            val labelWidth = labelPaint.measureText(label)

            val vectorX = point.x - chartCenter.x
            val vectorY = point.y - chartCenter.y
            val vectorLength = sqrt(vectorX * vectorX + vectorY * vectorY)
            val unitX = if (vectorLength == 0f) 0f else vectorX / vectorLength
            val unitY = if (vectorLength == 0f) 0f else vectorY / vectorLength
            val rawX = point.x + unitX * axisLabelDistance - labelWidth / 2f
            val rawY = point.y + unitY * axisLabelDistance + verticalTextOffset

            val safeX = rawX.coerceIn(0f, size.width - labelWidth)
            val safeY = rawY.coerceIn(labelPaint.textSize, size.height - 2.dp.toPx())

            drawContext.canvas.nativeCanvas.drawText(label, safeX, safeY, labelPaint)
        }
    }
}

private fun polygonPoints(
    center: Offset,
    radius: Float,
    pointsCount: Int,
): List<Offset> {
    val step = (2f * PI.toFloat()) / pointsCount
    val startAngle = -PI.toFloat() / 2f
    return List(pointsCount) { index ->
        val angle = startAngle + step * index
        Offset(
            x = center.x + cos(angle) * radius,
            y = center.y + sin(angle) * radius,
        )
    }
}

private fun pointsToPath(points: List<Offset>, close: Boolean): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    points.drop(1).forEach { point -> path.lineTo(point.x, point.y) }
    if (close) path.close()
    return path
}


private fun ellipsizeLabel(
    text: String,
    paint: Paint,
    maxWidth: Float,
): String {
    if (paint.measureText(text) <= maxWidth) return text

    val ellipsis = "…"
    val ellipsisWidth = paint.measureText(ellipsis)
    var endIndex = text.length
    while (endIndex > 0 && paint.measureText(text, 0, endIndex) + ellipsisWidth > maxWidth) {
        endIndex--
    }

    if (endIndex <= 0) return ellipsis
    return text.substring(0, endIndex).trimEnd() + ellipsis
}
