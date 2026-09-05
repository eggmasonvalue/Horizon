package com.perspectivelive.wallpaper.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OklchColorTest {

    @Test
    fun testBlackAndWhiteLimits() {
        val black = OklchColor.oklchToArgb(0f, 0f, 0f)
        val white = OklchColor.oklchToArgb(1f, 0f, 0f)

        assertEquals(0xFF000000.toInt(), black)
        assertEquals(0xFFFFFFFF.toInt(), white)
    }

    @Test
    fun testNeutralAchromaticGreysHaveEqualRgbChannels() {
        val grey = OklchColor.oklchToArgb(0.5f, 0f, 0f)
        val red = (grey shr 16) and 0xFF
        val green = (grey shr 8) and 0xFF
        val blue = grey and 0xFF

        assertEquals(red, green)
        assertEquals(green, blue)
        assertTrue("Mid grey channel should be non-zero and non-255", red in 90..150)
    }

    @Test
    fun testAlphaChannelPreservation() {
        val halfAlpha = OklchColor.oklchToArgb(0.8f, 0.05f, 120f, alpha = 128)
        val extractedAlpha = (halfAlpha ushr 24) and 0xFF
        assertEquals(128, extractedAlpha)
    }

    @Test
    fun testHueAngleWrapping() {
        val colorAt30 = OklchColor.oklchToArgb(0.6f, 0.1f, 30f)
        val colorAt390 = OklchColor.oklchToArgb(0.6f, 0.1f, 390f)
        val colorAtNegative330 = OklchColor.oklchToArgb(0.6f, 0.1f, -330f)

        assertEquals(colorAt30, colorAt390)
        assertEquals(colorAt30, colorAtNegative330)
    }

    @Test
    fun testLightnessMonotonicity() {
        val dark = OklchColor.oklchToArgb(0.2f, 0.05f, 180f)
        val light = OklchColor.oklchToArgb(0.8f, 0.05f, 180f)

        val darkR = (dark shr 16) and 0xFF
        val darkG = (dark shr 8) and 0xFF
        val darkB = dark and 0xFF

        val lightR = (light shr 16) and 0xFF
        val lightG = (light shr 8) and 0xFF
        val lightB = light and 0xFF

        val darkLuminance = 0.299 * darkR + 0.587 * darkG + 0.114 * darkB
        val lightLuminance = 0.299 * lightR + 0.587 * lightG + 0.114 * lightB

        assertTrue("Higher L must produce higher perceived luminance", lightLuminance > darkLuminance)
    }
}
