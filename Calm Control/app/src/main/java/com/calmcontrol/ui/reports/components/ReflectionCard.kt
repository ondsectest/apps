package com.calmcontrol.ui.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmcontrol.ui.theme.calmColors

/**
 * The month in plain sentences.
 *
 * Deliberately last on the screen: after the charts have shown the shape of things, these lines
 * say what it means. Nothing here is phrased as a shortfall.
 */
@Composable
fun ReflectionCard(lines: List<String>, modifier: Modifier = Modifier) {
    SectionCard(
        title = "This month",
        subtitle = "A short reflection",
        modifier = modifier,
    ) {
        if (lines.isEmpty()) {
            Text(
                text = "Your first month of reflections is on its way.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }

        lines.forEachIndexed { index, line ->
            Row(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .size(6.dp)
                        .background(calmColors.controlled, CircleShape),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (index != lines.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}
