package com.ctos.player.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.core.theme.monoStyle
import com.ctos.player.core.theme.techStyle

/** `21:04 · ctOS · 5G · 87%` faux status strip from the mockups. */
@Composable
fun CtosStatusBar(clock: String, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .height(34.dp)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(clock, style = monoStyle(10), color = palette.textMuted)
        Text("ctOS · 5G · 87%", style = monoStyle(10), color = palette.textMuted)
    }
}

/** Terminal-style search field with a blinking cyan caret. */
@Composable
fun TerminalSearchBar(
    text: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    val palette = LocalPalette.current
    val caretColor = accent ?: palette.tertiary
    val transition = rememberInfiniteTransition(label = "caret")
    val caretAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "caretAlpha",
    )
    Row(
        modifier
            .fillMaxWidth()
            .height(42.dp)
            .glassPanel(shape = RoundedCornerShape(12.dp), outline = (accent ?: Color(0x14FFFFFF)))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = accent ?: palette.textMuted,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text.ifBlank { placeholder },
            style = monoStyle(11),
            color = if (text.isBlank()) palette.textMuted else palette.textPrimary,
        )
        Box(
            Modifier
                .alpha(caretAlpha)
                .size(width = 6.dp, height = 12.dp)
                .background(caretColor),
        )
    }
}

/** Top tab strip (Inicio · Buscar · Biblioteca · Radio · Perfil). */
@Composable
fun CtosTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selected
            Text(
                text = label,
                style = techStyle(12, if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                color = if (isSelected) palette.textPrimary else palette.textMuted,
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .then(if (isSelected) Modifier.tabUnderline(palette.primary) else Modifier)
                    .padding(bottom = 6.dp),
            )
        }
    }
}

/** Pill-shaped filter chip; the active one is a solid yellow sticker. */
@Composable
fun CtosChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Box(
        modifier
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier
                        .neonGlow(palette.secondary, 10.dp, 0.4f, 999.dp)
                        .background(palette.secondary)
                } else {
                    Modifier
                        .background(palette.panel)
                        .border(1.dp, Color(0x14FFFFFF), CircleShape)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            style = techStyle(10, FontWeight.SemiBold),
            color = if (selected) palette.background else palette.textMuted,
        )
    }
}

/** Section header with an optional trailing accent action. */
@Composable
fun SectionRow(title: String, trailing: String? = null, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = techStyle(13, FontWeight.Bold), color = palette.textPrimary)
        if (trailing != null) {
            Text(trailing, style = techStyle(10, FontWeight.SemiBold), color = palette.primary)
        }
    }
}

/** Segmented control used by the queue sheet. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.panel)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(10.dp))
            .padding(3.dp),
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) {
                            Modifier
                                .neonGlow(palette.primary, 10.dp, 0.5f, 8.dp)
                                .background(palette.primary)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = techStyle(10, FontWeight.SemiBold),
                    color = if (isSelected) Color.White else palette.textMuted,
                )
            }
        }
    }
}

/** Gradient drag handle of the bottom sheets. */
@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    Box(
        modifier
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(3.dp))
            .neonGlow(palette.primary, 8.dp, 0.5f, 3.dp)
            .background(Brush.horizontalGradient(listOf(palette.primary, palette.tertiary))),
    )
}
