package com.ctos.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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
import com.ctos.player.core.theme.monoStyle
import com.ctos.player.data.model.Song

/** Square artwork with the little format badge in the bottom-right corner. */
@Composable
fun TrackCover(
    song: Song,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val palette = LocalPalette.current
    val accent = if (song.isVideoLike) palette.tertiary else palette.secondary
    val shape = RoundedCornerShape(if (song.isVideoLike) 6.dp else 10.dp)
    Box(modifier.size(48.dp)) {
        Box(
            Modifier
                .size(48.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(Color(0xFF2A2A35), Color(0xFF161619))))
                .then(
                    when {
                        highlighted -> Modifier
                            .neonGlow(palette.secondary, 10.dp, 0.5f, 10.dp)
                            .border(2.dp, palette.secondary, shape)
                        song.isVideoLike -> Modifier.border(2.dp, palette.tertiary.copy(alpha = 0.5f), shape)
                        else -> Modifier
                    },
                ),
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(shape),
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(palette.background)
                .border(1.5f.dp, accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (song.isVideoLike) Icons.Filled.PlayArrow else Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(9.dp),
            )
        }
    }
}

/** A library / search result row: cover, title, artist, duration, overflow. */
@Composable
fun TrackRow(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    showMenu: Boolean = true,
    onMenuClick: () -> Unit = {},
    leading: @Composable (() -> Unit)? = null,
) {
    val palette = LocalPalette.current
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading?.invoke()
        TrackCover(song, highlighted = isActive)
        Column(Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isActive) palette.secondary else palette.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = song.durationLabel,
            style = monoStyle(10),
            color = palette.textMuted,
        )
        if (showMenu) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Opciones",
                tint = palette.textMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onMenuClick),
            )
        }
    }
}
