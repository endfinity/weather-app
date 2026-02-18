package com.clearsky.weather.ui.pollen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.model.HourlyPollen
import com.clearsky.weather.domain.usecase.GetCombinedWeatherUseCase
import com.clearsky.weather.domain.usecase.GetSavedLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PollenUiState(
    val isLoading: Boolean = true,
    val pollenData: List<HourlyPollen>? = null,
    val pollenAvailable: Boolean = false,
    val error: String? = null,
    val locationName: String = "",
    val currentPollenSummary: PollenSummary? = null
)

data class PollenSummary(
    val alderLevel: PollenLevel,
    val birchLevel: PollenLevel,
    val grassLevel: PollenLevel,
    val mugwortLevel: PollenLevel,
    val oliveLevel: PollenLevel,
    val ragweedLevel: PollenLevel
)

enum class PollenLevel(val label: String) {
    NONE("None"),
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High"),
    VERY_HIGH("Very High");

    companion object {
        fun fromValue(value: Double): PollenLevel = when {
            value <= 0 -> NONE
            value < 20 -> LOW
            value < 50 -> MODERATE
            value < 100 -> HIGH
            else -> VERY_HIGH
        }
    }
}

@HiltViewModel
class PollenDetailViewModel @Inject constructor(
    private val getCombinedWeather: GetCombinedWeatherUseCase,
    private val getSavedLocations: GetSavedLocationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PollenUiState())
    val uiState: StateFlow<PollenUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val locations = getSavedLocations().first()
            val location = locations.firstOrNull()

            if (location == null) {
                _uiState.update { it.copy(isLoading = false, error = "No saved locations") }
                return@launch
            }

            _uiState.update { it.copy(locationName = location.name) }

            getCombinedWeather(
                latitude = location.latitude,
                longitude = location.longitude
            ).onSuccess { (_, airQuality) ->
                val pollen = airQuality.pollen
                val summary = pollen.hourly?.firstOrNull()?.let { current ->
                    PollenSummary(
                        alderLevel = PollenLevel.fromValue(current.alderPollen),
                        birchLevel = PollenLevel.fromValue(current.birchPollen),
                        grassLevel = PollenLevel.fromValue(current.grassPollen),
                        mugwortLevel = PollenLevel.fromValue(current.mugwortPollen),
                        oliveLevel = PollenLevel.fromValue(current.olivePollen),
                        ragweedLevel = PollenLevel.fromValue(current.ragweedPollen)
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pollenAvailable = pollen.available,
                        pollenData = pollen.hourly,
                        currentPollenSummary = summary
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}
