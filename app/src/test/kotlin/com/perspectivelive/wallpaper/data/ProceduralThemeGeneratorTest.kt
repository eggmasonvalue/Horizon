package com.perspectivelive.wallpaper.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProceduralThemeGeneratorTest {

    @Test
    fun testDeterministicThemeGenerationForSameDate() {
        val date = LocalDate.of(2026, 9, 6)
        val theme1 = ProceduralThemeGenerator.generateTheme(date)
        val theme2 = ProceduralThemeGenerator.generateTheme(date)

        assertEquals(theme1.id, theme2.id)
        assertEquals(theme1.name, theme2.name)
        assertEquals(theme1.lightScheme.backgroundColor, theme2.lightScheme.backgroundColor)
        assertEquals(theme1.lightScheme.pastYearsColor, theme2.lightScheme.pastYearsColor)
        assertEquals(theme1.lightScheme.currentYearColor, theme2.lightScheme.currentYearColor)
        assertEquals(theme1.darkScheme.backgroundColor, theme2.darkScheme.backgroundColor)
        assertEquals(theme1.darkScheme.currentYearColor, theme2.darkScheme.currentYearColor)
    }

    @Test
    fun testConsecutiveDaysProduceDistinctPalettes() {
        val date1 = LocalDate.of(2026, 9, 6)
        val date2 = LocalDate.of(2026, 9, 7)

        val theme1 = ProceduralThemeGenerator.generateTheme(date1)
        val theme2 = ProceduralThemeGenerator.generateTheme(date2)

        assertNotEquals("Consecutive days should have distinct light backgrounds",
            theme1.lightScheme.backgroundColor, theme2.lightScheme.backgroundColor)
        assertNotEquals("Consecutive days should have distinct dark backgrounds",
            theme1.darkScheme.backgroundColor, theme2.darkScheme.backgroundColor)
        assertNotEquals("Consecutive days should have distinct accents",
            theme1.lightScheme.currentYearColor, theme2.lightScheme.currentYearColor)
    }

    @Test
    fun testLightAndDarkSchemeProperties() {
        val date = LocalDate.of(2026, 9, 6)
        val theme = ProceduralThemeGenerator.generateTheme(date)

        assertFalse("Light scheme should have isDark = false", theme.lightScheme.isDark)
        assertTrue("Dark scheme should have isDark = true", theme.darkScheme.isDark)
        assertTrue("Light scheme should be dynamic", theme.lightScheme.isDynamic)
        assertTrue("Dark scheme should be dynamic", theme.darkScheme.isDynamic)
        assertFalse("Light scheme should not be custom", theme.lightScheme.isCustom)
        assertFalse("Theme should not be marked rotatable in static pool", theme.isRotatable)
        assertEquals(ProceduralThemeGenerator.THEME_ID, theme.id)
    }

    @Test
    fun testFutureDotsHaveThirtyPercentAlphaMask() {
        val date = LocalDate.of(2026, 9, 6)
        val theme = ProceduralThemeGenerator.generateTheme(date)

        val expectedLightFuture = (theme.lightScheme.pastYearsColor and 0x00FFFFFF) or 0x4D000000
        val expectedDarkFuture = (theme.darkScheme.pastYearsColor and 0x00FFFFFF) or 0x4D000000

        assertEquals(expectedLightFuture, theme.lightScheme.futureYearsColor)
        assertEquals(expectedDarkFuture, theme.darkScheme.futureYearsColor)
    }

    @Test
    fun testMoodDescriptorStructure() {
        val date = LocalDate.of(2026, 9, 6)
        val mood = ProceduralThemeGenerator.getMoodName(date)

        assertNotNull(mood)
        assertTrue("Mood name should contain separator ' & '", mood.contains(" & "))
        val parts = mood.split(" & ")
        assertEquals(2, parts.size)
        assertTrue("Base descriptor should not be empty", parts[0].isNotBlank())
        assertTrue("Accent descriptor should not be empty", parts[1].isNotBlank())
    }
}
