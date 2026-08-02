package com.calmcontrol.ui.reports.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.calmcontrol.domain.TrendPoint
import com.calmcontrol.domain.TrendRange
import com.calmcontrol.ui.theme.calmColors
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private val axisFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

/**
 * The long view. Two lines, and the green one is the one that should be climbing.
 *
 * Only the green line gets a gradient fill. Weighting both equally would turn the chart into a
 * contest; filling just the growth line is what makes progress the thing your eye lands on.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendLineChart(
    points: List<TrendPoint>,
    range: TrendRange,
    onRangeChange: (TrendRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = calmColors

    SectionCard(
        title = "Your trend",
        subtitle = "7-day average, so the line shows direction rather than noise",
        modifier = modifier,
    ) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            TrendRange.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == range,
                    onClick = { onRangeChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, TrendRange.entries.size),
                ) {
                    Text(option.label)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Two points minimum: a line needs a segment, and a single logged day would otherwise
        // render as an empty panel with axis dates rather than as an honest empty state.
        if (points.size < 2 || points.all { it.controlledAvg == 0f && it.angerAvg == 0f }) {
            Text(
                text = "Once you've logged a few days, your trend will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }

        // The draw-on reveal is keyed to the range toggle only. Replaying it on every data change
        // would make the chart flicker each time an event is logged.
        val reveal = remember { Animatable(0f) }
        LaunchedEffect(range) {
            reveal.snapTo(0f)
            reveal.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }

        val rawMax = points.maxOf { max(it.controlledAvg, it.angerAvg) }
        val animatedMax by animateFloatAsState(
            targetValue = max(rawMax * 1.15f, 0.5f),
            animationSpec = tween(600, easing = FastOutSlowInEasing),
            label = "trendScale",
        )

        val gridColor = MaterialTheme.colorScheme.outlineVariant
        val pathMeasure = remember { PathMeasure() }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            val yMax = if (animatedMax <= 0f) 1f else animatedMax
            val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f

            fun offsetsFor(selector: (TrendPoint) -> Float): List<Offset> =
                points.mapIndexed { index, point ->
                    val y = size.height - (selector(point) / yMax).coerceIn(0f, 1f) * size.height
                    Offset(index * stepX, y)
                }

            listOf(0.5f, 1f).forEach { at ->
                val y = size.height - size.height * at
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val controlledPath = smoothPath(offsetsFor { it.controlledAvg })
            val angerPath = smoothPath(offsetsFor { it.angerAvg })

            // The fill fades in with the reveal rather than being clipped to it — matching the
            // fill exactly to a partially drawn path costs a lot of geometry for no visible gain.
            val fillPath = Path().apply {
                addPath(controlledPath)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.controlled.copy(alpha = 0.24f * reveal.value),
                        Color.Transparent,
                    ),
                ),
            )

            drawRevealed(angerPath, colors.anger, reveal.value, pathMeasure, 2.5f.dp.toPx())
            drawRevealed(controlledPath, colors.controlled, reveal.value, pathMeasure, 3.dp.toPx())
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = points.first().date.format(axisFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = points.last().date.format(axisFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(16.dp))
        ChartLegend(
            controlledLabel = "Controlled",
            angerLabel = "Expressed",
            controlledColor = colors.controlled,
            angerColor = colors.anger,
        )
    }
}

/**
 * Cubic smoothing with control points at the horizontal midpoint of each segment. Cheap, stable,
 * and it cannot overshoot horizontally the way a naive tension spline can.
 */
private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (index in 0 until points.lastIndex) {
        val current = points[index]
        val next = points[index + 1]
        val midX = (current.x + next.x) / 2f
        path.cubicTo(midX, current.y, midX, next.y, next.x, next.y)
    }
    return path
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRevealed(
    path: Path,
    color: Color,
    progress: Float,
    measure: PathMeasure,
    strokeWidth: Float,
) {
    if (progress <= 0f) return
    val segment = Path()
    measure.setPath(path, false)
    measure.getSegment(0f, measure.length * progress, segment, true)
    drawPath(
        path = segment,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
}
