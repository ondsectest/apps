package com.calmcontrol.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.calmcontrol.R

/**
 * One family throughout: **Lora**, a warm humanist serif.
 *
 * A single voice everywhere is the point. Mixing a display serif with a UI sans is the safe
 * pairing, but it also means the app sounds like two different things depending on where you
 * look — a journal at the top of the screen and a dashboard at the bottom. Lora alone keeps it
 * the journal.
 *
 * The one thing a straight swap gets wrong: Lora's x-height is much smaller than a screen sans
 * like Inter, so the same point size reads visibly smaller and body copy starts to feel frail.
 * Every size below `titleLarge` is therefore set about 1sp larger than the Material baseline, and
 * the small sizes carry extra tracking — serifs need more room between letters than a sans does
 * before they start to fill in at small sizes.
 *
 * Lora is a variable font, so all four weights come from one file. That is why `minSdk 26`
 * matters beyond `java.time`: font variation axes need API 26.
 */

@OptIn(ExperimentalTextApi::class)
private fun loraFont(weight: Int) = Font(
    resId = R.font.lora_variable,
    weight = FontWeight(weight),
    style = FontStyle.Normal,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Lora = FontFamily(
    loraFont(400),
    loraFont(500),
    loraFont(600),
    loraFont(700),
)

/**
 * Centres the text within its line box, so a heading sits optically centred in its space instead
 * of floating high — the usual reason careful type still looks slightly off in a Compose layout.
 */
private val Trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun lora(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Medium,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = Lora,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = Trim,
)

/**
 * Line height and tracking move in opposite directions as size grows: line height loosens towards
 * 1.6x on small body copy and tightens towards 1.25x on headlines, while tracking runs negative
 * on the large sizes — which set too loose by default — and positive on the small ones.
 */
val CalmTypography = Typography(
    displayLarge = lora(52, 60, FontWeight.Medium, -1.0),
    displayMedium = lora(42, 50, FontWeight.Medium, -0.75),
    displaySmall = lora(34, 42, FontWeight.Medium, -0.5),

    headlineLarge = lora(30, 38, FontWeight.Medium, -0.4),
    headlineMedium = lora(26, 34, FontWeight.Medium, -0.3),
    headlineSmall = lora(22, 30, FontWeight.Medium, -0.2),

    titleLarge = lora(20, 28, FontWeight.Medium, -0.1),
    titleMedium = lora(17, 24, FontWeight.Medium, 0.0),
    titleSmall = lora(15, 21, FontWeight.Medium, 0.1),

    // Regular weight for running text; Lora's 400 is sturdy enough to hold a paragraph, and
    // Medium would make body copy feel shouted.
    bodyLarge = lora(17, 26, FontWeight.Normal, 0.0),
    bodyMedium = lora(15, 23, FontWeight.Normal, 0.1),
    bodySmall = lora(13, 20, FontWeight.Normal, 0.2),

    // Labels sit at Medium and carry the most tracking. These are the chart axis and legend
    // sizes, where a serif at Regular would start to close up.
    labelLarge = lora(15, 20, FontWeight.Medium, 0.15),
    labelMedium = lora(13, 17, FontWeight.Medium, 0.45),
    labelSmall = lora(12, 16, FontWeight.Medium, 0.55),
)

/**
 * Fixed-width digits.
 *
 * Use this anywhere a number is centred, right-aligned in a column, or changes in place. With
 * proportional figures a 1 is narrower than a 7, so a value that updates shifts position as it
 * changes. `tnum` makes every digit the same width, and the number simply changes.
 *
 * Lora implements the feature, so it takes effect rather than being silently ignored.
 */
fun TextStyle.tabularFigures(): TextStyle = copy(fontFeatureSettings = "tnum")
