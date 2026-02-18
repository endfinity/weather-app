package com.clearsky.weather.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.PrecipitationUnit
import com.clearsky.weather.domain.model.SettingsUiState
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.ThemeMode
import com.clearsky.weather.domain.model.TimeFormat
import com.clearsky.weather.domain.model.UserPreferences
import com.clearsky.weather.domain.model.WindSpeedUnit
import com.clearsky.weather.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            settingsRepository.getPreferences().collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    fun updateTemperatureUnit(unit: TemperatureUnit) {
        updatePreference { it.copy(temperatureUnit = unit) }
    }

    fun updateWindSpeedUnit(unit: WindSpeedUnit) {
        updatePreference { it.copy(windSpeedUnit = unit) }
    }

    fun updatePrecipitationUnit(unit: PrecipitationUnit) {
        updatePreference { it.copy(precipitationUnit = unit) }
    }

    fun updateTimeFormat(format: TimeFormat) {
        updatePreference { it.copy(timeFormat = format) }
    }

    fun updateThemeMode(mode: ThemeMode) {
        updatePreference { it.copy(themeMode = mode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        updatePreference { it.copy(dynamicColor = enabled) }
    }

    private fun updatePreference(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val updated = transform(_uiState.value.preferences)
            settingsRepository.updatePreferences(updated)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
