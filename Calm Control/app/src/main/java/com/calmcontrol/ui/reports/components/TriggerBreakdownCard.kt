package com.calmcontrol.ui.reports.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calmcontrol.domain.StrongestArea
import com.calmcontrol.domain.TriggerShare
import com.calmcontrol.ui.theme.calmColors
import com.calmcontrol.ui.theme.tabularFigures

/**
 * What sets you off, and how each one tends to go.
 *
 * Each bar's length is how often the trigger came up; the green portion inside it is how much of
 * that ended calmly. So the row says "this happens a lot" and "you're handling it" in one shape,
 * without a second chart.
 */
@Composable
fun TriggerBreakdownCard(
    triggers: List<TriggerShare>,
    strongestArea: StrongestArea?,
    modifier: Modifier = Modifier,
) {
    val colors = calmColors

    SectionCard(
        title = "What triggers you",
        subtitle = "Last 30 days",
        modifier = modifier,
    ) {
        if (triggers.isEmpty()) {
            Text(
                text = "Log a few moments and your patterns will start to show.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }

        // Bars are scaled against the largest category rather than against 100%. With seven
        // categories no single share gets near full width, so absolute scaling leaves every bar
        // stunted and the comparison — which is the point of this chart — hard to read. The
        // printed percentage keeps the real magnitude honest.
        val maxShare = triggers.maxOf { it.shareFraction }.coerceAtLeast(0.01f)

        triggers.forEach { share ->
            key(share.category) {
                TriggerRow(share, maxShare)
                Spacer(Modifier.height(14.dp))
            }
        }

        Spacer(Modifier.height(2.dp))
        ChartLegend(
            controlledLabel = "Controlled",
            angerLabel = "Expressed",
            controlledColor = colors.controlled,
            angerColor = colors.anger,
        )

        if (strongestArea != null) {
            Spacer(Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.controlledContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = buildStrongestAreaText(strongestArea),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun buildStrongestAreaText(area: StrongestArea): String =
    "Your strongest improvement area: you controlled ${area.category.phrase} " +
        "frustration ${area.controlledRate}% of the time."

@Composable
private fun TriggerRow(share: TriggerShare, maxShare: Float) {
    val colors = calmColors
    val animatedShare by animateFloatAsState(
        targetValue = (share.shareFraction / maxShare).coerceIn(0f, 1f),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "triggerShare",
    )
    val animatedControlled by animateFloatAsState(
        targetValue = (share.controlledRate / 100f).coerceIn(0f, 1f),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "triggerControlled",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = share.category.label,
            modifier = Modifier.width(64.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .background(colors.chartTrack, CircleShape),
        ) {
            // Outer segment is the trigger's share of the month; the green overlay is the part
            // of it that ended calmly. Zero-width segments are skipped rather than drawn at
            // fraction 0, which keeps the rounded shapes from rendering as slivers.
            if (animatedShare > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedShare)
                        .fillMaxHeight()
                        .background(colors.anger, CircleShape),
                ) {
                    if (animatedControlled > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedControlled)
                                .fillMaxHeight()
                                .background(colors.controlled, CircleShape),
                        )
                    }
                }
            }
        }

        Text(
            text = "${share.sharePercent}%",
            // Wide enough for "100%" in Lora at this size — 40dp fitted Roboto and wrapped once
            // the type scale moved.
            modifier = Modifier.width(52.dp),
            maxLines = 1,
            textAlign = TextAlign.End,
            // A right-aligned column of percentages: tabular digits keep them stacked.
            style = MaterialTheme.typography.labelLarge.tabularFigures(),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
