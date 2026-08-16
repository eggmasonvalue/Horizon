package com.perspectivelive.wallpaper.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.Layout
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import com.perspectivelive.wallpaper.data.ColorScheme
import com.perspectivelive.wallpaper.data.GridConfig
import com.perspectivelive.wallpaper.data.GridState
import com.perspectivelive.wallpaper.utils.ColorUtils
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Renders the grid on a Canvas.
 * Optimized with strategy pattern for shape drawing.
 */
class CanvasRenderer(
    private var gridState: GridState,
    val colorScheme: ColorScheme,
    private val screenWidth: Int,
    private val screenHeight: Int,
    customTypeface: Typeface? = null
) {
    companion object {
        private const val DEFAULT_CONTAINER_PADDING_SCALE = 0.05f
        private const val DEFAULT_HEALTH_GOAL = 10000f
        private const val MIN_UNIT_SCALE = 0.5f
        private const val MAX_UNIT_SCALE = 1.0f
        private const val TEXT_SIZE_SCALE = 0.40f
        private const val TEXT_OVERLAY_ALPHA = 200
        private const val BG_CACHE_INTERVAL_MS = 10_000L
        private const val MAX_SINGLE_LINE_WIDTH = 10000
        private const val SAFE_BOUNDS_SCALE = 0.707f
        private const val PROGRESS_BASE_ALPHA_RATIO = 0.2f
        private const val FULL_ALPHA = 255
        private const val THOUSAND_STEPS = 1000
        private const val RELATIVE_SIZE_SPAN_PROPORTION = 0.6f
        private const val SQUIRCLE_CORNER_RATIO = 0.22f
        private const val ROUNDED_CORNER_RATIO = 0.15f
        private const val RHOMBUS_SCALE = 0.707f
        private const val RHOMBUS_ROTATION_DEGREES = 45f
    }

    private var gridConfig: GridConfig
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Style settings
    private var unitShapeId: String = "rounded_square"
    private var unitScale: Float = 1.0f
    private var containerPaddingScale: Float = DEFAULT_CONTAINER_PADDING_SCALE

    private var shapeDrawer: ShapeDrawer = RoundedSquareDrawer()

    // Cache background color to reduce allocations in render loop
    private var lastBgUpdateMillis: Long = 0L
    private var cachedBgColor: Int = Color.BLACK

    // Health Connect formatting cache
    private var healthCache: Map<LocalDate, Float>? = null
    private var healthGoal: Float = DEFAULT_HEALTH_GOAL
    private var healthMetric: String = "NONE"
    private var showStatOverlay: Boolean = false
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = customTypeface ?: Typeface.DEFAULT_BOLD
    }

    init {
        gridConfig = calculateGrid()
        updateShapeDrawer()
    }

    fun updateHealthData(
        metric: String,
        goal: Float,
        showOverlay: Boolean,
        cache: Map<LocalDate, Float>?
    ) {
        this.healthMetric = metric
        this.healthGoal = goal
        this.showStatOverlay = showOverlay
        this.healthCache = cache
    }

    private fun calculateGrid(): GridConfig {
        return GridCalculator.calculateGridLayout(
            totalDots = gridState.totalItems,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            marginPercent = containerPaddingScale
        )
    }

    fun render(canvas: Canvas, currentItemOpacity: Float) {
        updateBackgroundCache()
        canvas.drawColor(cachedBgColor)

        var dotIndex = 0
        val effectiveSize = gridConfig.dotSize * unitScale.coerceIn(MIN_UNIT_SCALE, MAX_UNIT_SCALE)
        val offset = (gridConfig.dotSize - effectiveSize) / 2f
        val startX = gridConfig.offsetX
        val startY = gridConfig.offsetY
        val cellSize = gridConfig.dotSize + gridConfig.spacing

        // Precompute text styling if overlay is enabled
        if (showStatOverlay && healthMetric != "NONE") {
            textPaint.textSize = effectiveSize * TEXT_SIZE_SCALE
            textPaint.color = cachedBgColor
            textPaint.alpha = TEXT_OVERLAY_ALPHA
        }

        for (row in 0 until gridConfig.rows) {
            for (col in 0 until gridConfig.columns) {
                if (dotIndex >= gridState.totalItems) break

                val x = startX + col * cellSize + offset
                val y = startY + row * cellSize + offset
                val itemDate = gridState.startDate.plusDays(dotIndex.toLong())

                val params = RenderItemParams(canvas, dotIndex, x, y, effectiveSize, itemDate, currentItemOpacity)
                drawGridItem(params)

                dotIndex++
            }
        }
    }

    private fun updateBackgroundCache() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBgUpdateMillis > BG_CACHE_INTERVAL_MS || lastBgUpdateMillis == 0L) {
            val hour = LocalDateTime.now().hour
            cachedBgColor = if (colorScheme.isDynamic) {
                ColorUtils.adaptBackgroundForTimeOfDay(colorScheme.backgroundColor, hour)
            } else {
                colorScheme.backgroundColor
            }
            lastBgUpdateMillis = currentTime
        }
    }

    private fun drawGridItem(p: RenderItemParams) {
        val color = when {
            p.dotIndex < gridState.pastItems -> colorScheme.pastYearsColor
            p.dotIndex == gridState.currentIndex -> colorScheme.currentYearColor
            else -> colorScheme.futureYearsColor
        }

        paint.color = color
        var drawnText: CharSequence? = null

        if (p.dotIndex == gridState.currentIndex) {
            paint.alpha = (FULL_ALPHA * p.currentItemOpacity).toInt()
            if (showStatOverlay && healthMetric != "NONE" && healthCache != null) {
                healthCache?.get(p.itemDate)?.let { value ->
                    drawnText = formatHealthText(value, healthMetric)
                }
            }
        } else if (p.dotIndex < gridState.currentIndex && healthMetric != "NONE" && healthCache != null) {
            val value = healthCache?.get(p.itemDate) ?: 0f
            val progress = (value / healthGoal).coerceIn(0f, 1f)

            val originalAlpha = Color.alpha(color)
            val baseAlpha = (originalAlpha * PROGRESS_BASE_ALPHA_RATIO).toInt()
            paint.alpha = baseAlpha + ((originalAlpha - baseAlpha) * progress).toInt()

            if (showStatOverlay) drawnText = formatHealthText(value, healthMetric)
        } else {
            paint.alpha = Color.alpha(color)
        }

        shapeDrawer.draw(p.canvas, p.x, p.y, p.size, paint)

        drawnText?.let { text ->
            val maxSingleLineWidth = MAX_SINGLE_LINE_WIDTH

            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxSingleLineWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(false)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(text, textPaint, maxSingleLineWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
            }

            var actualTextWidth = 0f
            for (i in 0 until staticLayout.lineCount) {
                val lineWidth = staticLayout.getLineWidth(i)
                if (lineWidth > actualTextWidth) {
                    actualTextWidth = lineWidth
                }
            }

            val actualTextHeight = staticLayout.height.toFloat()
            val safeBounds = p.size * SAFE_BOUNDS_SCALE

            val scaleX = if (actualTextWidth > safeBounds) safeBounds / actualTextWidth else 1.0f
            val scaleY = if (actualTextHeight > safeBounds) safeBounds / actualTextHeight else 1.0f
            val scaleFactor = kotlin.math.min(scaleX, scaleY)

            p.canvas.save()
            p.canvas.translate(p.x + p.size / 2f, p.y + p.size / 2f)

            if (scaleFactor < 1.0f) {
                p.canvas.scale(scaleFactor, scaleFactor)
            }

            p.canvas.translate(-actualTextWidth / 2f, -actualTextHeight / 2f)

            staticLayout.draw(p.canvas)
            p.canvas.restore()
        }
    }

    private fun formatHealthText(value: Float, metric: String): CharSequence {
        val (text, suffix) = when (metric) {
            "STEPS" -> {
                if (value >= THOUSAND_STEPS) {
                    Pair(String.format("%.1f", value / THOUSAND_STEPS.toFloat()), "k")
                } else {
                    Pair(value.toInt().toString(), "")
                }
            }
            "CALORIES" -> Pair(value.toInt().toString(), "kcal")
            "DISTANCE" -> Pair(String.format("%.1f", value), "km")
            "SLEEP" -> Pair(String.format("%.1f", value), "h")
            else -> Pair(value.toInt().toString(), "")
        }

        if (suffix.isEmpty()) return text

        val spannable = SpannableString(text + suffix)
        spannable.setSpan(
            RelativeSizeSpan(RELATIVE_SIZE_SPAN_PROPORTION),
            text.length,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    fun updateGridState(newState: GridState) {
        gridState = newState
        gridConfig = calculateGrid()
    }

    fun updateStyle(shapeId: String, scale: Float, paddingScale: Float = DEFAULT_CONTAINER_PADDING_SCALE) {
        this.unitShapeId = shapeId
        this.unitScale = scale
        this.containerPaddingScale = paddingScale
        gridConfig = calculateGrid()
        updateShapeDrawer()
    }

    private fun updateShapeDrawer() {
        shapeDrawer = when (unitShapeId) {
            "circle" -> CircleDrawer()
            "rhombus", "square" -> RhombusDrawer()
            "squircle" -> SquircleDrawer()
            else -> RoundedSquareDrawer()
        }
    }

    private interface ShapeDrawer {
        fun draw(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint)
    }

    private class CircleDrawer : ShapeDrawer {
        override fun draw(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
            val radius = size / 2f
            canvas.drawCircle(x + radius, y + radius, radius, paint)
        }
    }

    private class RhombusDrawer : ShapeDrawer {
        override fun draw(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
            canvas.save()
            canvas.rotate(RHOMBUS_ROTATION_DEGREES, x + size / 2f, y + size / 2f)
            val center = size / 2f
            val halfScaled = (size * RHOMBUS_SCALE) / 2f
            canvas.drawRect(
                x + center - halfScaled,
                y + center - halfScaled,
                x + center + halfScaled,
                y + center + halfScaled,
                paint
            )
            canvas.restore()
        }
    }

    private class SquircleDrawer : ShapeDrawer {
        override fun draw(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
            val radius = size * SQUIRCLE_CORNER_RATIO
            canvas.drawRoundRect(x, y, x + size, y + size, radius, radius, paint)
        }
    }

    private class RoundedSquareDrawer : ShapeDrawer {
        override fun draw(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
            val radius = size * ROUNDED_CORNER_RATIO
            canvas.drawRoundRect(x, y, x + size, y + size, radius, radius, paint)
        }
    }
}
