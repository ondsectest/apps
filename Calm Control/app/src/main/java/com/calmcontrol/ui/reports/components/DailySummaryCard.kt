package com.calmcontrol.ui.reports.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calmcontrol.domain.DailySummary
import com.calmcontrol.ui.theme.calmColors
import com.calmcontrol.ui.theme.tabularFigures

/**
 * Today at a glance: the ring on the left, the counts on the right.
 *
 * The percentage in the middle of the ring is the self-control rate, never the anger rate. Which
 * number sits in the centre is the single most important decision on this screen.
 */
@Composable
fun DailySummaryCard(summary: DailySummary, modifier: Modifier = Modifier) {
    SectionCard(
        title = "Today",
        subtitle = if (summary.hasData) "How the day has gone so far" else null,
        modifier = modifier,
    ) {
        if (!summary.hasData) {
            Text(
                text = "No moments logged yet today. A quiet day counts too.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            ControlRing(
                fraction = summary.controlledFraction,
                percent = summary.selfControlRate,
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Labels stay single-word because this card has to survive a 320dp-wide screen
                // next to the ring. The section title already supplies "Today".
                StatRow(
                    label = "Triggers",
                    value = summary.total.toString(),
                    dotColor = MaterialTheme.colorScheme.outline,
                )
                StatRow(
                    label = "Controlled",
                    value = summary.controlledCount.toString(),
                    dotColor = calmColors.controlled,
                )
                StatRow(
                    label = "Expressed",
                    value = summary.angerCount.toString(),
                    dotColor = calmColors.anger,
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            Modifier
                .size(8.dp)
                .background(dotColor, CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.tabularFigures(),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Circular progress. Green sweeps the controlled share, terracotta closes the circle.
 *
 * The centre text is real [Text], not canvas-drawn, so it inherits typography and respects the
 * user's font-size setting.
 */
@Composable
fun ControlRing(
    fraction: Float,
    percent: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 124.dp,
    strokeWidth: Dp = 14.dp,
) {
    val colors = calmColors
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ringSweep",
    )

    Box(
        modifier = modifier
            .size(ringSize)
            .semantics { contentDescription = "$percent percent of today's triggers controlled" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(ringSize)) {
            val stroke = strokeWidth.toPx()
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val style = Stroke(width = stroke, cap = StrokeCap.Round)

            drawArc(
                color = colors.chartTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = style,
            )

            val greenSweep = 360f * animatedFraction
            val redSweep = 360f - greenSweep
            // Only carve a gap when both segments actually exist, otherwise a 100% ring would
            // show a notch for no reason.
            val gap = if (greenSweep > 2f && redSweep > 2f) 8f else 0f

            if (redSweep > 1f) {
                drawArc(
                    color = colors.anger,
                    startAngle = -90f + greenSweep + gap / 2f,
                    sweepAngle = (redSweep - gap).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style,
                )
            }
            if (greenSweep > 1f) {
                drawArc(
                    color = colors.controlled,
                    startAngle = -90f + gap / 2f,
                    sweepAngle = (greenSweep - gap).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                // Tabular figures. This is centred inside the ring, and with proportional digits
                // "11%" and "44%" set to different widths — so the number would visibly shift
                // sideways each time a logged moment changes the rate.
                style = MaterialTheme.typography.headlineMedium.tabularFigures(),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Controlled",
                style = MaterialTheme.typography.labelMedium,
                color = colors.controlled,
            )
        }
    }
}
