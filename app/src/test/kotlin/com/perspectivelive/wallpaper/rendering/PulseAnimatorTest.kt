package com.perspectivelive.wallpaper.rendering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseAnimatorTest {

    @Test
    fun testPacing30Fps() {
        assertEquals(30, PulseAnimator.TARGET_FPS)
        assertEquals(1000L / 30L, PulseAnimator.FRAME_DURATION_MS)
    }

    @Test
    fun testOpacityBounds() {
        val animator = PulseAnimator(cycleDurationMs = 2000L)
        val opacity = animator.getCurrentOpacity()
        assertTrue(
            "Opacity should be >= MIN_OPACITY (0.5), was $opacity",
            opacity >= PulseAnimator.MIN_OPACITY
        )
        assertTrue(
            "Opacity should be <= MAX_OPACITY (1.0), was $opacity",
            opacity <= PulseAnimator.MAX_OPACITY
        )
    }

    @Test
    fun testReset() {
        val animator = PulseAnimator(cycleDurationMs = 2000L)
        animator.reset()
        val opacity = animator.getCurrentOpacity()
        assertTrue(opacity in PulseAnimator.MIN_OPACITY..PulseAnimator.MAX_OPACITY)
    }
}
