package com.calmcontrol.ui.log

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmcontrol.data.local.Outcome
import com.calmcontrol.domain.Quote
import com.calmcontrol.ui.theme.calmColors

/** The logged moment a quote is being shown for. */
data class QuoteMoment(val outcome: Outcome, val quote: Quote)

/**
 * Shown after every logged moment, whichever orb was tapped.
 *
 * The headline is the part that has to be right. After "Got angry" this is the app's first
 * response to someone telling the truth about a bad moment, and anything that reads as a
 * reprimand teaches them to stop logging the red ones — which would quietly wreck every chart in
 * the app. So both headlines are affirming, and the angry one is the warmer of the two.
 */
@Composable
fun QuoteDialog(
    moment: QuoteMoment,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = calmColors
    val accent = when (moment.outcome) {
        Outcome.CONTROLLED -> colors.controlled
        Outcome.ANGER_EXPRESSED -> colors.anger
    }

    // The quote fades up a beat after the dialog lands, so the eye reaches the headline first
    // rather than meeting a wall of text.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(moment) { revealed = true }
    val quoteAlpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 550, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "quoteFade",
    )

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = when (moment.outcome) {
                        Outcome.CONTROLLED -> "That one counts"
                        Outcome.ANGER_EXPRESSED -> "Thank you for being honest"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (moment.outcome) {
                        Outcome.CONTROLLED -> "You felt it and chose your response."
                        Outcome.ANGER_EXPRESSED -> "Noticing the moment is where control begins."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(quoteAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "“",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 56.sp),
                    color = accent,
                )
                Text(
                    text = moment.quote.text,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = moment.quote.author,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
                moment.quote.source?.let { source ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = source,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue")
            }
        },
    )
}
