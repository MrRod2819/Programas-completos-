package com.ctos.player.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Entry transition shared by every list item: fade-in plus a 40px slide-up. */
val TrackEnterTransition = fadeIn(animationSpec = tween(durationMillis = 250)) +
    slideInVertically(animationSpec = tween(durationMillis = 250)) { 40 }

/** Bouncy spring used for item placement, reordering and overscroll. */
fun <T> softSpring() = spring<T>(stiffness = Spring.StiffnessLow)

/**
 * Fades and slides an item up as soon as it enters composition — i.e. when it
 * scrolls into view inside a LazyColumn. [index] and [stepMillis] stagger the
 * first screenful so the list assembles itself row by row.
 */
@Composable
fun Modifier.staggeredEntry(index: Int = 0, stepMillis: Int = 0): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val fraction by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 250, delayMillis = index * stepMillis),
        label = "staggeredEntry",
    )
    val slidePx = with(LocalDensity.current) { 40.dp.toPx() }
    return this.graphicsLayer {
        alpha = fraction
        translationY = (1f - fraction) * slidePx
    }
}
