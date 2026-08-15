package com.calmcontrol.ui.reports.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calmcontrol.domain.DayBucket
import com.calmcontrol.ui.theme.calmColors
import kotlin.math.max

/**
 * Seven days, two bars each. The point of this chart is one glance: are the green bars growing?
 *
 * Per-day numbers are revealed by tapping rather than printed above every bar. Fourteen permanent
 * value labels would turn a calm chart into a spreadsheet.
 */
@Composable
fun WeeklyBarChart(buckets: List<DayBucket>, modifier: Modifier = Modifier) {
    val colors = calmColors
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    SectionCard(
        title = "This week",
        subtitle = "Monday to Sunday",
        modifier = modifier,
    ) {
        if (buckets.isEmpty() || buckets.all { it.total == 0 }) {
            Text(
                text = "This week's picture starts with your first logged moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }

        val selected = selectedIndex?.let(buckets::getOrNull)
        Text(
            text = selected?.let {
                "${it.label} · ${it.controlledCount} controlled · ${it.angerCount} expressed"
            } ?: "Tap a day for its numbers",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected != null) FontWeight.Medium else FontWeight.Normal,
            color = if (selected != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(Modifier.height(14.dp))

        // One animated value per bar. The list is always seven long, so composing animations in a
        // loop is stable across recompositions.
        val maxValue = max(1, buckets.maxOf { max(it.controlledCount, it.angerCount) })
        val animatedControlled = buckets.map { bucket ->
            animateFloatAsState(
                targetValue = bucket.controlledCount.toFloat() / maxValue,
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                label = "controlledBar",
            ).value
        }
        val animatedAnger = buckets.map { bucket ->
            animateFloatAsState(
                targetValue = bucket.angerCount.toFloat() / maxValue,
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                label = "angerBar",
            ).value
        }

        val highlight = MaterialTheme.colorScheme.surfaceVariant
        val baseline = MaterialTheme.colorScheme.outlineVariant

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .pointerInput(buckets.size) {
                    detectTapGestures { offset ->
                        val slot = size.width / buckets.size.toFloat()
                        val index = (offset.x / slot).toInt().coerceIn(0, buckets.lastIndex)
                        selectedIndex = if (selectedIndex == index) null else index
                    }
                },
        ) {
            val slotWidth = size.width / buckets.size
            val barWidth = (slotWidth * 0.26f).coerceAtMost(18.dp.toPx())
            val innerGap = 6.dp.toPx()
            val plotHeight = size.height - 6.dp.toPx()
            val radius = CornerRadius(barWidth / 2f, barWidth / 2f)

            buckets.indices.forEach { index ->
                val slotStart = slotWidth * index
                val centre = slotStart + slotWidth / 2f

                if (selectedIndex == index) {
                    drawRoundRect(
                        color = highlight,
                        topLeft = Offset(slotStart + slotWidth * 0.06f, 0f),
                        size = Size(slotWidth * 0.88f, size.height),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    )
                }

                drawBar(
                    centreX = centre - (barWidth + innerGap) / 2f,
                    fraction = animatedControlled[index],
                    color = colors.controlled,
                    barWidth = barWidth,
                    plotHeight = plotHeight,
                    radius = radius,
                    emptyColor = colors.chartTrack,
                )
                drawBar(
                    centreX = centre + (barWidth + innerGap) / 2f,
                    fraction = animatedAnger[index],
                    color = colors.anger,
                    barWidth = barWidth,
                    plotHeight = plotHeight,
                    radius = radius,
                    emptyColor = colors.chartTrack,
                )
            }

            drawLine(
                color = baseline,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx(),
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            buckets.forEachIndexed { index, bucket ->
                Text(
                    text = bucket.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                    color = if (bucket.isInFuture) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        ChartLegend(
            controlledLabel = "Controlled",
            angerLabel = "Expressed",
            controlledColor = colors.controlled,
            angerColor = colors.anger,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/**
 * A day with no events still gets a faint stub, so an empty column reads as "nothing happened"
 * rather than as a rendering gap.
 */
private fun DrawScope.drawBar(
    centreX: Float,
    fraction: Float,
    color: Color,
    barWidth: Float,
    plotHeight: Float,
    radius: CornerRadius,
    emptyColor: Color,
) {
    val stub = barWidth
    val height = (plotHeight * fraction).coerceAtLeast(0f)
    val drawnHeight = max(height, stub)
    val left = centreX - barWidth / 2f
    drawRoundRect(
        color = if (height < stub / 2f) emptyColor else color,
        topLeft = Offset(left, size.height - drawnHeight),
        size = Size(barWidth, drawnHeight),
        cornerRadius = radius,
    )
}
