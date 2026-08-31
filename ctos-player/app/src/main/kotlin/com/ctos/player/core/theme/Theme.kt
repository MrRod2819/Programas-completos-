package com.ctos.player.core.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.ctos.player.data.model.RingStyle
import com.ctos.player.data.model.ThemePalette

/** Exposes the currently selected [ThemePalette] to the whole UI tree. */
val LocalPalette = staticCompositionLocalOf { ThemePalette.DedSecPop }

/** Exposes the vinyl ring style selected in Settings. */
val LocalRingStyle = staticCompositionLocalOf { RingStyle.GraffitiSplash }

@Composable
fun CtosTheme(
    palette: ThemePalette = ThemePalette.DedSecPop,
    ringStyle: RingStyle = RingStyle.GraffitiSplash,
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Palette swaps are animated so switching themes feels like a live re-skin.
    val primary by animateColorAsState(palette.primary, tween(400), label = "primary")
    val secondary by animateColorAsState(palette.secondary, tween(400), label = "secondary")
    val tertiary by animateColorAsState(palette.tertiary, tween(400), label = "tertiary")
    val background by animateColorAsState(palette.background, tween(400), label = "background")

    val colorScheme = darkColorScheme(
        primary = primary,
        onPrimary = TextPrimary,
        secondary = secondary,
        onSecondary = palette.background,
        tertiary = tertiary,
        onTertiary = palette.background,
        background = background,
        onBackground = palette.textPrimary,
        surface = palette.panel,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.panel,
        onSurfaceVariant = palette.textMuted,
        outline = HairLine,
    )

    CompositionLocalProvider(
        LocalPalette provides palette,
        LocalRingStyle provides ringStyle,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CtosTypography,
            content = content,
        )
    }
}
