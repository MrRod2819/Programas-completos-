package com.ctos.player.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The mockups use Chakra Petch for headers/badges, Inter for content and
 * JetBrains Mono for timers and stats. Those fonts are not bundled, so the
 * closest system families are used: a condensed-ish sans for tech headers,
 * the default sans for content and monospace for numeric readouts.
 */
val TechFamily = FontFamily.SansSerif
val ContentFamily = FontFamily.Default
val MonoFamily = FontFamily.Monospace

/** Chakra-Petch stand-in: uppercase-ish tech headers and badges. */
fun techStyle(size: Int, weight: FontWeight = FontWeight.SemiBold, letterSpacing: Double = 0.4) =
    TextStyle(
        fontFamily = TechFamily,
        fontWeight = weight,
        fontSize = size.sp,
        letterSpacing = letterSpacing.sp,
    )

/** JetBrains-Mono stand-in: timers, stats and status readouts. */
fun monoStyle(size: Int, weight: FontWeight = FontWeight.Medium) =
    TextStyle(
        fontFamily = MonoFamily,
        fontWeight = weight,
        fontSize = size.sp,
    )

val CtosTypography = Typography(
    displaySmall = techStyle(22, FontWeight.Bold),
    headlineMedium = techStyle(19, FontWeight.Bold),
    headlineSmall = techStyle(18, FontWeight.Bold),
    titleMedium = TextStyle(
        fontFamily = ContentFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = ContentFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.5f.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ContentFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = ContentFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.5f.sp,
    ),
    labelSmall = techStyle(9, FontWeight.SemiBold, 0.5),
)
