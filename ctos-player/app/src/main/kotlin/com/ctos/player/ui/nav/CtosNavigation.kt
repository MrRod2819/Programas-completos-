package com.ctos.player.ui.nav

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ctos.player.core.theme.LocalPalette
import com.ctos.player.ui.components.circularGlow

/** Destinations reachable from the bottom navigation. */
enum class CtosDestination(val route: String, val icon: ImageVector, val label: String) {
    Home("home", Icons.Filled.Home, "Inicio"),
    Search("search", Icons.Filled.Search, "Buscar"),
    Library("library", Icons.AutoMirrored.Filled.List, "Biblioteca"),
    Radio("radio", Icons.Filled.Radio, "Radio"),
    Profile("profile", Icons.Filled.Person, "Perfil"),
    Settings("settings", Icons.Filled.Settings, "Ajustes"),
}

/** Glassy bottom bar with the glowing magenta pill on the active item. */
@Composable
fun CtosBottomBar(
    selected: CtosDestination,
    onSelect: (CtosDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(palette.background.copy(alpha = 0.92f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        CtosDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            Box(
                Modifier
                    .size(26.dp)
                    .then(
                        if (isSelected) Modifier.circularGlow(palette.primary, 8.dp, 0.6f) else Modifier,
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) palette.primary else Color(0xFF2A2A34))
                    .clickable { onSelect(destination) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    destination.icon,
                    contentDescription = destination.label,
                    tint = if (isSelected) Color.White else palette.textMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/** Bouncy spring reused by navigation transitions and overscroll. */
fun <T> navSpring() = spring<T>(stiffness = Spring.StiffnessLow)
