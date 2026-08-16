package com.perspectivelive.wallpaper.data

import android.content.Context
import android.content.res.Configuration
import java.time.LocalDate

/**
 * Singleton provider for streamlined wallpaper themes with Light and Dark variants.
 */
object ColorSchemeProvider {

    const val DEFAULT_SCHEME_ID = "sage_garden"
    private const val OPACITY_30_PERCENT_MASK = 0x4D000000
    private const val RGB_MASK = 0x00FFFFFF

    // Iconic Colors
    private const val ICONIC_LIGHT_BG = 0xFFEAE7DC.toInt()
    private const val ICONIC_LIGHT_PAST = 0xFF8E8D8A.toInt()
    private const val ICONIC_LIGHT_CURRENT = 0xFFE85A4F.toInt()
    private const val ICONIC_LIGHT_FUTURE = 0x4D8E8D8A.toInt()
    private const val ICONIC_DARK_BG = 0xFF191816.toInt()
    private const val ICONIC_DARK_PAST = 0xFF8E8D8A.toInt()
    private const val ICONIC_DARK_CURRENT = 0xFFE85A4F.toInt()
    private const val ICONIC_DARK_FUTURE = 0x4D8E8D8A.toInt()

    // Nordic Minimal Colors
    private const val NORDIC_LIGHT_BG = 0xFFF8F9FA.toInt()
    private const val NORDIC_LIGHT_PAST = 0xFF7D9D9C.toInt()
    private const val NORDIC_LIGHT_CURRENT = 0xFFE76F51.toInt()
    private const val NORDIC_LIGHT_FUTURE = 0x4D7D9D9C.toInt()
    private const val NORDIC_DARK_BG = 0xFF131A19.toInt()
    private const val NORDIC_DARK_PAST = 0xFF6F9998.toInt()
    private const val NORDIC_DARK_CURRENT = 0xFFE76F51.toInt()
    private const val NORDIC_DARK_FUTURE = 0x4D6F9998.toInt()

    // Warm Sand Colors
    private const val SAND_LIGHT_BG = 0xFFFAF7F2.toInt()
    private const val SAND_LIGHT_PAST = 0xFFC9ADA7.toInt()
    private const val SAND_LIGHT_CURRENT = 0xFF9A8873.toInt()
    private const val SAND_LIGHT_FUTURE = 0x4DC9ADA7.toInt()
    private const val SAND_DARK_BG = 0xFF171311.toInt()
    private const val SAND_DARK_PAST = 0xFFA89690.toInt()
    private const val SAND_DARK_CURRENT = 0xFFD4B896.toInt()
    private const val SAND_DARK_FUTURE = 0x4DA89690.toInt()

    // Glacial Peak Colors
    private const val GLACIAL_LIGHT_BG = 0xFFF5F8FA.toInt()
    private const val GLACIAL_LIGHT_PAST = 0xFF7D94B0.toInt()
    private const val GLACIAL_LIGHT_CURRENT = 0xFFE57373.toInt()
    private const val GLACIAL_LIGHT_FUTURE = 0x4D7D94B0.toInt()
    private const val GLACIAL_DARK_BG = 0xFF0B1120.toInt()
    private const val GLACIAL_DARK_PAST = 0xFF5B9AA0.toInt()
    private const val GLACIAL_DARK_CURRENT = 0xFFFFA07A.toInt()
    private const val GLACIAL_DARK_FUTURE = 0x4D5B9AA0.toInt()

    // Rose Quartz Colors
    private const val ROSE_LIGHT_BG = 0xFFFFF9FA.toInt()
    private const val ROSE_LIGHT_PAST = 0xFFC5A3AB.toInt()
    private const val ROSE_LIGHT_CURRENT = 0xFF6D9DC5.toInt()
    private const val ROSE_LIGHT_FUTURE = 0x4DC5A3AB.toInt()
    private const val ROSE_DARK_BG = 0xFF1B1725.toInt()
    private const val ROSE_DARK_PAST = 0xFFA78BAA.toInt()
    private const val ROSE_DARK_CURRENT = 0xFFF2C14E.toInt()
    private const val ROSE_DARK_FUTURE = 0x4DA78BAA.toInt()

    // Monochrome Zen Colors
    private const val MONO_LIGHT_BG = 0xFFFAFAFA.toInt()
    private const val MONO_LIGHT_PAST = 0xFF666666.toInt()
    private const val MONO_LIGHT_CURRENT = 0xFF000000.toInt()
    private const val MONO_LIGHT_FUTURE = 0x4D666666.toInt()
    private const val MONO_DARK_BG = 0xFF121212.toInt()
    private const val MONO_DARK_PAST = 0xFF888888.toInt()
    private const val MONO_DARK_CURRENT = 0xFFFFFFFF.toInt()
    private const val MONO_DARK_FUTURE = 0x4D888888.toInt()

    // Health Steps Colors
    private const val HEALTH_STEPS_LIGHT_BG = 0xFFF1F8E9.toInt()
    private const val HEALTH_STEPS_LIGHT_PAST = 0xFFAED581.toInt()
    private const val HEALTH_STEPS_LIGHT_CURRENT = 0xFF558B2F.toInt()
    private const val HEALTH_STEPS_LIGHT_FUTURE = 0x4DAED581.toInt()
    private const val HEALTH_STEPS_DARK_BG = 0xFF152011.toInt()
    private const val HEALTH_STEPS_DARK_PAST = 0xFF558B2F.toInt()
    private const val HEALTH_STEPS_DARK_CURRENT = 0xFF8BC34A.toInt()
    private const val HEALTH_STEPS_DARK_FUTURE = 0x4D558B2F.toInt()

    // Health Calories Colors
    private const val HEALTH_CALS_LIGHT_BG = 0xFFFFF3E0.toInt()
    private const val HEALTH_CALS_LIGHT_PAST = 0xFFFFB74D.toInt()
    private const val HEALTH_CALS_LIGHT_CURRENT = 0xFFE65100.toInt()
    private const val HEALTH_CALS_LIGHT_FUTURE = 0x4DFFB74D.toInt()
    private const val HEALTH_CALS_DARK_BG = 0xFF22140A.toInt()
    private const val HEALTH_CALS_DARK_PAST = 0xFFE65100.toInt()
    private const val HEALTH_CALS_DARK_CURRENT = 0xFFFF9800.toInt()
    private const val HEALTH_CALS_DARK_FUTURE = 0x4DE65100.toInt()

    // Health Distance Colors
    private const val HEALTH_DIST_LIGHT_BG = 0xFFF3E5F5.toInt()
    private const val HEALTH_DIST_LIGHT_PAST = 0xFFBA68C8.toInt()
    private const val HEALTH_DIST_LIGHT_CURRENT = 0xFF6A1B9A.toInt()
    private const val HEALTH_DIST_LIGHT_FUTURE = 0x4DBA68C8.toInt()
    private const val HEALTH_DIST_DARK_BG = 0xFF1C0F24.toInt()
    private const val HEALTH_DIST_DARK_PAST = 0xFF8E24AA.toInt()
    private const val HEALTH_DIST_DARK_CURRENT = 0xFFBA68C8.toInt()
    private const val HEALTH_DIST_DARK_FUTURE = 0x4D8E24AA.toInt()

    // Health Sleep Colors
    private const val HEALTH_SLEEP_LIGHT_BG = 0xFFE3F2FD.toInt()
    private const val HEALTH_SLEEP_LIGHT_PAST = 0xFF64B5F6.toInt()
    private const val HEALTH_SLEEP_LIGHT_CURRENT = 0xFF1565C0.toInt()
    private const val HEALTH_SLEEP_LIGHT_FUTURE = 0x4D64B5F6.toInt()
    private const val HEALTH_SLEEP_DARK_BG = 0xFF0A1424.toInt()
    private const val HEALTH_SLEEP_DARK_PAST = 0xFF1976D2.toInt()
    private const val HEALTH_SLEEP_DARK_CURRENT = 0xFF64B5F6.toInt()
    private const val HEALTH_SLEEP_DARK_FUTURE = 0x4D1976D2.toInt()

    private val themes: List<WallpaperTheme> = listOf(
        // Iconic / Default
        WallpaperTheme(
            id = DEFAULT_SCHEME_ID,
            name = "Iconic",
            lightScheme = ColorScheme(
                id = DEFAULT_SCHEME_ID,
                name = "Iconic",
                backgroundColor = ICONIC_LIGHT_BG,
                pastYearsColor = ICONIC_LIGHT_PAST,
                currentYearColor = ICONIC_LIGHT_CURRENT,
                futureYearsColor = ICONIC_LIGHT_FUTURE,
                isDynamic = true,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = DEFAULT_SCHEME_ID,
                name = "Iconic",
                backgroundColor = ICONIC_DARK_BG,
                pastYearsColor = ICONIC_DARK_PAST,
                currentYearColor = ICONIC_DARK_CURRENT,
                futureYearsColor = ICONIC_DARK_FUTURE,
                isDynamic = true,
                isDark = true
            ),
            isRotatable = true
        ),

        // Health Connect Themes
        WallpaperTheme(
            id = "health_steps",
            name = "Steps Green",
            lightScheme = ColorScheme(
                id = "health_steps",
                name = "Steps Green",
                backgroundColor = HEALTH_STEPS_LIGHT_BG,
                pastYearsColor = HEALTH_STEPS_LIGHT_PAST,
                currentYearColor = HEALTH_STEPS_LIGHT_CURRENT,
                futureYearsColor = HEALTH_STEPS_LIGHT_FUTURE,
                isDynamic = false,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "health_steps",
                name = "Steps Green",
                backgroundColor = HEALTH_STEPS_DARK_BG,
                pastYearsColor = HEALTH_STEPS_DARK_PAST,
                currentYearColor = HEALTH_STEPS_DARK_CURRENT,
                futureYearsColor = HEALTH_STEPS_DARK_FUTURE,
                isDynamic = false,
                isDark = true
            ),
            isRotatable = false
        ),
        WallpaperTheme(
            id = "health_calories",
            name = "Vitality Orange",
            lightScheme = ColorScheme(
                id = "health_calories",
                name = "Vitality Orange",
                backgroundColor = HEALTH_CALS_LIGHT_BG,
                pastYearsColor = HEALTH_CALS_LIGHT_PAST,
                currentYearColor = HEALTH_CALS_LIGHT_CURRENT,
                futureYearsColor = HEALTH_CALS_LIGHT_FUTURE,
                isDynamic = false,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "health_calories",
                name = "Vitality Orange",
                backgroundColor = HEALTH_CALS_DARK_BG,
                pastYearsColor = HEALTH_CALS_DARK_PAST,
                currentYearColor = HEALTH_CALS_DARK_CURRENT,
                futureYearsColor = HEALTH_CALS_DARK_FUTURE,
                isDynamic = false,
                isDark = true
            ),
            isRotatable = false
        ),
        WallpaperTheme(
            id = "health_distance",
            name = "Distance Purple",
            lightScheme = ColorScheme(
                id = "health_distance",
                name = "Distance Purple",
                backgroundColor = HEALTH_DIST_LIGHT_BG,
                pastYearsColor = HEALTH_DIST_LIGHT_PAST,
                currentYearColor = HEALTH_DIST_LIGHT_CURRENT,
                futureYearsColor = HEALTH_DIST_LIGHT_FUTURE,
                isDynamic = false,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "health_distance",
                name = "Distance Purple",
                backgroundColor = HEALTH_DIST_DARK_BG,
                pastYearsColor = HEALTH_DIST_DARK_PAST,
                currentYearColor = HEALTH_DIST_DARK_CURRENT,
                futureYearsColor = HEALTH_DIST_DARK_FUTURE,
                isDynamic = false,
                isDark = true
            ),
            isRotatable = false
        ),
        WallpaperTheme(
            id = "health_sleep",
            name = "Deep Sleep Blue",
            lightScheme = ColorScheme(
                id = "health_sleep",
                name = "Deep Sleep Blue",
                backgroundColor = HEALTH_SLEEP_LIGHT_BG,
                pastYearsColor = HEALTH_SLEEP_LIGHT_PAST,
                currentYearColor = HEALTH_SLEEP_LIGHT_CURRENT,
                futureYearsColor = HEALTH_SLEEP_LIGHT_FUTURE,
                isDynamic = false,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "health_sleep",
                name = "Deep Sleep Blue",
                backgroundColor = HEALTH_SLEEP_DARK_BG,
                pastYearsColor = HEALTH_SLEEP_DARK_PAST,
                currentYearColor = HEALTH_SLEEP_DARK_CURRENT,
                futureYearsColor = HEALTH_SLEEP_DARK_FUTURE,
                isDynamic = false,
                isDark = true
            ),
            isRotatable = false
        ),

        // Curated Palettes
        WallpaperTheme(
            id = "nordic_minimal",
            name = "Nordic Minimal",
            lightScheme = ColorScheme(
                id = "nordic_minimal",
                name = "Nordic Minimal",
                backgroundColor = NORDIC_LIGHT_BG,
                pastYearsColor = NORDIC_LIGHT_PAST,
                currentYearColor = NORDIC_LIGHT_CURRENT,
                futureYearsColor = NORDIC_LIGHT_FUTURE,
                isDynamic = true,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "nordic_minimal",
                name = "Nordic Minimal",
                backgroundColor = NORDIC_DARK_BG,
                pastYearsColor = NORDIC_DARK_PAST,
                currentYearColor = NORDIC_DARK_CURRENT,
                futureYearsColor = NORDIC_DARK_FUTURE,
                isDynamic = true,
                isDark = true
            ),
            isRotatable = true
        ),
        WallpaperTheme(
            id = "warm_sand",
            name = "Warm Sand",
            lightScheme = ColorScheme(
                id = "warm_sand",
                name = "Warm Sand",
                backgroundColor = SAND_LIGHT_BG,
                pastYearsColor = SAND_LIGHT_PAST,
                currentYearColor = SAND_LIGHT_CURRENT,
                futureYearsColor = SAND_LIGHT_FUTURE,
                isDynamic = true,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "warm_sand",
                name = "Warm Sand",
                backgroundColor = SAND_DARK_BG,
                pastYearsColor = SAND_DARK_PAST,
                currentYearColor = SAND_DARK_CURRENT,
                futureYearsColor = SAND_DARK_FUTURE,
                isDynamic = true,
                isDark = true
            ),
            isRotatable = true
        ),
        WallpaperTheme(
            id = "glacial_peak",
            name = "Glacial Peak",
            lightScheme = ColorScheme(
                id = "glacial_peak",
                name = "Glacial Peak",
                backgroundColor = GLACIAL_LIGHT_BG,
                pastYearsColor = GLACIAL_LIGHT_PAST,
                currentYearColor = GLACIAL_LIGHT_CURRENT,
                futureYearsColor = GLACIAL_LIGHT_FUTURE,
                isDynamic = true,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "glacial_peak",
                name = "Glacial Peak",
                backgroundColor = GLACIAL_DARK_BG,
                pastYearsColor = GLACIAL_DARK_PAST,
                currentYearColor = GLACIAL_DARK_CURRENT,
                futureYearsColor = GLACIAL_DARK_FUTURE,
                isDynamic = true,
                isDark = true
            ),
            isRotatable = true
        ),
        WallpaperTheme(
            id = "rose_quartz",
            name = "Rose Quartz",
            lightScheme = ColorScheme(
                id = "rose_quartz",
                name = "Rose Quartz",
                backgroundColor = ROSE_LIGHT_BG,
                pastYearsColor = ROSE_LIGHT_PAST,
                currentYearColor = ROSE_LIGHT_CURRENT,
                futureYearsColor = ROSE_LIGHT_FUTURE,
                isDynamic = true,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "rose_quartz",
                name = "Rose Quartz",
                backgroundColor = ROSE_DARK_BG,
                pastYearsColor = ROSE_DARK_PAST,
                currentYearColor = ROSE_DARK_CURRENT,
                futureYearsColor = ROSE_DARK_FUTURE,
                isDynamic = true,
                isDark = true
            ),
            isRotatable = true
        ),
        WallpaperTheme(
            id = "monochrome_zen",
            name = "Monochrome Zen",
            lightScheme = ColorScheme(
                id = "monochrome_zen",
                name = "Monochrome Zen",
                backgroundColor = MONO_LIGHT_BG,
                pastYearsColor = MONO_LIGHT_PAST,
                currentYearColor = MONO_LIGHT_CURRENT,
                futureYearsColor = MONO_LIGHT_FUTURE,
                isDynamic = true,
                isDark = false
            ),
            darkScheme = ColorScheme(
                id = "monochrome_zen",
                name = "Monochrome Zen",
                backgroundColor = MONO_DARK_BG,
                pastYearsColor = MONO_DARK_PAST,
                currentYearColor = MONO_DARK_CURRENT,
                futureYearsColor = MONO_DARK_FUTURE,
                isDynamic = true,
                isDark = true
            ),
            isRotatable = true
        )
    )

    /**
     * Retrieves a scheme variant by theme ID, respecting light/dark mode and custom colors.
     */
    fun getScheme(
        id: String,
        isDarkMode: Boolean = false,
        prefsManager: PreferencesManager? = null
    ): ColorScheme {
        if (id == "custom" && prefsManager != null) {
            val customColors = prefsManager.getCustomColors()
            if (customColors != null) {
                return createCustomColorScheme(customColors, isDarkMode)
            }
        }

        val theme = themes.find { it.id == id } ?: themes.first()
        return theme.getScheme(isDarkMode)
    }

    /**
     * Overload to retrieve a scheme based on Android Context system theme.
     */
    fun getScheme(
        id: String,
        context: Context,
        prefsManager: PreferencesManager? = null
    ): ColorScheme {
        return getScheme(id, isSystemDarkMode(context), prefsManager)
    }

    /**
     * Checks if the system is in Dark Theme / Night Mode.
     */
    fun isSystemDarkMode(context: Context): Boolean {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Returns all available wallpaper themes.
     */
    fun getAllThemes(): List<WallpaperTheme> = themes

    /**
     * Returns all available color schemes resolved for the specified light/dark mode.
     */
    fun getAllSchemes(isDarkMode: Boolean = false): List<ColorScheme> {
        return themes.map { it.getScheme(isDarkMode) }
    }

    /**
     * Calculates the active scheme ID for daily rotation on a given date.
     * Deterministically cycles through rotatable themes.
     */
    fun getRotatedSchemeId(date: LocalDate = LocalDate.now()): String {
        val rotatableThemes = themes.filter { it.isRotatable }
        if (rotatableThemes.isEmpty()) return DEFAULT_SCHEME_ID
        val dayIndex = Math.floorMod(date.toEpochDay(), rotatableThemes.size.toLong()).toInt()
        return rotatableThemes[dayIndex].id
    }

    /**
     * Creates a ColorScheme from a CustomColorScheme.
     * Future years color is automatically set to 30% opacity of pastFutureColor.
     */
    fun createCustomColorScheme(custom: CustomColorScheme, isDarkMode: Boolean = false): ColorScheme {
        return ColorScheme(
            id = "custom",
            name = custom.name,
            backgroundColor = custom.backgroundColor,
            pastYearsColor = custom.pastFutureColor,
            currentYearColor = custom.currentColor,
            futureYearsColor = (custom.pastFutureColor and RGB_MASK) or OPACITY_30_PERCENT_MASK,
            isDynamic = false,
            isCustom = true,
            isDark = isDarkMode
        )
    }
}
