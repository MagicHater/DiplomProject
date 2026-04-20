package com.example.diplomproject.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class RadarMetric(
    val label: String,
    val value: Float,
)

@Composable
fun ProfileRadarChart(
    metrics: List<RadarMetric>,
    modifier: Modifier = Modifier,
    maxValue: Float = 10f,
) {
    if (metrics.size != 5) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Недостаточно данных для построения диаграммы",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        return
    }

    val gridColor = Color(0xFFBDBDBD)
    val axisColor = Color(0xFF9E9E9E)
    val profileStrokeColor = Color(0xFF1565C0)
    val profileFillColor = Color(0x661565C0)
    val labelColor = Color(0xFF424242)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp),
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val center = Offset(canvasWidth / 2f, canvasHeight / 2f + 10f)

        // Уменьшаем радиус, чтобы подписи не обрезались
        val radius = min(canvasWidth, canvasHeight) * 0.30f

        val levels = 10
        val angleStep = (2f * Math.PI / 5f).toFloat()
        val startAngle = (-Math.PI / 2.0).toFloat() // верхняя вершина

        fun pointAt(index: Int, scale: Float): Offset {
            val angle = startAngle + angleStep * index
            return Offset(
                x = center.x + cos(angle) * radius * scale,
                y = center.y + sin(angle) * radius * scale,
            )
        }

        fun buildPolygon(scale: Float): Path {
            val path = Path()
            val first = pointAt(0, scale)
            path.moveTo(first.x, first.y)
            for (i in 1 until 5) {
                val p = pointAt(i, scale)
                path.lineTo(p.x, p.y)
            }
            path.close()
            return path
        }

        // 1. Сетка: вложенные пятиугольники
        for (level in 1..levels) {
            val scale = level / levels.toFloat()
            drawPath(
                path = buildPolygon(scale),
                color = gridColor,
                style = Stroke(width = 2f),
            )
        }

        // 2. Оси
        for (i in 0 until 5) {
            val outer = pointAt(i, 1f)
            drawLine(
                color = axisColor,
                start = center,
                end = outer,
                strokeWidth = 1.5f,
            )
        }

        // 3. Цифры уровней вдоль верхней оси
        for (level in 0..levels) {
            val scale = level / levels.toFloat()
            val p = pointAt(0, scale)
            drawContext.canvas.nativeCanvas.drawText(
                level.toString(),
                p.x - 10f,
                p.y - 8f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }

        // 4. Подписи осей
        for (i in metrics.indices) {
            val p = pointAt(i, 1.14f)
            drawContext.canvas.nativeCanvas.drawText(
                metrics[i].label,
                p.x,
                p.y,
                android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }

        // 5. Профиль пользователя
        val normalized = metrics.map {
            (it.value / maxValue).coerceIn(0f, 1f)
        }

        val profilePath = Path()
        val firstPoint = pointAt(0, normalized[0])
        profilePath.moveTo(firstPoint.x, firstPoint.y)

        for (i in 1 until 5) {
            val p = pointAt(i, normalized[i])
            profilePath.lineTo(p.x, p.y)
        }
        profilePath.close()

        drawPath(
            path = profilePath,
            color = profileFillColor,
        )

        drawPath(
            path = profilePath,
            color = profileStrokeColor,
            style = Stroke(width = 4f),
        )

        // 6. Точки на вершинах профиля
        normalized.forEachIndexed { index, value ->
            val p = pointAt(index, value)
            drawCircle(
                color = profileStrokeColor,
                radius = 6f,
                center = p,
            )
        }
    }
}