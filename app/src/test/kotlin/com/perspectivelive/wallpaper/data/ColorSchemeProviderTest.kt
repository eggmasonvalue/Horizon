package com.perspectivelive.wallpaper.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ColorSchemeProviderTest {

    @Test
    fun testDefaultSchemeIsIconic() {
        val lightScheme = ColorSchemeProvider.getScheme("sage_garden", isDarkMode = false)
        val darkScheme = ColorSchemeProvider.getScheme("sage_garden", isDarkMode = true)

        assertEquals("Iconic", lightScheme.name)
        assertEquals("Iconic", darkScheme.name)
        assertFalse(lightScheme.isDark)
        assertTrue(darkScheme.isDark)
        assertNotEquals(lightScheme.backgroundColor, darkScheme.backgroundColor)
    }

    @Test
    fun testAllThemesHaveDistinctLightAndDarkVariants() {
        val themes = ColorSchemeProvider.getAllThemes()
        assertTrue("Themes collection should not be empty", themes.isNotEmpty())

        for (theme in themes) {
            val light = theme.getScheme(isDarkMode = false)
            val dark = theme.getScheme(isDarkMode = true)

            assertNotNull(light)
            assertNotNull(dark)
            assertEquals(theme.name, light.name)
            assertEquals(theme.name, dark.name)
            assertFalse("${theme.name} light scheme should not be dark", light.isDark)
            assertTrue("${theme.name} dark scheme should be dark", dark.isDark)
            assertNotEquals(
                "${theme.name} light and dark backgrounds should differ",
                light.backgroundColor,
                dark.backgroundColor
            )
        }
    }


    @Test
    fun testDailyRotationCyclesDeterministically() {
        val date1 = LocalDate.of(2026, 8, 16)
        val date2 = LocalDate.of(2026, 8, 17)
        val date3 = LocalDate.of(2026, 8, 18)

        val schemeId1 = ColorSchemeProvider.getRotatedSchemeId(date1)
        val schemeId2 = ColorSchemeProvider.getRotatedSchemeId(date2)
        val schemeId3 = ColorSchemeProvider.getRotatedSchemeId(date3)

        assertNotNull(schemeId1)
        assertNotNull(schemeId2)
        assertNotNull(schemeId3)

        // Consecutive days should yield rotated themes
        assertNotEquals("Consecutive days should have different themes", schemeId1, schemeId2)

        // Same date should always yield the exact same theme
        assertEquals(schemeId1, ColorSchemeProvider.getRotatedSchemeId(date1))
    }

    @Test
    fun testCustomColorSchemeCreation() {
        val custom = CustomColorScheme(
            name = "My Custom Aura",
            backgroundColor = 0xFF112233.toInt(),
            pastFutureColor = 0xFF445566.toInt(),
            currentColor = 0xFF778899.toInt()
        )

        val lightCustom = ColorSchemeProvider.createCustomColorScheme(custom, isDarkMode = false)
        val darkCustom = ColorSchemeProvider.createCustomColorScheme(custom, isDarkMode = true)

        assertEquals("My Custom Aura", lightCustom.name)
        assertTrue(lightCustom.isCustom)
        assertFalse(lightCustom.isDark)
        assertTrue(darkCustom.isDark)
        assertEquals(0xFF112233.toInt(), lightCustom.backgroundColor)
        assertEquals(0xFF445566.toInt(), lightCustom.pastYearsColor)
        assertEquals(0xFF778899.toInt(), lightCustom.currentYearColor)
    }

    @Test
    fun testFallbackForUnknownSchemeId() {
        val fallback = ColorSchemeProvider.getScheme("completely_unknown_scheme_id", isDarkMode = false)
        assertEquals("Iconic", fallback.name)
    }

    @Test
    fun testFourteenDayRotationCycleHasFourteenDistinctThemes() {
        val startDate = LocalDate.of(2026, 9, 1)
        val rotatedIds = (0 until 14).map { offset ->
            ColorSchemeProvider.getRotatedSchemeId(startDate.plusDays(offset.toLong()))
        }.toSet()

        assertEquals("14 consecutive days should yield exactly 14 unique themes", 14, rotatedIds.size)

        // Day 15 should repeat Day 1
        val day1 = ColorSchemeProvider.getRotatedSchemeId(startDate)
        val day15 = ColorSchemeProvider.getRotatedSchemeId(startDate.plusDays(14))
        assertEquals("Day 15 should cycle back to Day 1", day1, day15)
    }

    @Test
    fun testAtmosphereSchemeResolution() {
        val date = LocalDate.of(2026, 9, 6)
        val lightAtmosphere = ColorSchemeProvider.getScheme(
            id = ColorSchemeProvider.ATMOSPHERE_SCHEME_ID,
            isDarkMode = false,
            date = date
        )
        val darkAtmosphere = ColorSchemeProvider.getScheme(
            id = ColorSchemeProvider.ATMOSPHERE_SCHEME_ID,
            isDarkMode = true,
            date = date
        )

        assertEquals("Atmosphere", lightAtmosphere.name)
        assertEquals("Atmosphere", darkAtmosphere.name)
        assertFalse(lightAtmosphere.isDark)
        assertTrue(darkAtmosphere.isDark)
        assertNotEquals(lightAtmosphere.backgroundColor, darkAtmosphere.backgroundColor)
    }

    @Test
    fun testTotalThemesCount() {
        val allThemes = ColorSchemeProvider.getAllThemes()
        // 14 curated + 4 health connect + 1 atmosphere = 19
        assertEquals(19, allThemes.size)
    }
}
