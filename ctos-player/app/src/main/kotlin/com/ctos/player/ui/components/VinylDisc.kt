package com.ctos.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ctos.player.data.model.RingStyle

/**
 * The Now Playing hero: a segmented "Graffiti Splash" ring, the rotating
 * album-art label and the physical tonearm resting on top of it.
 *
 * The disc spins at 33 RPM while [isPlaying]; when playback pauses the
 * rotation eases to a stop instead of snapping, and the tonearm lifts away.
 */
@Composable
fun VinylDisc(
    isPlaying: Boolean,
    artworkUri: Any?,
    ringStyle: RingStyle,
    magenta: Color,
    yellow: Color,
    cyan: Color,
    modifier: Modifier = Modifier,
    discSize: Dp = 236.dp,
) {
    val rotation = remember { mutableFloatStateOf(0f) }
    val speed by animateFloatAsState(
        targetValue = if (isPlaying) DEGREES_PER_SECOND else 0f,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "discSpeed",
    )

    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val deltaSeconds = (now - last) / 1_000_000_000f
            last = now
            rotation.floatValue = (rotation.floatValue + speed * deltaSeconds) % 360f
        }
    }

    // The tonearm drops onto the record while playing and lifts when paused.
    val needleAngle by animateFloatAsState(
        targetValue = if (isPlaying) 28f else 8f,
        animationSpec = tween(durationMillis = 600),
        label = "needleAngle",
    )

    Box(modifier.size(discSize), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(discSize * LABEL_FRACTION)
                .graphicsLayer { rotationZ = rotation.floatValue }
                .clip(CircleShape),
        )
        Canvas(Modifier.size(discSize)) {
            drawRing(ringStyle, magenta, yellow, cyan, rotation.floatValue)
            drawLabelPlate(discSize.toPx() * LABEL_FRACTION, rotation.floatValue)
            drawTonearm(needleAngle, yellow)
        }
    }
}

private const val LABEL_FRACTION = 0.66f

/** 33⅓ RPM expressed in degrees per second. */
private const val DEGREES_PER_SECOND = 200f

private fun DrawScope.drawRing(
    style: RingStyle,
    magenta: Color,
    yellow: Color,
    cyan: Color,
    rotation: Float,
) {
    val strokeWidth = size.minDimension * 0.055f
    val radius = (size.minDimension - strokeWidth) / 2f
    when (style) {
        RingStyle.GraffitiSplash -> {
            // Six hard colour segments, as in the CSS conic-gradient.
            val segments = listOf(magenta, yellow, cyan, magenta, yellow, cyan)
            val sweep = 360f / segments.size
            segments.forEachIndexed { index, color ->
                drawArc(
                    color = color,
                    startAngle = rotation + index * sweep - 90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth),
                )
            }
        }

        RingStyle.MagentaHalo -> drawHalo(magenta, strokeWidth, radius)
        RingStyle.CyanHalo -> drawHalo(cyan, strokeWidth, radius)
        RingStyle.YellowTicks -> {
            val ticks = 72
            repeat(ticks) { index ->
                if (index % 2 == 0) {
                    drawArc(
                        color = yellow,
                        startAngle = rotation + index * (360f / ticks) - 90f,
                        sweepAngle = 360f / ticks * 0.6f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawHalo(color: Color, strokeWidth: Float, radius: Float) {
    repeat(4) { step ->
        drawCircle(
            color = color.copy(alpha = 0.16f / (step + 1)),
            radius = radius + strokeWidth * step * 0.6f,
            style = Stroke(width = strokeWidth),
        )
    }
    drawCircle(color = color, radius = radius, style = Stroke(width = strokeWidth))
}

private fun DrawScope.drawLabelPlate(labelDiameter: Float, rotation: Float) {
    val center = Offset(size.width / 2f, size.height / 2f)
    // Vinyl grooves + specular highlight over the artwork.
    rotate(rotation, center) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.35f, size.height * 0.30f),
                radius = labelDiameter * 0.5f,
            ),
            radius = labelDiameter / 2f,
            center = center,
        )
    }
    drawCircle(
        color = Color(0xFF08070A),
        radius = labelDiameter / 2f + 3f,
        center = center,
        style = Stroke(width = 6f),
    )
    drawCircle(Color(0xFF08070A), radius = size.minDimension * 0.034f, center = center)
    drawCircle(
        color = Color(0xFF444444),
        radius = size.minDimension * 0.034f,
        center = center,
        style = Stroke(width = 4f),
    )
}

private fun DrawScope.drawTonearm(angleDegrees: Float, accent: Color) {
    val pivot = Offset(size.width * 0.93f, size.height * 0.07f)
    rotate(angleDegrees, pivot) {
        val armLength = size.minDimension * 0.30f
        val armThickness = size.minDimension * 0.055f
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFF3A3A45), Color(0xFF1C1C22))),
            topLeft = Offset(pivot.x - armLength, pivot.y - armThickness / 2f),
            size = Size(armLength, armThickness),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(armThickness / 2f),
        )
        val tip = Offset(pivot.x - armLength, pivot.y)
        drawCircle(accent.copy(alpha = 0.35f), radius = armThickness * 0.85f, center = tip)
        drawCircle(accent, radius = armThickness * 0.42f, center = tip)
    }
}
