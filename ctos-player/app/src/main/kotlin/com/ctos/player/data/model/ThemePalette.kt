package com.ctos.player.data.model

import androidx.compose.ui.graphics.Color
import com.ctos.player.core.theme.BgBase
import com.ctos.player.core.theme.BgPanel
import com.ctos.player.core.theme.CyberYellow
import com.ctos.player.core.theme.DedSecMagenta
import com.ctos.player.core.theme.NeonCyan
import com.ctos.player.core.theme.PureBlack
import com.ctos.player.core.theme.TextMuted
import com.ctos.player.core.theme.TextPrimary

/** A selectable visual identity ("Elige tu identidad visual en la red"). */
data class ThemePalette(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val panel: Color,
    val textPrimary: Color = TextPrimary,
    val textMuted: Color = TextMuted,
    val animatedGradient: Boolean = false,
) {
    /** Swatches shown on the palette rows of the Settings screen. */
    val swatches: List<Color> get() = listOf(primary, secondary, tertiary)

    companion object {
        val DedSecPop = ThemePalette(
            id = "dedsec_pop",
            name = "DedSec Pop",
            subtitle = "Pop art hacker · combo 3 colores",
            description = "Magenta + amarillo + negro. El combo del logo DedSec — anillo activo: Graffiti Splash.",
            primary = DedSecMagenta,
            secondary = CyberYellow,
            tertiary = NeonCyan,
            background = BgBase,
            panel = BgPanel,
        )

        val BayAreaSunset = ThemePalette(
            id = "bay_area_sunset",
            name = "Bay Area Sunset",
            subtitle = "Skyline SF · naranja/rosa/púrpura",
            description = "El atardecer sobre la bahía: naranja, rosa y púrpura sobre negro.",
            primary = Color(0xFFFF4FA0),
            secondary = Color(0xFFFF8A3D),
            tertiary = Color(0xFF7A3DFF),
            background = Color(0xFF0C0810),
            panel = Color(0xFF17111C),
        )

        val GlitchRainbow = ThemePalette(
            id = "glitch_rainbow",
            name = "Glitch Rainbow",
            subtitle = "Degradado animado · caótico",
            description = "Degradado animado que recorre cian, magenta y amarillo sin parar.",
            primary = Color(0xFF33E6E6),
            secondary = DedSecMagenta,
            tertiary = CyberYellow,
            background = BgBase,
            panel = BgPanel,
            animatedGradient = true,
        )

        val HackspaceGreen = ThemePalette(
            id = "hackspace_green",
            name = "Hackspace Green",
            subtitle = "Un solo acento vivo",
            description = "Verde lima neón como único acento sobre terminal negra.",
            primary = Color(0xFFB6FF3D),
            secondary = Color(0xFF7FCC1F),
            tertiary = Color(0xFFF4F4F7),
            background = BgBase,
            panel = BgPanel,
        )

        val AmoledPureBlack = ThemePalette(
            id = "amoled_pure_black",
            name = "AMOLED Pure Black",
            subtitle = "Ahorro de batería · S21",
            description = "Negro puro #000000 para apagar los píxeles del panel AMOLED.",
            primary = DedSecMagenta,
            secondary = CyberYellow,
            tertiary = NeonCyan,
            background = PureBlack,
            panel = Color(0xFF0B0B0D),
        )

        val all = listOf(DedSecPop, BayAreaSunset, GlitchRainbow, HackspaceGreen, AmoledPureBlack)

        fun fromId(id: String?): ThemePalette = all.firstOrNull { it.id == id } ?: DedSecPop
    }
}

/** Ring drawn around the vinyl disc on the Now Playing screen. */
enum class RingStyle(val id: String, val label: String) {
    GraffitiSplash("graffiti_splash", "Graffiti Splash"),
    MagentaHalo("magenta_halo", "Magenta Halo"),
    CyanHalo("cyan_halo", "Cyan Halo"),
    YellowTicks("yellow_ticks", "Yellow Ticks");

    companion object {
        fun fromId(id: String?): RingStyle = entries.firstOrNull { it.id == id } ?: GraffitiSplash
    }
}
