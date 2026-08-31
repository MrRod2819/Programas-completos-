package com.ctos.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Neon halo behind a component, mimicking the CSS `box-shadow: 0 0 Npx color`
 * glows used all over the mockups.
 */
fun Modifier.neonGlow(
    color: Color,
    radius: Dp = 14.dp,
    alpha: Float = 0.55f,
    cornerRadius: Dp = 0.dp,
): Modifier = this.then(
    Modifier.drawBehind {
        val spread = radius.toPx()
        val steps = 6
        repeat(steps) { step ->
            val fraction = (step + 1) / steps.toFloat()
            val inset = -spread * fraction
            drawRoundRect(
                color = color.copy(alpha = alpha * (1f - fraction) / steps * 2.2f),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    (cornerRadius.toPx() - inset).coerceAtLeast(0f),
                ),
            )
        }
    },
)

/** Glowing 1.5dp neon outline, as used by the "recently played" cards. */
fun Modifier.neonBorder(
    color: Color,
    cornerRadius: Dp = 12.dp,
    width: Dp = 1.5f.dp,
    glowAlpha: Float = 0.4f,
): Modifier = this
    .neonGlow(color, radius = 12.dp, alpha = glowAlpha, cornerRadius = cornerRadius)
    .border(width, color.copy(alpha = 0.75f), RoundedCornerShape(cornerRadius))

/** Frosted glass panel: translucent fill, hairline outline and soft shadow. */
fun Modifier.glassPanel(
    shape: Shape = RoundedCornerShape(14.dp),
    fill: Color = Color(0x99121218),
    outline: Color = Color(0x14FFFFFF),
    elevation: Dp = 8.dp,
): Modifier = this
    .shadow(elevation, shape, clip = false)
    .background(fill, shape)
    .border(1.dp, outline, shape)

/** Circular neon halo for round elements (play FAB, avatars, disc). */
fun Modifier.circularGlow(color: Color, radius: Dp = 20.dp, alpha: Float = 0.6f): Modifier =
    this.then(
        Modifier.drawBehind {
            val spread = radius.toPx()
            val base = size.minDimension / 2f
            val steps = 8
            repeat(steps) { step ->
                val fraction = (step + 1) / steps.toFloat()
                drawCircle(
                    color = color.copy(alpha = alpha * (1f - fraction) / steps * 2.4f),
                    radius = base + spread * fraction,
                )
            }
        },
    )

/** Underline used by the active top tab: 2dp magenta bar with a glow. */
fun Modifier.tabUnderline(color: Color): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidth = 2.dp.toPx()
        val y = size.height - strokeWidth / 2f
        repeat(3) { step ->
            drawLine(
                color = color.copy(alpha = 0.25f / (step + 1)),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth * (step + 2),
            )
        }
        drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth)
    },
)

/** Diagonal neon sweep used by the skeleton shimmer. */
fun scanlineBrush(colors: List<Color>, startX: Float, widthPx: Float): Brush = Brush.linearGradient(
    colors = colors,
    start = Offset(startX, 0f),
    end = Offset(startX + widthPx, widthPx),
)

internal fun Modifier.ringStroke(brush: Brush, width: Dp): Modifier = this.then(
    Modifier.drawBehind {
        drawCircle(
            brush = brush,
            radius = (size.minDimension - width.toPx()) / 2f,
            style = Stroke(width = width.toPx()),
        )
    },
)
