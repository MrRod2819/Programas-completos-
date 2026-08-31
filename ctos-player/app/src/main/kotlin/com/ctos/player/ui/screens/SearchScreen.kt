package com.ctos.player.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.core.theme.monoStyle
import com.ctos.player.core.theme.techStyle
import com.ctos.player.ui.components.TrackRow
import com.ctos.player.ui.components.TrackRowSkeleton
import com.ctos.player.ui.components.glassPanel
import com.ctos.player.ui.components.staggeredEntry
import com.ctos.player.ui.viewmodel.SearchResults
import com.ctos.player.data.model.Song

/** Radar screen: a live conic sweep plus categorised results. */
@Composable
fun SearchScreen(
    query: String,
    results: SearchResults,
    scanning: Boolean,
    clock: String,
    onQueryChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    contentPadding: PaddingValues = PaddingValues(bottom = 150.dp),
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Column(modifier.fillMaxSize()) {
        com.ctos.player.ui.components.CtosStatusBar(clock)

        Row(
            Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(42.dp)
                .glassPanel(shape = RoundedCornerShape(12.dp), outline = palette.tertiary)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = monoStyle(11).copy(color = palette.textPrimary),
                cursorBrush = SolidColor(palette.tertiary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text("buscar en la red...", style = monoStyle(11), color = palette.textMuted)
                    }
                    inner()
                },
            )
        }

        AnimatedContent(
            targetState = scanning,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "searchSwap",
        ) { isScanning ->
            if (isScanning) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Radar()
                    Text(
                        "ESCANEANDO LA RED...",
                        style = monoStyle(10),
                        color = palette.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                    )
                    repeat(4) { TrackRowSkeleton() }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = contentPadding.calculateBottomPadding(),
                    ),
                ) {
                    if (results.isEmpty) {
                        item {
                            Text(
                                if (query.isBlank()) "Escribe para escanear la red." else "Sin resultados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted,
                                modifier = Modifier.padding(vertical = 24.dp),
                            )
                        }
                    }
                    if (results.songs.isNotEmpty()) {
                        item(key = "h-songs") { ResultHeader("CANCIONES") }
                        itemsIndexed(results.songs, key = { _, s -> "song-" + s.id }) { index, song ->
                            TrackRow(
                                song = song,
                                onClick = { onSongClick(song) },
                                showMenu = false,
                                modifier = Modifier
                                    .animateItem()
                                    .staggeredEntry(index.coerceAtMost(8), 35),
                            )
                        }
                    }
                    if (results.artists.isNotEmpty()) {
                        item(key = "h-artists") { ResultHeader("ARTISTAS") }
                        itemsIndexed(results.artists, key = { _, a -> "artist-$a" }) { index, artist ->
                            ArtistRow(
                                artist,
                                modifier = Modifier
                                    .animateItem()
                                    .staggeredEntry(index.coerceAtMost(8), 35),
                            )
                        }
                    }
                    if (results.videos.isNotEmpty()) {
                        item(key = "h-videos") { ResultHeader("VIDEOS") }
                        itemsIndexed(results.videos, key = { _, s -> "video-" + s.id }) { index, song ->
                            TrackRow(
                                song = song,
                                onClick = { onSongClick(song) },
                                showMenu = false,
                                modifier = Modifier
                                    .animateItem()
                                    .staggeredEntry(index.coerceAtMost(8), 35),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultHeader(label: String) {
    val palette = LocalPalette.current
    Text(
        label,
        style = techStyle(11, FontWeight.SemiBold),
        color = palette.textMuted,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
    )
}

@Composable
private fun ArtistRow(artist: String, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF2A2A35), Color(0xFF161619)))),
        )
        Column {
            Text(artist, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
            Text(
                "Artista de la biblioteca",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted,
            )
        }
    }
}

/** Concentric rings with an infinitely rotating conic sweep. */
@Composable
private fun Radar(modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    val transition = rememberInfiniteTransition(label = "radar")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "radarSweep",
    )
    Box(
        modifier
            .fillMaxWidth()
            .padding(vertical = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(120.dp)) {
            val radius = size.minDimension / 2f
            listOf(1f, 0.66f, 0.33f).forEach { fraction ->
                drawCircle(
                    color = palette.tertiary.copy(alpha = 0.20f * fraction),
                    radius = radius * fraction,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        palette.tertiary.copy(alpha = 0.5f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Transparent,
                    ),
                ),
                startAngle = angle,
                sweepAngle = 110f,
                useCenter = true,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
            )
        }
    }
    Spacer(Modifier.height(0.dp))
}
