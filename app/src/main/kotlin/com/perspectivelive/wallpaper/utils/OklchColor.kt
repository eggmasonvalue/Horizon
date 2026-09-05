package com.perspectivelive.wallpaper.utils

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/**
 * Utility for perceptual color manipulation using the Oklch color space.
 * Oklch represents colors via Lightness (L), Chroma (C), and Hue (h in degrees),
 * providing perceptual uniformity for algorithmic palette generation.
 */
object OklchColor {

    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val FULL_CIRCLE_DEGREES = 360.0
    private const val GAMMA_THRESHOLD = 0.0031308
    private const val GAMMA_FACTOR = 12.92
    private const val GAMMA_EXPONENT = 1.0 / 2.4
    private const val GAMMA_SCALE = 1.055
    private const val GAMMA_OFFSET = 0.055
    private const val COLOR_BYTE_MAX = 255.0
    private const val MAX_BYTE = 255
    private const val HALF_ROUNDING = 0.5

    private const val ALPHA_SHIFT = 24
    private const val RED_SHIFT = 16
    private const val GREEN_SHIFT = 8

    // Oklab to LMS matrix coefficients
    private const val A_TO_L = 0.3963377774
    private const val B_TO_L = 0.2158037573
    private const val A_TO_M = 0.1055613458
    private const val B_TO_M = 0.0638541728
    private const val A_TO_S = 0.0894841775
    private const val B_TO_S = 1.2914855480

    // LMS to linear sRGB matrix coefficients
    private const val L_TO_R = 4.0767416621
    private const val M_TO_R = 3.3077115913
    private const val S_TO_R = 0.2309699292

    private const val L_TO_G = 1.2684380046
    private const val M_TO_G = 2.6097574011
    private const val S_TO_G = 0.3413193965

    private const val L_TO_B = 0.0041960863
    private const val M_TO_B = 0.7034186147
    private const val S_TO_B = 1.7076147010

    /**
     * Converts Oklch coordinates (Lightness, Chroma, Hue) to a 32-bit ARGB color.
     *
     * @param lightness Perceptual lightness in range [0.0, 1.0]
     * @param chroma Chroma (saturation) in range [0.0, ~0.4]
     * @param hueDegrees Hue angle in range [0.0, 360.0]
     * @param alpha Alpha channel in range [0, 255] (default 255)
     * @return ARGB packed integer color
     */
    fun oklchToArgb(
        lightness: Float,
        chroma: Float,
        hueDegrees: Float,
        alpha: Int = MAX_BYTE
    ): Int {
        val l = lightness.toDouble().coerceIn(0.0, 1.0)
        val c = max(0.0, chroma.toDouble())
        val hRad = ((hueDegrees % FULL_CIRCLE_DEGREES + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES) * DEG_TO_RAD

        val a = c * cos(hRad)
        val b = c * sin(hRad)

        val lLms = (l + A_TO_L * a + B_TO_L * b).let { it * it * it }
        val mLms = (l - A_TO_M * a - B_TO_M * b).let { it * it * it }
        val sLms = (l - A_TO_S * a - B_TO_S * b).let { it * it * it }

        val rLin = +L_TO_R * lLms - M_TO_R * mLms + S_TO_R * sLms
        val gLin = -L_TO_G * lLms + M_TO_G * mLms - S_TO_G * sLms
        val bLin = -L_TO_B * lLms - M_TO_B * mLms + S_TO_B * sLms

        val red = linearToSrgb(rLin)
        val green = linearToSrgb(gLin)
        val blue = linearToSrgb(bLin)

        val rByte = (red * COLOR_BYTE_MAX + HALF_ROUNDING).toInt().coerceIn(0, MAX_BYTE)
        val gByte = (green * COLOR_BYTE_MAX + HALF_ROUNDING).toInt().coerceIn(0, MAX_BYTE)
        val bByte = (blue * COLOR_BYTE_MAX + HALF_ROUNDING).toInt().coerceIn(0, MAX_BYTE)
        val aByte = alpha.coerceIn(0, MAX_BYTE)

        return (aByte shl ALPHA_SHIFT) or (rByte shl RED_SHIFT) or (gByte shl GREEN_SHIFT) or bByte
    }

    private fun linearToSrgb(value: Double): Double {
        val clamped = value.coerceIn(0.0, 1.0)
        return if (clamped <= GAMMA_THRESHOLD) {
            GAMMA_FACTOR * clamped
        } else {
            GAMMA_SCALE * clamped.pow(GAMMA_EXPONENT) - GAMMA_OFFSET
        }
    }
}
