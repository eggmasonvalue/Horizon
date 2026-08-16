package com.perspectivelive.wallpaper.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.perspectivelive.wallpaper.data.ColorSchemeProvider
import com.perspectivelive.wallpaper.data.DayCounterMode
import com.perspectivelive.wallpaper.data.PreferencesManager
import com.perspectivelive.wallpaper.data.StyleConfig
import com.perspectivelive.wallpaper.data.UserPreferences
import java.time.LocalDate

/**
 * ViewModel for managing user preferences and UI state in the Settings screen.
 * Handles loading, updating, and saving preferences, ensuring separation of concerns.
 */
class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    companion object {
        private const val DEFAULT_AGE_OFFSET = 25L
        private const val DEFAULT_LIFESPAN = 90
        private const val DEFAULT_EVENT_OFFSET_DAYS = 30L
    }

    private val _userPreferences = MutableLiveData<UserPreferences>()
    val userPreferences: LiveData<UserPreferences> = _userPreferences

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        if (!preferencesManager.hasPreferences()) {
            createDefaultPreferences()
        }

        try {
            _userPreferences.value = preferencesManager.getPreferences()
            initDayCounterDefaults()
        } catch (e: Exception) {
            createDefaultPreferences()
            _userPreferences.value = preferencesManager.getPreferences()
        }
    }

    private fun createDefaultPreferences() {
        val defaultDate = LocalDate.now().minusYears(DEFAULT_AGE_OFFSET)
        val defaultScheme = ColorSchemeProvider.DEFAULT_SCHEME_ID

        val defaultPrefs = UserPreferences(
            birthDate = defaultDate,
            expectedLifespan = DEFAULT_LIFESPAN,
            colorSchemeId = defaultScheme,
            isOnboardingComplete = true
        )

        preferencesManager.savePreferences(defaultPrefs)
    }

    private fun initDayCounterDefaults() {
        val currentPrefs = _userPreferences.value ?: return

        if (currentPrefs.eventDate == null ||
            currentPrefs.eventName == null ||
            currentPrefs.countdownStartDate == null
        ) {
            val today = LocalDate.now()
            val updatedPrefs = currentPrefs.copy(
                eventDate = currentPrefs.eventDate ?: today,
                eventName = currentPrefs.eventName ?: "My Event",
                countdownStartDate = currentPrefs.countdownStartDate ?: today,
                isDayCounterOnboardingComplete = true
            )
            preferencesManager.savePreferences(updatedPrefs)
            _userPreferences.value = updatedPrefs
        }
    }

    /**
     * Updates the birth date. Returns true if valid and updated, false otherwise.
     */
    fun updateBirthDate(date: LocalDate): Boolean {
        if (!date.isBefore(LocalDate.now())) return false

        val current = _userPreferences.value ?: return false
        val updated = current.copy(birthDate = date)
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
        return true
    }

    fun updateExpectedLifespan(lifespan: Int) {
        val current = _userPreferences.value ?: return
        val updated = current.copy(expectedLifespan = lifespan)
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
    }

    fun updateColorScheme(config: StyleConfig) {
        val current = _userPreferences.value ?: return
        val updated = current.copy(
            colorSchemeId = config.schemeId,
            unitShapeId = config.shapeId,
            unitScale = config.scale,
            containerPaddingScale = config.paddingScale,
            pulsePeriodMs = config.pulsePeriodMs,
            healthMetric = config.healthMetric,
            healthMetricGoal = config.healthGoal,
            showStatOverlay = config.showStatOverlay,
            isDailyRotationEnabled = config.isDailyRotationEnabled
        )
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
    }

    // Momentum Updates
    fun setNoTomorrowMode() {
        val current = _userPreferences.value ?: return
        val today = LocalDate.now()
        val updated = current.copy(
            eventName = "No Tomorrow",
            countdownStartDate = today,
            eventDate = today,
            dayCounterMode = DayCounterMode.NO_TOMORROW
        )
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
    }

    fun setVsYesterdayMode() {
        val current = _userPreferences.value ?: return
        val today = LocalDate.now()
        val updated = current.copy(
            eventName = "Rise Above",
            countdownStartDate = today.minusDays(1),
            eventDate = today,
            dayCounterMode = DayCounterMode.VS_YESTERDAY
        )
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
    }

    fun updateEventName(name: String) {
        val current = _userPreferences.value ?: return
        val updated = current.copy(eventName = name)
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
    }

    /**
     * Updates the event date. Returns true if valid and updated, false otherwise.
     */
    fun updateEventDate(date: LocalDate): Boolean {
        val current = _userPreferences.value ?: return false
        val startDate = current.countdownStartDate ?: LocalDate.now()

        // Ensure event date is not before start date (allow same day)
        if (date.isBefore(startDate)) return false

        val updated = current.copy(
            eventDate = date,
            countdownStartDate = startDate,
            dayCounterMode = DayCounterMode.STATIC
        )
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
        return true
    }

    /**
     * Updates the start date. Returns true if valid and updated, false otherwise.
     */
    fun updateStartDate(date: LocalDate): Boolean {
        val current = _userPreferences.value ?: return false
        val eventDate = current.eventDate ?: date.plusDays(DEFAULT_EVENT_OFFSET_DAYS)

        // Ensure start date is not after event date
        if (date.isAfter(eventDate)) return false

        val updated = current.copy(
            countdownStartDate = date,
            dayCounterMode = DayCounterMode.STATIC
        )
        _userPreferences.value = updated
        preferencesManager.savePreferences(updated)
        return true
    }

    fun savePreferences() {
        _userPreferences.value?.let { preferencesManager.savePreferences(it) }
    }
}

class SettingsViewModelFactory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
