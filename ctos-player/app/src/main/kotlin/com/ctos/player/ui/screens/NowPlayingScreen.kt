package com.ctos.player.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.LiveGreen
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.core.theme.LocalRingStyle
import com.ctos.player.core.theme.heroBackground
import com.ctos.player.core.theme.monoStyle
import com.ctos.player.core.theme.techStyle
import com.ctos.player.data.model.PlayerState
import com.ctos.player.data.model.RepeatMode as PlayerRepeatMode
import com.ctos.player.data.model.formatClock
import com.ctos.player.ui.components.CtosStatusBar
import com.ctos.player.ui.components.VinylDisc
import com.ctos.player.ui.components.circularGlow
import com.ctos.player.ui.components.glassPanel
import com.ctos.player.ui.components.neonGlow

/** Immersive full-screen player with the vinyl hero and transport controls. */
@Composable
fun NowPlayingScreen(
    state: PlayerState,
    clock: String,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    val song = state.current

    Column(
        modifier
            .fillMaxSize()
            .heroBackground(palette.primary, palette.tertiary),
    ) {
        CtosStatusBar(clock)

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .glassPanel(shape = RoundedCornerShape(9.dp), elevation = 0.dp)
                    .clickable(onClick = onCollapse),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = "Contraer",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("REPRODUCIENDO DESDE", style = techStyle(9), color = palette.textMuted)
                Text(
                    state.sourceLabel,
                    style = techStyle(11, FontWeight.SemiBold),
                    color = palette.textPrimary,
                )
            }
            LiveIndicator()
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 26.dp, bottom = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            VinylDisc(
                isPlaying = state.isPlaying,
                artworkUri = song?.albumArtUri,
                ringStyle = LocalRingStyle.current,
                magenta = palette.primary,
                yellow = palette.secondary,
                cyan = palette.tertiary,
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = song?.title ?: "Sin reproducción",
                    style = techStyle(19, FontWeight.Bold),
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val isFavorite = song != null && song.id in state.favorites
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = palette.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onToggleFavorite),
                )
            }
            Text(
                text = listOfNotNull(song?.artist, song?.album?.takeIf { it.isNotBlank() })
                    .joinToString(" · ")
                    .ifBlank { "—" },
                style = techStyle(12, FontWeight.Normal),
                color = palette.textMuted,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        CyberSeekBar(
            progress = state.progress,
            positionMs = state.positionMs,
            remainingMs = state.remainingMs,
            durationMs = state.durationMs,
            onSeek = onSeek,
            modifier = Modifier.padding(start = 26.dp, end = 26.dp, top = 22.dp),
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            ControlIcon(Icons.Filled.Shuffle, "Aleatorio", state.shuffle, onShuffle)
            ControlIcon(Icons.Filled.SkipPrevious, "Anterior", false, onPrevious, size = 28)
            Box(
                Modifier
                    .size(66.dp)
                    .circularGlow(palette.primary, 22.dp, 0.7f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFF5FA8), palette.primary),
                            center = Offset(0.3f, 0.3f),
                        ),
                    )
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pausar" else "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            ControlIcon(Icons.Filled.SkipNext, "Siguiente", false, onNext, size = 28)
            ControlIcon(
                imageVector = if (state.repeatMode == PlayerRepeatMode.One) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                contentDescription = "Repetir",
                active = state.repeatMode != PlayerRepeatMode.Off,
                onClick = onRepeat,
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 26.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            FooterAction(Icons.AutoMirrored.Filled.QueueMusic, "COLA", onQueue)
            FooterAction(Icons.Filled.Lyrics, "LETRAS") {}
            FooterAction(Icons.Filled.BarChart, "SPECTRUM") {}
            FooterAction(Icons.Filled.Share, "BROADCAST") {}
        }
    }
}

@Composable
private fun LiveIndicator() {
    val palette = LocalPalette.current
    val transition = rememberInfiniteTransition(label = "live")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "liveAlpha",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .circularGlow(LiveGreen, 4.dp, alpha)
                .clip(CircleShape)
                .background(LiveGreen.copy(alpha = alpha)),
        )
        Text("LIVE", style = monoStyle(9), color = palette.textMuted)
    }
}

/** Gradient track with a rotated diamond thumb; drag anywhere to scrub. */
@Composable
private fun CyberSeekBar(
    progress: Float,
    positionMs: Long,
    remainingMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    var scrub by remember { mutableStateOf<Float?>(null) }
    val shown = scrub ?: progress

    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> scrub = (offset.x / size.width).coerceIn(0f, 1f) },
                        onDragEnd = {
                            scrub?.let { onSeek((it * durationMs).toLong()) }
                            scrub = null
                        },
                        onDragCancel = { scrub = null },
                    ) { change, _ ->
                        scrub = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.12f)),
            )
            Box(
                Modifier
                    .fillMaxWidth(shown)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .neonGlow(palette.primary, 8.dp, 0.6f, 3.dp)
                    .background(Brush.horizontalGradient(listOf(palette.primary, palette.secondary))),
            )
            Box(
                Modifier
                    .fillMaxWidth(shown)
                    .height(10.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .rotate(45f)
                        .neonGlow(palette.secondary, 8.dp, 0.8f)
                        .background(palette.secondary),
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatClock(positionMs), style = monoStyle(10), color = palette.textMuted)
            Text("-" + formatClock(remainingMs), style = monoStyle(10), color = palette.textMuted)
        }
    }
}

@Composable
private fun ControlIcon(
    imageVector: ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
    size: Int = 24,
) {
    val palette = LocalPalette.current
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = if (active) palette.secondary else palette.textMuted,
        modifier = Modifier
            .then(if (active) Modifier.neonGlow(palette.secondary, 6.dp, 0.5f, 12.dp) else Modifier)
            .size(size.dp)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun FooterAction(imageVector: ImageVector, label: String, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Icon(imageVector, contentDescription = label, tint = palette.textMuted, modifier = Modifier.size(20.dp))
        Text(label, style = techStyle(8), color = palette.textMuted)
    }
}
