package com.perspectivelive.wallpaper.settings

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.perspectivelive.wallpaper.R
import com.perspectivelive.wallpaper.data.ColorScheme
import com.perspectivelive.wallpaper.data.ColorSchemeProvider
import com.perspectivelive.wallpaper.data.PreferencesManager
import com.perspectivelive.wallpaper.data.StyleConfig
import com.perspectivelive.wallpaper.service.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Material 3 Modal Bottom Sheet for style selection (Shape, Size, Color, Rotation, Breathing Cycle).
 */
class StyleSelectionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var colorCardAdapter: ColorCardAdapter

    private var selectedScheme: ColorScheme? = null
    private var selectedShapeId: String = "rounded_square"
    private var selectedScale: Float = DEFAULT_SCALE
    private var selectedPaddingScale: Float = DEFAULT_PADDING_SCALE
    private var selectedPulsePeriodMs: Long = DEFAULT_PULSE_PERIOD
    private var selectedDailyRotationEnabled: Boolean = false

    private var selectedHealthMetric: String = HealthConnectManager.METRIC_NONE
    private var selectedHealthGoal: Float = DEFAULT_HEALTH_GOAL
    private var selectedShowStatOverlay: Boolean = false
    private var enableHealthSettings: Boolean = true

    private var onStyleApplied: ((ColorScheme, StyleConfig) -> Unit)? = null

    private val healthPermissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        HealthConnectManager.getRequiredPermission(selectedHealthMetric)?.let { perm ->
            if (grantedPermissions.contains(perm)) {
                updateHealthUI()
            } else {
                view?.findViewById<MaterialButtonToggleGroup>(R.id.healthMetricToggleGroup)?.check(R.id.btnMetricNone)
                selectedHealthMetric = HealthConnectManager.METRIC_NONE
                updateHealthUI()
            }
        }
    }

    private val customColorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val isDark = ColorSchemeProvider.isSystemDarkMode(requireContext())
            preferencesManager.getCustomColors()?.let { customColors ->
                val customScheme = ColorSchemeProvider.createCustomColorScheme(customColors, isDark)
                colorCardAdapter.updateCustomScheme(customScheme)
                selectedScheme = customScheme
                selectedDailyRotationEnabled = false
                view?.findViewById<MaterialSwitch>(R.id.switchDailyRotation)?.isChecked = false
                colorCardAdapter.setSelectedScheme(customScheme.id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_style_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        arguments?.let { args ->
            selectedShapeId = args.getString(ARG_INITIAL_SHAPE_ID, "rounded_square")
            selectedScale = args.getFloat(ARG_INITIAL_SCALE, DEFAULT_SCALE)
            selectedPaddingScale = args.getFloat(ARG_INITIAL_PADDING_SCALE, DEFAULT_PADDING_SCALE)
            selectedPulsePeriodMs = args.getLong(ARG_INITIAL_PULSE_PERIOD_MS, DEFAULT_PULSE_PERIOD)
            selectedDailyRotationEnabled = args.getBoolean(ARG_INITIAL_DAILY_ROTATION, false)
            selectedHealthMetric = args.getString(ARG_INITIAL_HEALTH_METRIC, HealthConnectManager.METRIC_NONE)
            selectedHealthGoal = args.getFloat(ARG_INITIAL_HEALTH_GOAL, DEFAULT_HEALTH_GOAL)
            selectedShowStatOverlay = args.getBoolean(ARG_INITIAL_STAT_OVERLAY, false)
            enableHealthSettings = args.getBoolean(ARG_ENABLE_HEALTH_SETTINGS, true)
        }

        setupHealthToggle(view)
        setupShapeToggle(view)
        setupSizeSlider(view)
        setupPaddingSlider(view)
        setupPulsePeriodSlider(view)
        setupDailyRotationToggle(view)
        setupColorGrid(view)
        setupButtons(view)
    }

    override fun getTheme(): Int = com.google.android.material.R.style.Theme_Material3_DayNight_BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = (resources.displayMetrics.heightPixels * PEEK_HEIGHT_RATIO).toInt()
            }
        }

        return dialog
    }

    private fun setupHealthToggle(view: View) {
        if (!enableHealthSettings) {
            view.findViewById<View>(R.id.healthSectionContainer)?.visibility = View.GONE
            return
        }

        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.healthMetricToggleGroup)

        val initialBtnId = when (selectedHealthMetric) {
            HealthConnectManager.METRIC_STEPS -> R.id.btnMetricSteps
            HealthConnectManager.METRIC_CALORIES -> R.id.btnMetricCalories
            HealthConnectManager.METRIC_DISTANCE -> R.id.btnMetricDistance
            HealthConnectManager.METRIC_SLEEP -> R.id.btnMetricSleep
            else -> R.id.btnMetricNone
        }
        toggleGroup.check(initialBtnId)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newMetric = when (checkedId) {
                    R.id.btnMetricSteps -> HealthConnectManager.METRIC_STEPS
                    R.id.btnMetricCalories -> HealthConnectManager.METRIC_CALORIES
                    R.id.btnMetricDistance -> HealthConnectManager.METRIC_DISTANCE
                    R.id.btnMetricSleep -> HealthConnectManager.METRIC_SLEEP
                    else -> HealthConnectManager.METRIC_NONE
                }

                if (newMetric != HealthConnectManager.METRIC_NONE && newMetric != selectedHealthMetric) {
                    selectedHealthMetric = newMetric
                    checkHealthPermissionsAndApply()
                } else if (newMetric == HealthConnectManager.METRIC_NONE) {
                    selectedHealthMetric = newMetric
                    updateHealthUI()
                }
            }
        }

        val goalInput = view.findViewById<TextInputEditText>(R.id.goalInputEdit)
        goalInput.setText(selectedHealthGoal.toInt().toString())
        goalInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }
            override fun afterTextChanged(s: Editable?) {
                val value = s?.toString()?.toFloatOrNull()
                if (value != null && value > 0) {
                    selectedHealthGoal = value
                }
            }
        })

        val switchOverlay = view.findViewById<MaterialSwitch>(R.id.switchStatOverlay)
        switchOverlay.isChecked = selectedShowStatOverlay
        switchOverlay.setOnCheckedChangeListener { _, isChecked ->
            selectedShowStatOverlay = isChecked
        }

        updateHealthUI()
    }

    private fun checkHealthPermissionsAndApply() {
        lifecycleScope.launch {
            try {
                val hcManager = HealthConnectManager(requireContext())
                if (!hcManager.hasPermissions(selectedHealthMetric)) {
                    HealthConnectManager.getRequiredPermission(selectedHealthMetric)?.let { perm ->
                        healthPermissionLauncher.launch(setOf(perm))
                    }
                } else {
                    updateHealthUI()
                }
            } catch (e: IllegalStateException) {
                view?.findViewById<MaterialButtonToggleGroup>(R.id.healthMetricToggleGroup)?.check(R.id.btnMetricNone)
                selectedHealthMetric = HealthConnectManager.METRIC_NONE
                updateHealthUI()
            }
        }
    }

    private fun updateHealthUI() {
        val container = view?.findViewById<View>(R.id.healthGoalContainer)
        if (selectedHealthMetric == HealthConnectManager.METRIC_NONE) {
            container?.visibility = View.GONE
        } else {
            container?.visibility = View.VISIBLE
        }
    }

    private fun setupShapeToggle(view: View) {
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.shapeToggleGroup)

        val initialBtnId = when (selectedShapeId) {
            "circle" -> R.id.btnShapeCircle
            "rhombus", "square" -> R.id.btnShapeRhombus
            else -> R.id.btnShapeRounded
        }
        toggleGroup.check(initialBtnId)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedShapeId = when (checkedId) {
                    R.id.btnShapeCircle -> "circle"
                    R.id.btnShapeRhombus -> "rhombus"
                    else -> "rounded_square"
                }
            }
        }
    }

    private fun setupSizeSlider(view: View) {
        val slider = view.findViewById<Slider>(R.id.sizeSlider)
        val valueText = view.findViewById<TextView>(R.id.sizeValueText)

        if (slider != null) {
            slider.value = selectedScale.coerceIn(MIN_SCALE, MAX_SCALE)
            valueText?.text = "${(selectedScale * PERCENTAGE_MULTIPLIER).toInt()}%"

            slider.addOnChangeListener { _, value, _ ->
                selectedScale = value
                valueText?.text = "${(value * PERCENTAGE_MULTIPLIER).toInt()}%"
            }
        }
    }

    private fun setupPaddingSlider(view: View) {
        val slider = view.findViewById<Slider>(R.id.paddingSlider)
        val valueText = view.findViewById<TextView>(R.id.paddingValueText)

        if (slider != null) {
            slider.value = selectedPaddingScale
            valueText?.text = "${(selectedPaddingScale * PERCENTAGE_MULTIPLIER).toInt()}%"

            slider.addOnChangeListener { _, value, _ ->
                selectedPaddingScale = value
                valueText?.text = "${(value * PERCENTAGE_MULTIPLIER).toInt()}%"
            }
        }
    }

    private fun setupPulsePeriodSlider(view: View) {
        val slider = view.findViewById<Slider>(R.id.blinkPeriodSlider)
        val valueText = view.findViewById<TextView>(R.id.blinkPeriodValueText)

        if (slider != null) {
            slider.value = selectedPulsePeriodMs.toFloat().coerceIn(MIN_PULSE_PERIOD_MS, MAX_PULSE_PERIOD_MS)
            valueText?.text = "${selectedPulsePeriodMs}ms"

            slider.addOnChangeListener { _, value, _ ->
                selectedPulsePeriodMs = value.toLong()
                valueText?.text = "${selectedPulsePeriodMs}ms"
            }
        }
    }

    private fun setupDailyRotationToggle(view: View) {
        val switchRotation = view.findViewById<MaterialSwitch>(R.id.switchDailyRotation)
        if (switchRotation != null) {
            switchRotation.isChecked = selectedDailyRotationEnabled
            switchRotation.setOnCheckedChangeListener { _, isChecked ->
                selectedDailyRotationEnabled = isChecked
            }
        }
    }

    private fun setupColorGrid(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.colorCardsRecyclerView)
        val isDarkMode = ColorSchemeProvider.isSystemDarkMode(requireContext())

        // Get all preset schemes resolved for current mode
        val schemes = ColorSchemeProvider.getAllSchemes(isDarkMode).toMutableList()

        // Add custom scheme if it exists
        if (preferencesManager.hasCustomColors()) {
            preferencesManager.getCustomColors()?.let { customColors ->
                val customScheme = ColorSchemeProvider.createCustomColorScheme(customColors, isDarkMode)
                schemes.add(customScheme)
            }
        }

        // Set up adapter
        colorCardAdapter = ColorCardAdapter(
            schemes = schemes,
            onSchemeSelected = { scheme ->
                selectedScheme = scheme
                selectedDailyRotationEnabled = false
                view.findViewById<MaterialSwitch>(R.id.switchDailyRotation)?.isChecked = false
            },
            onCreateCustom = {
                launchCustomColorPicker()
            }
        )

        val initialSchemeId = arguments?.getString(ARG_INITIAL_SCHEME_ID)

        if (initialSchemeId != null) {
            selectedScheme = schemes.find { it.id == initialSchemeId }
            colorCardAdapter.setSelectedScheme(initialSchemeId)
        } else {
            selectedScheme = schemes.firstOrNull()
            selectedScheme?.id?.let { colorCardAdapter.setSelectedScheme(it) }
        }

        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerView.adapter = colorCardAdapter
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.applyButton).setOnClickListener {
            selectedScheme?.let { scheme ->
                val config = StyleConfig(
                    schemeId = scheme.id,
                    shapeId = selectedShapeId,
                    scale = selectedScale,
                    paddingScale = selectedPaddingScale,
                    pulsePeriodMs = selectedPulsePeriodMs,
                    healthMetric = selectedHealthMetric,
                    healthGoal = selectedHealthGoal,
                    showStatOverlay = selectedShowStatOverlay,
                    isDailyRotationEnabled = selectedDailyRotationEnabled
                )
                onStyleApplied?.invoke(scheme, config)
            }
            dismiss()
        }
    }

    private fun launchCustomColorPicker() {
        val intent = Intent(requireContext(), CustomColorActivity::class.java)
        customColorLauncher.launch(intent)
    }

    /**
     * Sets the callback for when style is applied.
     */
    fun setOnStyleAppliedListener(listener: (ColorScheme, StyleConfig) -> Unit) {
        onStyleApplied = listener
    }

    companion object {
        private const val DEFAULT_SCALE = 1.0f
        private const val DEFAULT_PADDING_SCALE = 0.05f
        private const val DEFAULT_PULSE_PERIOD = 2000L
        private const val DEFAULT_HEALTH_GOAL = 10000f
        private const val PEEK_HEIGHT_RATIO = 0.7
        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 1.0f
        private const val MIN_PULSE_PERIOD_MS = 500f
        private const val MAX_PULSE_PERIOD_MS = 5000f
        private const val PERCENTAGE_MULTIPLIER = 100

        private const val ARG_INITIAL_SCHEME_ID = "initial_scheme_id"
        private const val ARG_INITIAL_SHAPE_ID = "initial_shape_id"
        private const val ARG_INITIAL_SCALE = "initial_scale"
        private const val ARG_INITIAL_PADDING_SCALE = "initial_padding_scale"
        private const val ARG_INITIAL_PULSE_PERIOD_MS = "initial_pulse_period_ms"
        private const val ARG_INITIAL_DAILY_ROTATION = "initial_daily_rotation"
        private const val ARG_INITIAL_HEALTH_METRIC = "initial_health_metric"
        private const val ARG_INITIAL_HEALTH_GOAL = "initial_health_goal"
        private const val ARG_INITIAL_STAT_OVERLAY = "initial_stat_overlay"
        private const val ARG_ENABLE_HEALTH_SETTINGS = "enable_health_settings"

        /**
         * Creates a new instance of the style sheet with initial settings.
         */
        fun newInstance(
            config: StyleConfig,
            enableHealthSettings: Boolean = true
        ): StyleSelectionBottomSheet {
            return StyleSelectionBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_INITIAL_SCHEME_ID, config.schemeId)
                    putString(ARG_INITIAL_SHAPE_ID, config.shapeId)
                    putFloat(ARG_INITIAL_SCALE, config.scale)
                    putFloat(ARG_INITIAL_PADDING_SCALE, config.paddingScale)
                    putLong(ARG_INITIAL_PULSE_PERIOD_MS, config.pulsePeriodMs)
                    putBoolean(ARG_INITIAL_DAILY_ROTATION, config.isDailyRotationEnabled)
                    putString(ARG_INITIAL_HEALTH_METRIC, config.healthMetric)
                    putFloat(ARG_INITIAL_HEALTH_GOAL, config.healthGoal)
                    putBoolean(ARG_INITIAL_STAT_OVERLAY, config.showStatOverlay)
                    putBoolean(ARG_ENABLE_HEALTH_SETTINGS, enableHealthSettings)
                }
            }
        }
    }
}
