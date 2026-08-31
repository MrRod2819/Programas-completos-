package com.ctos.player.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.core.theme.techStyle
import com.ctos.player.data.model.RingStyle
import com.ctos.player.data.model.ThemePalette
import com.ctos.player.ui.components.CtosStatusBar
import com.ctos.player.ui.components.SectionRow
import com.ctos.player.ui.components.neonGlow

/** "Personalización": palette engine plus the disc ring style selector. */
@Composable
fun SettingsScreen(
    selectedPalette: ThemePalette,
    selectedRing: RingStyle,
    clock: String,
    onSelectPalette: (ThemePalette) -> Unit,
    onSelectRing: (RingStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CtosStatusBar(clock)
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Personalización", style = techStyle(18, FontWeight.Bold), color = palette.textPrimary)
            Text(
                "Elige tu identidad visual en la red",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textMuted,
            )
        }

        Row(
            Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A1520), Color(0xFF0D0B10))))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MiniDisc(selectedPalette)
            Column {
                Text(
                    selectedPalette.name,
                    style = techStyle(13, FontWeight.Bold),
                    color = palette.textPrimary,
                )
                Text(
                    selectedPalette.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            ThemePalette.all.forEach { option ->
                PaletteRow(
                    option = option,
                    selected = option.id == selectedPalette.id,
                    onClick = { onSelectPalette(option) },
                )
            }
        }

        SectionRow("Estilo del anillo del disco", modifier = Modifier.padding(horizontal = 20.dp))
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RingStyle.entries.forEach { style ->
                RingOption(
                    style = style,
                    selected = style == selectedRing,
                    onClick = { onSelectRing(style) },
                )
            }
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun MiniDisc(option: ThemePalette) {
    val transition = rememberInfiniteTransition(label = "miniDiscTheme")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "miniDiscSpin",
    )
    Box(
        Modifier
            .size(70.dp)
            .rotate(if (option.animatedGradient) spin else 0f)
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    listOf(option.primary, option.secondary, option.tertiary, option.primary),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(option.panel),
        )
    }
}

@Composable
private fun PaletteRow(option: ThemePalette, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) {
                    Modifier
                        .neonGlow(palette.primary, 12.dp, 0.3f, 12.dp)
                        .border(1.dp, palette.primary, RoundedCornerShape(12.dp))
                } else {
                    Modifier.border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                },
            )
            .background(palette.panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row {
            option.swatches.forEachIndexed { index, color ->
                Box(
                    Modifier
                        // Overlapping stack, mirroring the CSS `margin-left:-5px`.
                        .offset(x = (index * -5).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(2.dp, palette.panel, CircleShape),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(option.name, style = techStyle(11, FontWeight.SemiBold), color = palette.textPrimary)
            Text(
                option.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted,
            )
        }
        Box(
            Modifier
                .size(16.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) palette.primary else palette.textMuted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(palette.primary),
                )
            }
        }
    }
}

@Composable
private fun RingOption(style: RingStyle, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    val brush = when (style) {
        RingStyle.GraffitiSplash -> Brush.sweepGradient(
            listOf(palette.primary, palette.secondary, palette.tertiary, palette.primary),
        )
        RingStyle.MagentaHalo -> Brush.radialGradient(
            listOf(Color.Transparent, Color.Transparent, palette.primary),
        )
        RingStyle.CyanHalo -> Brush.radialGradient(
            listOf(Color.Transparent, Color.Transparent, palette.tertiary),
        )
        RingStyle.YellowTicks -> Brush.sweepGradient(
            List(24) { index -> if (index % 2 == 0) palette.secondary else palette.background },
        )
    }
    Box(
        Modifier
            .size(56.dp)
            .then(
                if (selected) {
                    Modifier.neonGlow(palette.secondary, 12.dp, 0.5f, 28.dp)
                } else {
                    Modifier
                },
            )
            .clip(CircleShape)
            .background(brush)
            .then(
                if (selected) Modifier.border(2.dp, palette.secondary, CircleShape) else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(palette.background),
        )
    }
}
