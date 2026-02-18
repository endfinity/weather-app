package com.clearsky.weather.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val weather: WeatherData? = null,
    val airQuality: AirQualityData? = null,
    val currentLocation: SavedLocation? = null,
    val savedLocations: List<SavedLocation> = emptyList(),
    val selectedLocationIndex: Int = 0,
    val isOffline: Boolean = false,
    val lastUpdated: Long? = null,
    val activeAlerts: List<WeatherAlert> = emptyList(),
    val preferences: UserPreferences = UserPreferences()
)

@Immutable
data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<GeocodingResult> = emptyList(),
    val error: String? = null
)

@Immutable
data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isSaving: Boolean = false
)
