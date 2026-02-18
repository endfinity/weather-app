package com.clearsky.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.data.location.LocationService
import com.clearsky.weather.data.network.ConnectivityStatus
import com.clearsky.weather.data.network.NetworkConnectivityObserver
import com.clearsky.weather.data.notification.FcmTokenManager
import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.model.HomeUiState
import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.model.WeatherData
import com.clearsky.weather.domain.repository.NotificationRepository
import com.clearsky.weather.domain.repository.SettingsRepository
import com.clearsky.weather.domain.usecase.GetCombinedWeatherUseCase
import com.clearsky.weather.domain.usecase.GetSavedLocationsUseCase
import com.clearsky.weather.domain.usecase.SaveLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCombinedWeather: GetCombinedWeatherUseCase,
    private val getSavedLocations: GetSavedLocationsUseCase,
    private val saveLocation: SaveLocationUseCase,
    private val locationService: LocationService,
    private val settingsRepository: SettingsRepository,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val notificationRepository: NotificationRepository,
    private val fcmTokenManager: FcmTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val weatherCache = mutableMapOf<Long, Pair<WeatherData, AirQualityData?>>()
    private var pendingRefresh = false
    private var lastRefreshTime = 0L

    companion object {
        private const val REFRESH_COOLDOWN_MS = 3_000L
    }

    init {
        observeSavedLocations()
        observePreferences()
        observeConnectivity()
        fcmTokenManager.registerIfNeeded()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            settingsRepository.getPreferences().collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.connectivityStatus.collect { status ->
                val wasOffline = _uiState.value.isOffline
                val isNowOnline = status == ConnectivityStatus.Available

                _uiState.update { it.copy(isOffline = !isNowOnline) }

                if (wasOffline && isNowOnline && pendingRefresh) {
                    pendingRefresh = false
                    onRefresh()
                }
            }
        }
    }

    private fun observeSavedLocations() {
        viewModelScope.launch {
            getSavedLocations().collect { locations ->
                val currentIndex = _uiState.value.selectedLocationIndex
                    .coerceIn(0, (locations.size - 1).coerceAtLeast(0))
                val currentLocation = locations.getOrNull(currentIndex)

                _uiState.update {
                    it.copy(
                        savedLocations = locations,
                        selectedLocationIndex = currentIndex,
                        currentLocation = currentLocation
                    )
                }

                if (currentLocation != null) {
                    loadWeatherForLocation(currentLocation)
                }
            }
        }
    }

    fun detectCurrentLocation() {
        if (!locationService.hasLocationPermission()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            locationService.getCurrentLocation()
                .onSuccess { deviceLocation ->
                    val existing = _uiState.value.savedLocations
                        .find { it.isCurrentLocation }

                    val location = SavedLocation(
                        id = existing?.id ?: 0,
                        name = deviceLocation.cityName ?: "Current Location",
                        latitude = deviceLocation.latitude,
                        longitude = deviceLocation.longitude,
                        countryCode = deviceLocation.countryCode ?: "",
                        admin1 = deviceLocation.adminArea,
                        timezone = deviceLocation.timezone,
                        isCurrentLocation = true,
                        sortOrder = -1,
                        addedAt = existing?.addedAt ?: System.currentTimeMillis()
                    )

                    saveLocation(location)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Could not detect location"
                        )
                    }
                }
        }
    }

    fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()

    fun onPageChanged(index: Int) {
        val locations = _uiState.value.savedLocations
        if (index !in locations.indices) return

        val location = locations[index]
        _uiState.update {
            it.copy(selectedLocationIndex = index, currentLocation = location)
        }

        val cached = weatherCache[location.id]
        if (cached != null) {
            _uiState.update {
                it.copy(
                    weather = cached.first,
                    airQuality = cached.second,
                    isLoading = false,
                    error = null,
                    lastUpdated = cached.first.fetchedAt
                )
            }
        } else {
            loadWeatherForLocation(location)
        }
    }

    private fun loadWeatherForLocation(
        location: SavedLocation,
        forceRefresh: Boolean = false
    ) {
        if (!forceRefresh) {
            val cached = weatherCache[location.id]
            if (cached != null) {
                if (_uiState.value.currentLocation?.id == location.id) {
                    _uiState.update {
                        it.copy(
                            weather = cached.first,
                            airQuality = cached.second,
                            isLoading = false,
                            error = null,
                            lastUpdated = cached.first.fetchedAt
                        )
                    }
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getCombinedWeather(
                latitude = location.latitude,
                longitude = location.longitude,
                forceRefresh = forceRefresh
            ).onSuccess { (weather, airQuality) ->
                weatherCache[location.id] = weather to airQuality

                if (_uiState.value.currentLocation?.id == location.id) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            weather = weather,
                            airQuality = airQuality,
                            currentLocation = location,
                            lastUpdated = weather.fetchedAt,
                            isOffline = false,
                            error = null
                        )
                    }
                    fetchAlerts(location)
                }
            }.onFailure { error ->
                if (_uiState.value.currentLocation?.id == location.id) {
                    val hasCache = weatherCache[location.id] != null
                    if (!connectivityObserver.isConnected()) {
                        pendingRefresh = true
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = if (hasCache) null else (error.message ?: "Failed to load weather data"),
                            isOffline = !connectivityObserver.isConnected()
                        )
                    }
                }
            }
        }
    }

    private fun fetchAlerts(location: SavedLocation) {
        viewModelScope.launch {
            notificationRepository.getActiveAlerts(
                location.latitude, location.longitude
            ).onSuccess { alerts ->
                _uiState.update { it.copy(activeAlerts = alerts) }
            }
        }
    }

    fun onRefresh() {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < REFRESH_COOLDOWN_MS) return
        if (_uiState.value.isRefreshing) return

        lastRefreshTime = now
        val location = _uiState.value.currentLocation ?: return
        weatherCache.remove(location.id)
        _uiState.update { it.copy(isRefreshing = true) }
        loadWeatherForLocation(location, forceRefresh = true)
    }

    fun onLocationSelected(index: Int) {
        onPageChanged(index)
    }
}
