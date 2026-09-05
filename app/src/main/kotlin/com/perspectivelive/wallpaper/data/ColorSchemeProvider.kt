package com.perspectivelive.wallpaper.data

import android.content.Context
import android.content.res.Configuration
import java.time.LocalDate

/**
 * Singleton provider for streamlined wallpaper themes with Light and Dark variants.
 */
object ColorSchemeProvider {

    const val DEFAULT_SCHEME_ID = "sage_garden"
    const val ATMOSPHERE_SCHEME_ID = ProceduralThemeGenerator.THEME_ID
    private const val OPACITY_30_PERCENT_MASK = 0x4D000000
    private const val RGB_MASK = 0x00FFFFFF

    /**
     * Retrieves a scheme variant by theme ID, respecting light/dark mode and custom colors.
     */
    fun getScheme(
        id: String,
        isDarkMode: Boolean = false,
        prefsManager: PreferencesManager? = null,
        date: LocalDate = LocalDate.now()
    ): ColorScheme {
        if (id == ATMOSPHERE_SCHEME_ID) {
            return ProceduralThemeGenerator.generateTheme(date).getScheme(isDarkMode)
        }
        if (id == "custom" && prefsManager != null) {
            val customColors = prefsManager.getCustomColors()
            if (customColors != null) {
                return createCustomColorScheme(customColors, isDarkMode)
            }
        }

        val theme = CuratedThemes.list.find { it.id == id } ?: CuratedThemes.list.first()
        return theme.getScheme(isDarkMode)
    }

    /**
     * Overload to retrieve a scheme based on Android Context system theme.
     */
    fun getScheme(
        id: String,
        context: Context,
        prefsManager: PreferencesManager? = null,
        date: LocalDate = LocalDate.now()
    ): ColorScheme {
        return getScheme(id, isSystemDarkMode(context), prefsManager, date)
    }

    /**
     * Checks if the system is in Dark Theme / Night Mode.
     */
    fun isSystemDarkMode(context: Context): Boolean {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Returns all available wallpaper themes including the dynamic Atmosphere theme.
     */
    fun getAllThemes(date: LocalDate = LocalDate.now()): List<WallpaperTheme> =
        CuratedThemes.list + ProceduralThemeGenerator.generateTheme(date)

    /**
     * Returns all available color schemes resolved for the specified light/dark mode.
     */
    fun getAllSchemes(isDarkMode: Boolean = false, date: LocalDate = LocalDate.now()): List<ColorScheme> {
        return getAllThemes(date).map { it.getScheme(isDarkMode) }
    }

    /**
     * Calculates the active scheme ID for daily rotation on a given date.
     * Deterministically cycles through rotatable curated themes.
     */
    fun getRotatedSchemeId(date: LocalDate = LocalDate.now()): String {
        val rotatableThemes = CuratedThemes.list.filter { it.isRotatable }
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
