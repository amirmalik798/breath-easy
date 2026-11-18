package com.amirmalik.breatheasy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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
    val latestBar = MaterialTheme.colorScheme.secondary
    val grid = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val values = points.map { it.totalMinor.coerceAtLeast(0L) }
    val maxValue = values.maxOrNull() ?: 0L
    val hasSpending = maxValue > 0L
    val chartHeight = 184.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        "Monthly spending",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Amounts in \${currency.isoCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        "12 months",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            if (!hasSpending) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
                        .padding(horizontal = 18.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Box(
                        modifier = Modifier.size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                    Text(
                        "No spending data yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Record a \${currency.isoCode} purchase to start building your monthly trend.",
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                val axisLabels = (4 downTo 0).map { step ->
                    (maxValue.toDouble() * step / 4.0).toLong()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.width(54.dp)
                            .height(chartHeight)
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End,
                    ) {
                        axisLabels.forEach { amount ->
                            Text(
                                text = compactAxisAmount(amount, currency),
                                style = MaterialTheme.typography.labelSmall,
                                color = muted,
                                maxLines = 1,
                            )
                        }
                    }

                    Canvas(
                        modifier = Modifier.weight(1f).height(chartHeight),
                    ) {
                        val left = 4.dp.toPx()
                        val right = size.width - 4.dp.toPx()
                        val top = 8.dp.toPx()
                        val bottom = size.height - 8.dp.toPx()
                        val plotHeight = bottom - top
                        val plotWidth = right - left
                        val divisor = maxValue.toDouble().coerceAtLeast(1.0)

                        repeat(5) { index ->
                            val y = top + plotHeight * index / 4f
                            drawLine(
                                color = grid,
                                start = Offset(left, y),
                                end = Offset(right, y),
                                strokeWidth = if (index == 4) 1.5.dp.toPx() else 1.dp.toPx(),
                            )
                        }

                        if (values.isNotEmpty()) {
                            val slot = plotWidth / values.size
                            val barWidth = (slot * 0.58f).coerceAtMost(24.dp.toPx())
                            values.forEachIndexed { index, value ->
                                if (value > 0L) {
                                    val fraction = (value.toDouble() / divisor).toFloat()
                                        .coerceIn(0f, 1f)
                                    val barHeight = (plotHeight * fraction).coerceAtLeast(3.dp.toPx())
                                    val x = left + slot * index + (slot - barWidth) / 2f
                                    val y = bottom - barHeight
                                    val color = if (index == values.lastIndex) latestBar else primary
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                color,
                                                color.copy(alpha = 0.62f),
                                            ),
                                            startY = y,
                                            endY = bottom,
                                        ),
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.width(54.dp))
                    points.forEachIndexed { index, point ->
                        Text(
                            text = point.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                .take(3),
                            color = if (index == points.lastIndex) primary else muted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (index == points.lastIndex) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(9.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(primary),
                    )
                    Text(
                        "Monthly total",
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Latest month highlighted",
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                    )
                }
            }
        }
    }
}

private fun compactAxisAmount(minorAmount: Long, currency: CurrencyCode): String {
    val majorAmount = java.math.BigDecimal.valueOf(minorAmount, currency.fractionDigits)
    val absoluteAmount = majorAmount.abs()
    val divisor: Long
    val suffix: String

    when {
        absoluteAmount >= java.math.BigDecimal("1000000") -> {
            divisor = 1_000_000L
            suffix = "M"
        }
        absoluteAmount >= java.math.BigDecimal("1000") -> {
            divisor = 1_000L
            suffix = "k"
        }
        else -> {
            divisor = 1L
            suffix = ""
        }
    }

    val scaled = majorAmount.divide(
        java.math.BigDecimal.valueOf(divisor),
        1,
        java.math.RoundingMode.HALF_UP,
    )
    val formatter = java.text.NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }
    return formatter.format(scaled) + suffix
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
