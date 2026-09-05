package com.perspectivelive.wallpaper.data

import com.perspectivelive.wallpaper.utils.OklchColor
import java.time.LocalDate

/**
 * Deterministic generator producing infinite, harmonious wallpaper themes.
 * Adheres strictly to the architectural and calm minimalist ethos of Perspective - Live
 * using Oklch perceptual color coordinates.
 */
object ProceduralThemeGenerator {

    const val THEME_ID = "atmosphere"
    const val THEME_NAME = "Atmosphere"

    private const val OPACITY_30_PERCENT_MASK = 0x4D000000
    private const val RGB_MASK = 0x00FFFFFF

    // Golden angle step guarantees consecutive days have visually distinct base hues
    private const val GOLDEN_ANGLE = 137.507764f
    private const val FULL_CIRCLE = 360f

    // Lightness & Chroma invariant bounds
    private const val LIGHT_BG_L = 0.965f
    private const val LIGHT_BG_C = 0.012f

    private const val DARK_BG_L = 0.095f
    private const val DARK_BG_C = 0.018f

    private const val PAST_DOT_L = 0.58f
    private const val PAST_DOT_C = 0.055f

    private const val LIGHT_CURRENT_L = 0.62f
    private const val DARK_CURRENT_L = 0.72f
    private const val CURRENT_DOT_C = 0.16f

    private const val SPLIT_COMPLEMENTARY_OFFSET = 150f
    private const val COMPLEMENTARY_OFFSET = 180f

    // Hue angle boundaries for base descriptors
    private const val HUE_TERRACOTTA_MAX = 35f
    private const val HUE_DUNE_MAX = 70f
    private const val HUE_MOSS_MAX = 125f
    private const val HUE_CELADON_MAX = 175f
    private const val HUE_GLACIAL_MAX = 230f
    private const val HUE_BASALT_MAX = 280f
    private const val HUE_TWILIGHT_MAX = 325f

    // Hue angle boundaries for accent descriptors
    private const val ACCENT_EMBER_MAX = 45f
    private const val ACCENT_OCHRE_MAX = 90f
    private const val ACCENT_VERDANT_MAX = 150f
    private const val ACCENT_CYAN_MAX = 210f
    private const val ACCENT_ZENITH_MAX = 270f
    private const val ACCENT_AMETHYST_MAX = 320f

    /**
     * Generates a paired WallpaperTheme deterministically for the given date.
     */
    fun generateTheme(date: LocalDate = LocalDate.now()): WallpaperTheme {
        val epochDay = date.toEpochDay()
        val baseHue = Math.floorMod((epochDay * GOLDEN_ANGLE).toLong(), FULL_CIRCLE.toLong()).toFloat()

        val offset = if (Math.floorMod(epochDay, 2L) == 0L) SPLIT_COMPLEMENTARY_OFFSET else COMPLEMENTARY_OFFSET
        val accentHue = (baseHue + offset) % FULL_CIRCLE

        val lightBg = OklchColor.oklchToArgb(LIGHT_BG_L, LIGHT_BG_C, baseHue)
        val darkBg = OklchColor.oklchToArgb(DARK_BG_L, DARK_BG_C, baseHue)

        val pastColor = OklchColor.oklchToArgb(PAST_DOT_L, PAST_DOT_C, baseHue)
        val futureColor = (pastColor and RGB_MASK) or OPACITY_30_PERCENT_MASK

        val lightCurrent = OklchColor.oklchToArgb(LIGHT_CURRENT_L, CURRENT_DOT_C, accentHue)
        val darkCurrent = OklchColor.oklchToArgb(DARK_CURRENT_L, CURRENT_DOT_C, accentHue)

        val lightScheme = ColorScheme(
            id = THEME_ID,
            name = THEME_NAME,
            backgroundColor = lightBg,
            pastYearsColor = pastColor,
            currentYearColor = lightCurrent,
            futureYearsColor = futureColor,
            isDynamic = true,
            isCustom = false,
            isDark = false
        )

        val darkScheme = ColorScheme(
            id = THEME_ID,
            name = THEME_NAME,
            backgroundColor = darkBg,
            pastYearsColor = pastColor,
            currentYearColor = darkCurrent,
            futureYearsColor = futureColor,
            isDynamic = true,
            isCustom = false,
            isDark = true
        )

        return WallpaperTheme(
            id = THEME_ID,
            name = THEME_NAME,
            lightScheme = lightScheme,
            darkScheme = darkScheme,
            isRotatable = false
        )
    }

    /**
     * Creates an architectural and evocative mood descriptor for the date.
     */
    fun getMoodName(date: LocalDate = LocalDate.now()): String {
        val epochDay = date.toEpochDay()
        val baseHue = Math.floorMod((epochDay * GOLDEN_ANGLE).toLong(), FULL_CIRCLE.toLong()).toFloat()
        val offset = if (Math.floorMod(epochDay, 2L) == 0L) SPLIT_COMPLEMENTARY_OFFSET else COMPLEMENTARY_OFFSET
        val accentHue = (baseHue + offset) % FULL_CIRCLE

        val baseDescriptor = getBaseDescriptor(baseHue)
        val accentDescriptor = getAccentDescriptor(accentHue)

        return "$baseDescriptor & $accentDescriptor"
    }

    private fun getBaseDescriptor(baseHue: Float): String = when (baseHue) {
        in 0f..HUE_TERRACOTTA_MAX -> "Terracotta"
        in HUE_TERRACOTTA_MAX..HUE_DUNE_MAX -> "Dune"
        in HUE_DUNE_MAX..HUE_MOSS_MAX -> "Moss"
        in HUE_MOSS_MAX..HUE_CELADON_MAX -> "Celadon"
        in HUE_CELADON_MAX..HUE_GLACIAL_MAX -> "Glacial"
        in HUE_GLACIAL_MAX..HUE_BASALT_MAX -> "Basalt"
        in HUE_BASALT_MAX..HUE_TWILIGHT_MAX -> "Twilight"
        else -> "Cinnabar"
    }

    private fun getAccentDescriptor(accentHue: Float): String = when (accentHue) {
        in 0f..ACCENT_EMBER_MAX -> "Ember"
        in ACCENT_EMBER_MAX..ACCENT_OCHRE_MAX -> "Ochre"
        in ACCENT_OCHRE_MAX..ACCENT_VERDANT_MAX -> "Verdant"
        in ACCENT_VERDANT_MAX..ACCENT_CYAN_MAX -> "Cyan"
        in ACCENT_CYAN_MAX..ACCENT_ZENITH_MAX -> "Zenith"
        in ACCENT_ZENITH_MAX..ACCENT_AMETHYST_MAX -> "Amethyst"
        else -> "Coral"
    }
}
