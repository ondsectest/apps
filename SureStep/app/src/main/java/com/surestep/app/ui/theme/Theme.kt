package com.surestep.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.surestep.app.data.prefs.ThemeMode

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

private val SureStepShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun SureStepTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val baseScheme = if (dark) DarkColors else LightColors
    val scheme = if (highContrast) baseScheme.toHighContrast(dark) else baseScheme
    val statusColors = if (dark) DarkStatusColors else LightStatusColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = SureStepTypography,
            shapes = SureStepShapes,
            content = content,
        )
    }
}

/**
 * Pushes text and outlines to the ends of the luminance range and flattens
 * surface tints, so foreground/background pairs clear WCAG AA comfortably.
 */
private fun androidx.compose.material3.ColorScheme.toHighContrast(dark: Boolean) = copy(
    background = if (dark) Color.Black else Color.White,
    surface = if (dark) Color.Black else Color.White,
    onBackground = if (dark) Color.White else Color.Black,
    onSurface = if (dark) Color.White else Color.Black,
    surfaceVariant = if (dark) Color(0xFF1B1B1B) else Color(0xFFEDEDED),
    onSurfaceVariant = if (dark) Color(0xFFEDEDED) else Color(0xFF1B1B1B),
    outline = if (dark) Color(0xFFCCCCCC) else Color(0xFF333333),
    outlineVariant = if (dark) Color(0xFF666666) else Color(0xFF999999),
    primary = if (dark) Color(0xFFBFE0F5) else Color(0xFF0B4A6B),
    onPrimary = if (dark) Color.Black else Color.White,
)

/** Readable text colour for an arbitrary task accent colour. */
fun onAccentColor(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF12181C) else Color.White
