package com.ctos.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.core.theme.monoStyle
import com.ctos.player.core.theme.techStyle
import com.ctos.player.data.model.Song
import com.ctos.player.ui.components.CtosStatusBar
import com.ctos.player.ui.components.TrackRow
import com.ctos.player.ui.components.circularGlow
import com.ctos.player.ui.components.neonGlow
import com.ctos.player.ui.viewmodel.OperatorStats

/** Operator profile with the hacker stat grid, badges and recent activity. */
@Composable
fun ProfileScreen(
    operatorName: String,
    stats: OperatorStats,
    recentActivity: List<Song>,
    clock: String,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CtosStatusBar(clock)

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(80.dp)
                    .circularGlow(palette.primary, 16.dp, 0.5f)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF2A2A35), Color(0xFF141418))))
                    .border(3.dp, palette.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    operatorName.take(2).uppercase(),
                    style = techStyle(22, FontWeight.Bold),
                    color = palette.textPrimary,
                )
            }
            Text(
                operatorName,
                style = techStyle(15, FontWeight.Bold),
                color = palette.textPrimary,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                "OPERADOR · NIVEL ${stats.level} · XP ${stats.xp}",
                style = monoStyle(10),
                color = palette.secondary,
            )
            Box(
                Modifier
                    .padding(top = 10.dp, start = 40.dp, end = 40.dp)
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(stats.xpProgress)
                        .height(4.dp)
                        .neonGlow(palette.primary, 6.dp, 0.5f, 4.dp)
                        .background(Brush.horizontalGradient(listOf(palette.primary, palette.secondary))),
                )
            }
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("${stats.totalMinutes}h", "MINUTOS ESCUCHADOS (TOTAL)", Modifier.weight(1f))
                StatCard(stats.topGenreLabel, "GÉNERO TOP", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("${stats.playlists}", "PLAYLISTS CREADAS", Modifier.weight(1f))
                StatCard("${stats.streakDays}", "DÍAS DE RACHA", Modifier.weight(1f))
            }
        }

        Text(
            "Insignias desbloqueadas",
            style = techStyle(13, FontWeight.Bold),
            color = palette.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BadgeSticker(Icons.Filled.Star, palette.primary)
            BadgeSticker(Icons.Filled.Album, palette.secondary)
            BadgeSticker(Icons.Filled.Add, palette.tertiary)
        }

        Text(
            "Actividad reciente",
            style = techStyle(13, FontWeight.Bold),
            color = palette.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Column(Modifier.padding(horizontal = 20.dp)) {
            recentActivity.take(5).forEach { song ->
                TrackRow(song = song, onClick = { onSongClick(song) }, showMenu = false)
            }
            if (recentActivity.isEmpty()) {
                Text(
                    "Sin actividad todavía.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textMuted,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }
        Spacer(Modifier.height(140.dp))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.panel)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text(value, style = techStyle(20, FontWeight.Bold), color = palette.secondary)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun BadgeSticker(icon: ImageVector, accent: Color) {
    Box(
        Modifier
            .size(46.dp)
            .neonGlow(accent, 12.dp, 0.4f, 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f))))
            .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}
