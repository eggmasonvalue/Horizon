package com.perspectivelive.wallpaper.service

import android.app.WallpaperColors
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.perspectivelive.wallpaper.R
import com.perspectivelive.wallpaper.data.ColorSchemeProvider
import com.perspectivelive.wallpaper.data.GridState
import com.perspectivelive.wallpaper.data.PreferencesManager
import com.perspectivelive.wallpaper.data.UserPreferences
import com.perspectivelive.wallpaper.rendering.CanvasRenderer
import com.perspectivelive.wallpaper.rendering.PulseAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Base WallpaperService that encapsulates common lifecycle, rendering loop, and scheduling logic.
 * Subclasses need to provide specific state calculation logic.
 */
abstract class BaseWallpaperService : WallpaperService() {

    companion object {
        private const val TAG = "BaseWallpaperService"
        private const val MAX_CONSECUTIVE_ERRORS = 5
        private const val PLACEHOLDER_COLOR = 0xFF0A0A0A.toInt()
        private const val DEFAULT_PULSE_PERIOD = 2000L
    }

    private val activeEngines = mutableSetOf<BaseEngine>()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activeEngines.forEach { it.onSystemConfigurationChanged(newConfig) }
    }

    override fun onCreateEngine(): Engine {
        return createBaseEngine()
    }

    abstract fun createBaseEngine(): BaseEngine

    abstract inner class BaseEngine : Engine(), SharedPreferences.OnSharedPreferenceChangeListener {

        protected lateinit var preferencesManager: PreferencesManager
        protected var renderer: CanvasRenderer? = null
        protected lateinit var animator: PulseAnimator
        protected lateinit var scheduler: MidnightScheduler
        protected val handler = Handler(Looper.getMainLooper())

        protected var isRenderingVisible: Boolean = false
        protected var surfaceWidth: Int = 0
        protected var surfaceHeight: Int = 0
        private var consecutiveErrors = 0
        protected var isSafeMode: Boolean = false

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            try {
                super.onCreate(surfaceHolder)
                activeEngines.add(this)

                preferencesManager = PreferencesManager(this@BaseWallpaperService)
                val pulsePeriod = try {
                    preferencesManager.getPreferences().pulsePeriodMs
                } catch (e: Exception) {
                    DEFAULT_PULSE_PERIOD
                }
                animator = PulseAnimator(pulsePeriod)
                scheduler = MidnightScheduler(this@BaseWallpaperService)

                // Register for preference changes
                val prefs = getSharedPreferences("life_calendar_prefs", Context.MODE_PRIVATE)
                prefs.registerOnSharedPreferenceChangeListener(this)

                val filter = IntentFilter(MidnightReceiver.ACTION_UPDATE_WALLPAPER)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    @Suppress("UnspecifiedRegisterReceiverFlag")
                    registerReceiver(updateReceiver, filter)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onCreate", e)
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            try {
                super.onSurfaceCreated(holder)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onSurfaceCreated", e)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            try {
                super.onSurfaceChanged(holder, format, width, height)
                surfaceWidth = width
                surfaceHeight = height
                initializeRendererAsync()
            } catch (e: Exception) {
                Log.e(TAG, "Error in onSurfaceChanged", e)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            try {
                super.onSurfaceDestroyed(holder)
                isRenderingVisible = false
                handler.removeCallbacksAndMessages(null)
                scheduler.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error in onSurfaceDestroyed", e)
            }
        }

        open fun onSystemConfigurationChanged(newConfig: Configuration) {
            initializeRendererAsync()
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onComputeColors(): WallpaperColors? {
            val scheme = renderer?.colorScheme ?: return null
            return WallpaperColors(
                Color.valueOf(scheme.pastYearsColor),
                Color.valueOf(scheme.currentYearColor),
                Color.valueOf(scheme.backgroundColor)
            )
        }

        override fun onVisibilityChanged(visible: Boolean) {
            try {
                super.onVisibilityChanged(visible)
                isRenderingVisible = visible

                if (visible) {
                    animator.reset()
                    if (!isSafeMode) {
                        initializeRendererAsync()
                    }
                    scheduleNextFrame()
                    scheduler.scheduleMidnightCheck()
                } else {
                    handler.removeCallbacksAndMessages(null)
                    scheduler.cancel()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onVisibilityChanged", e)
            }
        }

        override fun onDestroy() {
            try {
                super.onDestroy()
                activeEngines.remove(this)
                handler.removeCallbacksAndMessages(null)
                scheduler.cancel()

                val prefs = getSharedPreferences("life_calendar_prefs", Context.MODE_PRIVATE)
                prefs.unregisterOnSharedPreferenceChangeListener(this)

                try {
                    unregisterReceiver(updateReceiver)
                } catch (e: IllegalArgumentException) {
                    // Ignore if not registered
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onDestroy", e)
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // Re-initialize renderer if style/content preferences change
            handler.post {
                initializeRendererAsync()
                scheduleNextFrame()
            }
        }

        protected open fun initializeRendererAsync() {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    initializeRenderer()
                }
            }
        }

        protected open fun initializeRenderer(healthCache: Map<LocalDate, Float>? = null) {
            try {
                if (!hasPreferences()) {
                    drawPlaceholder()
                    return
                }

                val preferences = preferencesManager.getPreferences()
                val gridState = getGridState(preferences) ?: return

                val activeSchemeId = if (preferences.isDailyRotationEnabled) {
                    ColorSchemeProvider.getRotatedSchemeId(LocalDate.now())
                } else {
                    preferences.colorSchemeId
                }

                val isDarkMode = ColorSchemeProvider.isSystemDarkMode(this@BaseWallpaperService)
                val colorScheme = ColorSchemeProvider.getScheme(
                    id = activeSchemeId,
                    isDarkMode = isDarkMode,
                    prefsManager = preferencesManager
                )

                val typeface = try {
                    ResourcesCompat.getFont(this@BaseWallpaperService, R.font.geist_bold)
                        ?: ResourcesCompat.getFont(this@BaseWallpaperService, R.font.geist)
                } catch (e: Exception) {
                    null
                }

                animator.cycleDurationMs = preferences.pulsePeriodMs
                renderer = CanvasRenderer(
                    gridState = gridState,
                    colorScheme = colorScheme,
                    screenWidth = surfaceWidth,
                    screenHeight = surfaceHeight,
                    customTypeface = typeface
                )

                renderer?.updateStyle(
                    preferences.unitShapeId,
                    preferences.unitScale,
                    preferences.containerPaddingScale
                )
                consecutiveErrors = 0
                isSafeMode = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    notifyColorsChanged()
                }

                drawFrame()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "State invalid", e)
                drawPlaceholder()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing renderer", e)
                handleError()
            }
        }

        protected abstract fun hasPreferences(): Boolean

        protected abstract fun getGridState(preferences: UserPreferences): GridState?

        protected abstract fun performMidnightUpdate(preferences: UserPreferences)

        private fun scheduleNextFrame() {
            if (!isRenderingVisible || isSafeMode) return
            handler.postDelayed({
                drawFrame()
                scheduleNextFrame()
            }, PulseAnimator.FRAME_DURATION_MS)
        }

        private fun drawFrame() {
            if (renderer == null && !isSafeMode) return

            val holder = surfaceHolder
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    if (isSafeMode) {
                        canvas.drawColor(Color.BLACK)
                    } else {
                        val pulseOpacity = animator.getCurrentOpacity()
                        renderer?.render(canvas, pulseOpacity)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in drawFrame", e)
                handleError()
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error unlocking canvas", e)
                    }
                }
            }
        }

        protected fun drawPlaceholder() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.drawColor(PLACEHOLDER_COLOR)
            } catch (e: Exception) {
                Log.e(TAG, "Error drawing placeholder", e)
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error unlocking canvas", e)
                    }
                }
            }
        }

        private fun handleError() {
            consecutiveErrors++
            if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                Log.e(TAG, "Too many consecutive errors, entering safe mode")
                isSafeMode = true
                drawPlaceholder()
            }
        }

        private val updateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == MidnightReceiver.ACTION_UPDATE_WALLPAPER) {
                    if (hasPreferences()) {
                        performMidnightUpdate(preferencesManager.getPreferences())
                        scheduler.scheduleMidnightCheck()
                    }
                }
            }
        }
    }
}
