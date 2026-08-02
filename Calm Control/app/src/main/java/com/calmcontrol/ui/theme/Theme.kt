package com.calmcontrol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Chart colours carry meaning, so they live outside the Material scheme where nothing can
 * repurpose them. No chart component hardcodes a hex; they all read from here.
 */
@Immutable
data class CalmColors(
    val controlled: Color,
    val controlledSoft: Color,
    val controlledContainer: Color,
    val anger: Color,
    val angerSoft: Color,
    val angerContainer: Color,
    val chartTrack: Color,
)

private val LightCalmColors = CalmColors(
    controlled = ControlledLight,
    controlledSoft = ControlledSoftLight,
    controlledContainer = ControlledContainerLight,
    anger = AngerLight,
    angerSoft = AngerSoftLight,
    angerContainer = AngerContainerLight,
    chartTrack = ChartTrackLight,
)

private val DarkCalmColors = CalmColors(
    controlled = ControlledDark,
    controlledSoft = ControlledSoftDark,
    controlledContainer = ControlledContainerDark,
    anger = AngerDark,
    angerSoft = AngerSoftDark,
    angerContainer = AngerContainerDark,
    chartTrack = ChartTrackDark,
)

val LocalCalmColors = staticCompositionLocalOf { LightCalmColors }

private val LightScheme = lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = GreenContainerLight,
    onPrimaryContainer = OnGreenContainerLight,
    // The secondary and tertiary families are overridden rather than left to default. Material's
    // baseline scheme is lavender, and components that reach for it — the segmented button's
    // selected state, for one — would otherwise import a colour the palette never chose.
    secondary = GreenPrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = GreenContainerLight,
    onSecondaryContainer = OnGreenContainerLight,
    tertiary = GreenPrimaryLight,
    onTertiary = Color.White,
    tertiaryContainer = GreenContainerLight,
    onTertiaryContainer = OnGreenContainerLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = AngerLight,
    onError = Color.White,
    errorContainer = AngerContainerLight,
    onErrorContainer = Color(0xFF4A211B),
)

private val DarkScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = OnGreenContainerLight,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = OnGreenContainerDark,
    secondary = GreenPrimaryDark,
    onSecondary = OnGreenContainerLight,
    secondaryContainer = GreenContainerDark,
    onSecondaryContainer = OnGreenContainerDark,
    tertiary = GreenPrimaryDark,
    onTertiary = OnGreenContainerLight,
    tertiaryContainer = GreenContainerDark,
    onTertiaryContainer = OnGreenContainerDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = AngerDark,
    onError = Color(0xFF4A211B),
    errorContainer = AngerContainerDark,
    onErrorContainer = Color(0xFFF6E3DE),
)

/**
 * Dynamic colour is deliberately not used. Wallpaper-derived theming would hand the app whatever
 * accent the user's home screen happens to have, and green-means-controlled is the whole point.
 */
@Composable
fun CalmControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val calmColors = if (darkTheme) DarkCalmColors else LightCalmColors
    CompositionLocalProvider(LocalCalmColors provides calmColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = CalmTypography,
            content = content,
        )
    }
}

/** Shorthand for reading chart colours inside composables. */
val calmColors: CalmColors
    @Composable get() = LocalCalmColors.current
