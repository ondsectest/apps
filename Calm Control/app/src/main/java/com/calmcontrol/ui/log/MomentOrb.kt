package com.calmcontrol.ui.log

import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = (2.0 * PI).toFloat()

/** Seconds for the light sweep to travel once around the orb. */
private const val SPIN_PERIOD = 11f

/** Seconds for one full breath, in and out. */
private const val BREATH_PERIOD = 5f

/** Seconds for a sparkle to go from dark, to bright, and back. */
private const val TWINKLE_PERIOD = 2.4f

/**
 * The tappable orb on the Log screen.
 *
 * Everything is driven from one clock that counts seconds and never resets. That matters more
 * than it sounds: an earlier version ran on looping 0→360 and 0→1 animations, and because each
 * sparkle orbits at a fractional multiple of the spin, the wrap from 360 back to 0 teleported
 * every sparkle at once, every eleven seconds. The individual loops were each smooth; what you
 * saw was the whole orb appearing to stop and start again. A monotonic clock has no seam to hit —
 * `sin` and `cos` are periodic, so positions stay continuous no matter how large the time grows.
 *
 * Four rhythms run off that clock at periods that do not divide into each other, so the
 * combination never visibly repeats: the breath, the sweep of light, the surface glitter, and a
 * second ring of glitter orbiting outside the body.
 */
@Composable
fun MomentOrb(
    label: String,
    baseColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Someone who has turned animations off system-wide has usually done it for a reason —
    // vestibular sensitivity, or an older device. Honour it: the clock stays at zero, and they
    // get a still orb that still carries glitter.
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }

    val elapsed = rememberElapsedSeconds(animationsEnabled)

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press",
    )

    // Seeded off the label so each orb gets its own arrangement, but the same one every launch.
    val surfaceSparkles = remember(label) {
        buildSparkles(label.hashCode(), count = 15, minRadius = 0.16f, maxRadius = 0.78f)
    }
    val orbitSparkles = remember(label) {
        buildSparkles(label.hashCode() * 31, count = 9, minRadius = 1.03f, maxRadius = 1.20f)
    }

    val highlight = lerp(baseColor, Color.White, 0.30f)
    val shade = lerp(baseColor, Color.Black, 0.14f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            // Read in the layer phase rather than during composition, so the breath does not
            // recompose this subtree sixty times a second.
            .graphicsLayer {
                val breath = 1.0125f + 0.0125f * sin(elapsed.value * TAU / BREATH_PERIOD)
                val scale = breath * pressScale
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 14.dp,
                shape = CircleShape,
                // Must not clip: the orbiting glitter lives outside the circle.
                clip = false,
                ambientColor = baseColor,
                spotColor = baseColor,
            )
            // drawWithCache, not drawBehind: the gradients and the star path depend only on the
            // size, so they are built once instead of being reallocated on every frame. The old
            // version churned about thirty objects per frame per orb, which is its own source of
            // stutter once the garbage collector notices.
            .drawWithCache {
                val radius = size.minDimension / 2f
                val centre = Offset(size.width / 2f, size.height / 2f)

                val auraBrush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.22f),
                        baseColor.copy(alpha = 0.07f),
                        Color.Transparent,
                    ),
                    center = centre,
                    radius = radius * 1.32f,
                )
                val bodyBrush = Brush.radialGradient(
                    colors = listOf(highlight, baseColor, shade),
                    center = Offset(size.width * 0.34f, size.height * 0.28f),
                    radius = radius * 1.6f,
                )
                val sweepBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.30f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.14f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.24f),
                        Color.Transparent,
                    ),
                    center = centre,
                )
                val specularBrush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(size.width * 0.34f, size.height * 0.26f),
                    radius = radius * 0.62f,
                )
                val rimBrush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color.White.copy(alpha = 0.17f),
                    ),
                    center = centre,
                    radius = radius,
                )
                // One path, rewound per sparkle, instead of twenty-four allocations a frame.
                val starPath = Path()

                onDrawBehind {
                    val seconds = elapsed.value

                    drawCircle(auraBrush, radius * 1.32f, centre)
                    drawCircle(bodyBrush, radius, centre)

                    rotate(degrees = seconds * 360f / SPIN_PERIOD, pivot = centre) {
                        drawCircle(sweepBrush, radius, centre)
                    }

                    drawCircle(specularBrush, radius, centre)
                    drawCircle(rimBrush, radius, centre)

                    surfaceSparkles.forEach { sparkle ->
                        drawSparkle(sparkle, centre, radius, seconds, Color.White, starPath)
                    }
                    orbitSparkles.forEach { sparkle ->
                        drawSparkle(sparkle, centre, radius, seconds, highlight, starPath)
                    }
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = label.replace('\n', ' '),
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            // SemiBold rather than the role's Medium, and a touch of extra tracking: this label
            // sits on a moving, glittering surface, and thin serifs are the first thing to get
            // lost against it.
            style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.2.sp),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color.White,
        )
    }
}

/**
 * Seconds since this orb first drew, as a continuously rising value.
 *
 * Backed by the infinite animation frame clock, so it pauses when nothing is visible and stays
 * under the control of tests, unlike a wall clock. Float precision stays comfortable — after a
 * full hour on screen the value is 3600, still resolved to well under a millisecond.
 */
@Composable
private fun rememberElapsedSeconds(enabled: Boolean): State<Float> =
    produceState(initialValue = 0f, enabled) {
        if (!enabled) return@produceState
        var startNanos = 0L
        while (true) {
            withInfiniteAnimationFrameNanos { frameNanos ->
                if (startNanos == 0L) startNanos = frameNanos
                value = (frameNanos - startNanos) / 1_000_000_000f
            }
        }
    }

/** One speck of glitter, with its own place, size, rhythm and orbital speed. */
private data class Sparkle(
    val angleDegrees: Float,
    val radiusFraction: Float,
    val sizeFraction: Float,
    val phase: Float,
    val drift: Float,
)

private fun buildSparkles(
    seed: Int,
    count: Int,
    minRadius: Float,
    maxRadius: Float,
): List<Sparkle> {
    val random = Random(seed)
    return List(count) {
        Sparkle(
            angleDegrees = random.nextFloat() * 360f,
            radiusFraction = minRadius + random.nextFloat() * (maxRadius - minRadius),
            sizeFraction = 0.055f + random.nextFloat() * 0.070f,
            phase = random.nextFloat(),
            // Fractional speeds are safe now that time never wraps; they are what stops the
            // glitter from moving as one rigid constellation.
            drift = 0.35f + random.nextFloat() * 1.1f,
        )
    }
}

/**
 * A four-point star with concave edges — the shape reads as a glint, where a plain dot would just
 * read as a dot. Each sparkle is dark for roughly half its cycle, which is what makes the set
 * shimmer rather than pulse.
 */
private fun DrawScope.drawSparkle(
    sparkle: Sparkle,
    centre: Offset,
    ballRadius: Float,
    seconds: Float,
    color: Color,
    path: Path,
) {
    val brightness = sin((seconds / TWINKLE_PERIOD + sparkle.phase) * TAU)
    if (brightness <= 0f) return

    val degrees = sparkle.angleDegrees + seconds * (360f / SPIN_PERIOD) * sparkle.drift
    val radians = degrees * TAU / 360f
    val distance = ballRadius * sparkle.radiusFraction
    val position = Offset(
        x = centre.x + cos(radians) * distance,
        y = centre.y + sin(radians) * distance,
    )
    val size = ballRadius * sparkle.sizeFraction * (0.55f + 0.45f * brightness)

    // Bloom first, then the glint on top of it. The bloom is kept faint on purpose: fifteen
    // overlapping white halos will happily turn a green orb grey, and the colour is carrying
    // meaning here.
    drawCircle(
        color = color.copy(alpha = 0.16f * brightness),
        radius = size * 1.7f,
        center = position,
    )

    path.rewind()
    path.moveTo(position.x, position.y - size)
    path.quadraticTo(position.x, position.y, position.x + size, position.y)
    path.quadraticTo(position.x, position.y, position.x, position.y + size)
    path.quadraticTo(position.x, position.y, position.x - size, position.y)
    path.quadraticTo(position.x, position.y, position.x, position.y - size)
    path.close()
    drawPath(path, color = color.copy(alpha = 0.92f * brightness))
}
