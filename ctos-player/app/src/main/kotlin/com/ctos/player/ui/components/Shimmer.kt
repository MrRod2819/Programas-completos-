package com.ctos.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.BgPanel
import com.ctos.player.core.theme.LocalPalette

/**
 * CRT scanline shimmer: a skewed neon gradient sweeping horizontally across
 * the placeholder, tinted with the palette's cyan and magenta accents.
 */
fun Modifier.crtShimmer(
    progress: Float,
    tintA: Color,
    tintB: Color,
): Modifier = this.drawWithCache {
    val bandWidth = size.width * 0.45f
    val startX = -bandWidth + (size.width + bandWidth * 2f) * progress
    val brush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            tintA.copy(alpha = 0.12f),
            tintB.copy(alpha = 0.10f),
            Color.Transparent,
        ),
        start = Offset(startX, 0f),
        // Slight vertical offset gives the sweep the skewed CRT look.
        end = Offset(startX + bandWidth, size.height * 1.6f),
    )
    onDrawWithContent {
        drawContent()
        drawRect(brush)
    }
}

/** Shared infinite sweep progress so every skeleton animates in lockstep. */
@Composable
fun rememberShimmerProgress(durationMillis: Int = 1400): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )
    return progress
}

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    progress: Float = rememberShimmerProgress(),
    cornerRadius: Dp = 8.dp,
) {
    val palette = LocalPalette.current
    Box(
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(BgPanel)
            .crtShimmer(progress, palette.tertiary, palette.primary),
    )
}

/** Placeholder row matching the geometry of a real track row. */
@Composable
fun TrackRowSkeleton(modifier: Modifier = Modifier, progress: Float = rememberShimmerProgress()) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBlock(Modifier.size(48.dp), progress, cornerRadius = 10.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SkeletonBlock(
                Modifier
                    .fillMaxWidth(0.55f)
                    .height(11.dp),
                progress,
            )
            SkeletonBlock(
                Modifier
                    .fillMaxWidth(0.32f)
                    .height(9.dp),
                progress,
            )
        }
        SkeletonBlock(
            Modifier
                .width(26.dp)
                .height(9.dp),
            progress,
        )
    }
}

/** Placeholder for the horizontal "Reproducido recientemente" cards. */
@Composable
fun RecentCardSkeleton(progress: Float = rememberShimmerProgress()) {
    SkeletonBlock(
        Modifier.size(100.dp),
        progress,
        cornerRadius = 12.dp,
    )
}

@Composable
fun LibrarySkeleton(rows: Int = 6, modifier: Modifier = Modifier) {
    val progress = rememberShimmerProgress()
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { RecentCardSkeleton(progress) }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                SkeletonBlock(
                    Modifier
                        .width(62.dp)
                        .height(24.dp)
                        .clip(CircleShape),
                    progress,
                    cornerRadius = 12.dp,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        repeat(rows) { TrackRowSkeleton(progress = progress) }
    }
}
