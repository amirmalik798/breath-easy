package com.amirmalik.breatheasy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.domain.MonthlySpendPoint
import com.amirmalik.breatheasy.domain.MonthlyConsumptionPoint
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

@Composable
fun MonthlySpendingChart(
    points: List<MonthlySpendPoint>,
    currency: CurrencyCode,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val values = points.map { it.totalMinor.toFloat() }
    val maxValue = max(values.maxOrNull() ?: 0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp),
        ) {
            val left = 8.dp.toPx()
            val right = size.width - 8.dp.toPx()
            val top = 12.dp.toPx()
            val bottom = size.height - 12.dp.toPx()
            val chartHeight = bottom - top
            val chartWidth = right - left

            repeat(4) { index ->
                val y = top + chartHeight * index / 3f
                drawLine(grid, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
            }

            if (values.isEmpty()) return@Canvas
            val slot = chartWidth / values.size
            val barWidth = (slot * 0.42f).coerceAtMost(28.dp.toPx())
            values.forEachIndexed { index, value ->
                if (value > 0f) {
                    val fraction = (value / maxValue).coerceIn(0f, 1f)
                    val height = chartHeight * fraction
                    val x = left + slot * index + (slot - barWidth) / 2f
                    val y = bottom - height
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = if (index == values.lastIndex) {
                                listOf(primary, primary.copy(alpha = 0.58f))
                            } else {
                                listOf(primary.copy(alpha = 0.76f), primary.copy(alpha = 0.28f))
                            },
                            startY = y,
                            endY = bottom,
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx()),
                    )
                }
            }

            // A subtle trend line keeps comparisons legible when bars are close in height.
            if (values.size > 1 && values.any { it > 0f }) {
                val path = Path()
                values.forEachIndexed { index, value ->
                    val x = left + slot * index + slot / 2f
                    val y = bottom - (chartHeight * (value / maxValue).coerceIn(0f, 1f))
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = primary,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
                values.forEachIndexed { index, value ->
                    val x = left + slot * index + slot / 2f
                    val y = bottom - (chartHeight * (value / maxValue).coerceIn(0f, 1f))
                    drawCircle(primary, radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            points.forEach { point ->
                Text(
                    text = point.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    color = muted,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
    }
}


@Composable
fun MonthlyConsumptionChart(
    points: List<MonthlyConsumptionPoint>,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.secondary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val values = points.map { it.cigaretteCount.toFloat() }
    val maxValue = max(values.maxOrNull() ?: 0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val left = 8.dp.toPx()
            val right = size.width - 8.dp.toPx()
            val top = 10.dp.toPx()
            val bottom = size.height - 10.dp.toPx()
            val chartHeight = bottom - top
            val chartWidth = right - left
            repeat(4) { index ->
                val y = top + chartHeight * index / 3f
                drawLine(grid, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
            }
            if (values.isEmpty()) return@Canvas
            val slot = chartWidth / values.size
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = left + slot * index + slot / 2f
                val y = bottom - chartHeight * (value / maxValue).coerceIn(0f, 1f)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val area = Path().apply {
                addPath(path)
                lineTo(left + slot * (values.lastIndex) + slot / 2f, bottom)
                lineTo(left + slot / 2f, bottom)
                close()
            }
            drawPath(
                area,
                Brush.verticalGradient(
                    colors = listOf(primary.copy(alpha = 0.30f), primary.copy(alpha = 0.015f)),
                    startY = top,
                    endY = bottom,
                ),
            )
            drawPath(path, primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            values.forEachIndexed { index, value ->
                val x = left + slot * index + slot / 2f
                val y = bottom - chartHeight * (value / maxValue).coerceIn(0f, 1f)
                drawCircle(primary.copy(alpha = 0.18f), radius = 7.dp.toPx(), center = Offset(x, y))
                drawCircle(primary, radius = 4.dp.toPx(), center = Offset(x, y))
                drawCircle(surface, radius = 1.8.dp.toPx(), center = Offset(x, y))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            points.forEach { point ->
                Text(
                    text = point.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    color = muted,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun ProgressArc(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 12.dp,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val track = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2f, stroke / 2f)
        drawArc(
            color = track,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    primary,
                    secondary,
                    tertiary,
                    primary,
                ),
            ),
            startAngle = 135f,
            sweepAngle = 270f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}
