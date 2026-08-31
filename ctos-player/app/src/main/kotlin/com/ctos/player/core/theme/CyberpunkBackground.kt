package com.ctos.player.core.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws the ctOS sticker-bomb texture: two/three radial ambient glows plus a
 * faint 24dp technical grid, exactly like the `.texture` + `.grid-lines`
 * layers of the HTML mockups.
 */
fun Modifier.cyberpunkBackground(
    base: Color,
    magenta: Color,
    cyan: Color,
    yellow: Color,
    gridSpacing: Dp = 24.dp,
    gridAlpha: Float = 1f,
): Modifier = composedGrid(base, magenta, cyan, yellow, gridSpacing, gridAlpha)

private fun Modifier.composedGrid(
    base: Color,
    magenta: Color,
    cyan: Color,
    yellow: Color,
    gridSpacing: Dp,
    gridAlpha: Float,
): Modifier = this.then(
    Modifier.drawWithGrid(base, magenta, cyan, yellow, gridSpacing, gridAlpha),
)

private fun Modifier.drawWithGrid(
    base: Color,
    magenta: Color,
    cyan: Color,
    yellow: Color,
    gridSpacing: Dp,
    gridAlpha: Float,
) = this.then(
    Modifier.drawBehind {
        drawRect(base)
        val maxDim = maxOf(size.width, size.height)

        drawCircleGlow(magenta.copy(alpha = 0.10f), Offset(size.width * 0.15f, size.height * 0.20f), maxDim * 0.55f)
        drawCircleGlow(cyan.copy(alpha = 0.08f), Offset(size.width * 0.85f, size.height * 0.75f), maxDim * 0.55f)
        drawCircleGlow(yellow.copy(alpha = 0.05f), Offset(size.width * 0.5f, size.height * 0.5f), maxDim * 0.70f)

        val step = gridSpacing.toPx()
        val line = GridLine.copy(alpha = GridLine.alpha * gridAlpha)
        var x = 0f
        while (x <= size.width) {
            drawLine(line, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    },
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCircleGlow(
    color: Color,
    center: Offset,
    radius: Float,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

/**
 * Radial "now playing" backdrop: a magenta bloom at the top, a cyan bloom at
 * the bottom-left and a vertical fade to near-black.
 */
fun Modifier.heroBackground(magenta: Color, cyan: Color): Modifier = this.then(
    Modifier.drawBehind {
        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFF141018),
                0.6f to Color(0xFF08070A),
                1f to Color(0xFF08070A),
            ),
        )
        val maxDim = maxOf(size.width, size.height)
        drawCircleGlow(magenta.copy(alpha = 0.25f), Offset(size.width * 0.5f, size.height * 0.15f), maxDim * 0.6f)
        drawCircleGlow(cyan.copy(alpha = 0.15f), Offset(size.width * 0.2f, size.height * 0.9f), maxDim * 0.5f)
    },
)

/** Convenience wrapper that fills the screen with the ctOS background. */
@Composable
fun CyberpunkSurface(
    modifier: Modifier = Modifier,
    gridAlpha: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .cyberpunkBackground(
                base = palette.background,
                magenta = palette.primary,
                cyan = palette.tertiary,
                yellow = palette.secondary,
                gridAlpha = gridAlpha,
            ),
        content = content,
    )
}

/** Unused helper kept for density-aware sizing in custom canvases. */
@Composable
fun rememberPx(dp: Dp): Float = with(LocalDensity.current) { dp.toPx() }

internal fun Size.shortestSide(): Float = minOf(width, height)
