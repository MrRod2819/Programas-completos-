package com.ctos.player.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.core.theme.techStyle
import com.ctos.player.data.model.LibraryFilter
import com.ctos.player.data.model.LibraryUiState
import com.ctos.player.data.model.Song
import com.ctos.player.ui.components.CtosChip
import com.ctos.player.ui.components.CtosStatusBar
import com.ctos.player.ui.components.CtosTabs
import com.ctos.player.ui.components.LibrarySkeleton
import com.ctos.player.ui.components.RecentCardSkeleton
import com.ctos.player.ui.components.SectionRow
import com.ctos.player.ui.components.TerminalSearchBar
import com.ctos.player.ui.components.TrackRow
import com.ctos.player.ui.components.circularGlow
import com.ctos.player.ui.components.neonBorder
import com.ctos.player.ui.components.staggeredEntry

/**
 * Home / Biblioteca: operator header, top tabs, recently played rail, filter
 * chips and the local track list (with shimmer skeletons while MediaStore is
 * being scanned).
 */
@Composable
fun HomeScreen(
    state: LibraryUiState,
    songs: List<Song>,
    recent: List<Song>,
    filter: LibraryFilter,
    activeSongId: Long?,
    operatorName: String,
    clock: String,
    onFilterChange: (LibraryFilter) -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMenu: (Song) -> Unit,
    onSearchClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onThemeClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(bottom = 150.dp),
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Column(modifier.fillMaxSize()) {
        CtosStatusBar(clock)

        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .circularGlow(palette.primary, 8.dp, 0.6f)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1B22))
                        .border(2.dp, palette.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        operatorName.take(2).uppercase(),
                        style = techStyle(13, FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                ) {
                    Text("BIENVENIDO, OPERADOR", style = techStyle(10), color = palette.textMuted)
                    Text(
                        operatorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                    )
                }
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.panel)
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        tint = palette.textMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(6.dp)
                            .circularGlow(palette.primary, 4.dp, 0.9f)
                            .clip(CircleShape)
                            .background(palette.primary),
                    )
                }
                Box(
                    Modifier
                        .padding(start = 8.dp)
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.sweepGradient(
                                listOf(palette.primary, palette.secondary, palette.tertiary, palette.primary),
                            ),
                        )
                        .clickable(onClick = onThemeClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(palette.panel),
                    )
                }
            }

            TerminalSearchBar(
                text = "",
                placeholder = "Buscar en la red...",
                onClick = onSearchClick,
            )
            Spacer(Modifier.height(14.dp))
            CtosTabs(
                tabs = listOf("Inicio", "Buscar", "Biblioteca", "Radio", "Perfil"),
                selected = 0,
                onSelect = onTabSelected,
            )
            Spacer(Modifier.height(10.dp))
        }

        AnimatedContent(
            targetState = state is LibraryUiState.Ready,
            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
            label = "librarySwap",
        ) { ready ->
            if (!ready) {
                LibrarySkeleton(modifier = Modifier.padding(horizontal = 20.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = contentPadding.calculateBottomPadding(),
                    ),
                ) {
                    item(key = "recent-header") {
                        SectionRow("Reproducido recientemente", "VER TODO", Modifier.padding(vertical = 10.dp))
                    }
                    item(key = "recent-rail") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (recent.isEmpty()) {
                                items(3) { RecentCardSkeleton() }
                            } else {
                                items(recent, key = { it.id }) { song ->
                                    RecentCard(song, onClick = { onSongClick(song) })
                                }
                            }
                        }
                    }
                    item(key = "chips") {
                        Row(
                            Modifier.padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LibraryFilter.entries.forEach { entry ->
                                CtosChip(entry.label, entry == filter, { onFilterChange(entry) })
                            }
                        }
                    }
                    item(key = "library-header") {
                        SectionRow("Tu biblioteca", "${songs.size} pistas", Modifier.padding(bottom = 4.dp))
                    }
                    itemsIndexed(
                        items = songs,
                        key = { _, song -> song.id },
                    ) { index, song ->
                        TrackRow(
                            song = song,
                            onClick = { onSongClick(song) },
                            onMenuClick = { onSongMenu(song) },
                            isActive = song.id == activeSongId,
                            modifier = Modifier
                                .animateItem()
                                .staggeredEntry(index = index.coerceAtMost(8), stepMillis = 35),
                        )
                    }
                    if (songs.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                "No se encontraron pistas en el dispositivo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted,
                                modifier = Modifier.padding(vertical = 24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 100dp neon-bordered card of the "Reproducido recientemente" rail. */
@Composable
private fun RecentCard(song: Song, onClick: () -> Unit) {
    val palette = LocalPalette.current
    val accent = when ((song.id % 3).toInt()) {
        0 -> palette.primary
        1 -> palette.tertiary
        else -> palette.secondary
    }
    Box(
        Modifier
            .size(100.dp)
            .neonBorder(accent, cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF22222C), Color(0xFF141418))))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = song.title,
            style = techStyle(9, FontWeight.SemiBold),
            color = palette.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
        )
    }
}
