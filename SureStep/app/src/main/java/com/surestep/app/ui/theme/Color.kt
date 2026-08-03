package com.surestep.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// A deliberately quiet palette: slate blue as the primary, sage green for
// "recorded", warm amber and muted clay for partial/missed. Nothing shouts.

private val SlateBlue40 = Color(0xFF2A6F97)
private val SlateBlue80 = Color(0xFF9FCCE8)
private val SlateBlue10 = Color(0xFF001E2F)
private val SlateBlue30 = Color(0xFF14577C)
private val SlateBlue90 = Color(0xFFCDE5F6)

private val Sage40 = Color(0xFF3F6B54)
private val Sage80 = Color(0xFFA6D2B8)
private val Sage10 = Color(0xFF002115)
private val Sage90 = Color(0xFFC1EFD3)

private val Mist40 = Color(0xFF52606B)
private val Mist80 = Color(0xFFB9C6D2)
private val Mist90 = Color(0xFFD5E2EE)
private val Mist10 = Color(0xFF0E1D26)

val LightColors = lightColorScheme(
    primary = SlateBlue40,
    onPrimary = Color.White,
    primaryContainer = SlateBlue90,
    onPrimaryContainer = SlateBlue10,
    secondary = Sage40,
    onSecondary = Color.White,
    secondaryContainer = Sage90,
    onSecondaryContainer = Sage10,
    tertiary = Mist40,
    onTertiary = Color.White,
    tertiaryContainer = Mist90,
    onTertiaryContainer = Mist10,
    background = Color(0xFFFBFCFE),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFBFCFE),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
    error = Color(0xFFA33A31),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF410002),
)

val DarkColors = darkColorScheme(
    primary = SlateBlue80,
    onPrimary = SlateBlue10,
    primaryContainer = SlateBlue30,
    onPrimaryContainer = SlateBlue90,
    secondary = Sage80,
    onSecondary = Sage10,
    secondaryContainer = Color(0xFF275343),
    onSecondaryContainer = Sage90,
    tertiary = Mist80,
    onTertiary = Mist10,
    tertiaryContainer = Color(0xFF3A4853),
    onTertiaryContainer = Mist90,
    background = Color(0xFF101418),
    onBackground = Color(0xFFE1E3E5),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF41484D),
    onSurfaceVariant = Color(0xFFC1C7CE),
    outline = Color(0xFF8B9198),
    outlineVariant = Color(0xFF41484D),
    error = Color(0xFFFFB4AA),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD5),
)

/** Status colours for the calendar, shared by both themes with per-theme tuning. */
data class StatusColors(
    val complete: Color,
    val partial: Color,
    val missed: Color,
    val neutral: Color,
)

val LightStatusColors = StatusColors(
    complete = Color(0xFF3F6B54),
    partial = Color(0xFFB2762A),
    missed = Color(0xFFA33A31),
    neutral = Color(0xFFC1C7CE),
)

val DarkStatusColors = StatusColors(
    complete = Color(0xFF7FC7A0),
    partial = Color(0xFFE0AC6A),
    missed = Color(0xFFE59A92),
    neutral = Color(0xFF41484D),
)

/**
 * Task accents are persisted as ARGB longs (0xFF2A6F97). Narrowing to Int is
 * what [Color]'s ColorInt constructor expects, and is lossless for that range.
 */
fun Long.toAccentColor(): Color = Color(this.toInt())

/** Accent colours offered when creating or editing a task. */
object TaskPalette {
    val options: List<Long> = listOf(
        0xFF2A6F97, // slate blue
        0xFF3F6B54, // sage
        0xFF7B5E7B, // muted plum
        0xFFB2762A, // amber
        0xFF52606B, // mist grey
        0xFF356B6B, // teal
        0xFF8C5A4A, // clay
        0xFF4A5A8C, // indigo
    )

    fun forIndex(index: Int): Long = options[index % options.size]
}
