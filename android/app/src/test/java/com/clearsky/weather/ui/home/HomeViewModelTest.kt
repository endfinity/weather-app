package com.clearsky.weather.ui.home

import app.cash.turbine.test
import com.clearsky.weather.data.location.LocationService
import com.clearsky.weather.data.network.ConnectivityStatus
import com.clearsky.weather.data.network.NetworkConnectivityObserver
import com.clearsky.weather.data.notification.FcmTokenManager
import com.clearsky.weather.domain.model.*
import com.clearsky.weather.domain.repository.NotificationRepository
import com.clearsky.weather.domain.repository.SettingsRepository
import com.clearsky.weather.domain.usecase.GetCombinedWeatherUseCase
import com.clearsky.weather.domain.usecase.GetSavedLocationsUseCase
import com.clearsky.weather.domain.usecase.SaveLocationUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getCombinedWeather: GetCombinedWeatherUseCase
    private lateinit var getSavedLocations: GetSavedLocationsUseCase
    private lateinit var saveLocation: SaveLocationUseCase
    private lateinit var locationService: LocationService
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var connectivityObserver: NetworkConnectivityObserver
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var fcmTokenManager: FcmTokenManager

    private val connectivityFlow = MutableStateFlow(ConnectivityStatus.Available)
    private val locationsFlow = MutableStateFlow<List<SavedLocation>>(emptyList())
    private val preferencesFlow = MutableStateFlow(UserPreferences())

    private val testLocation = SavedLocation(
        id = 1L,
        name = "Test City",
        latitude = 40.0,
        longitude = -74.0,
        countryCode = "US",
        admin1 = "NY",
        timezone = "America/New_York",
        isCurrentLocation = false,
        sortOrder = 0,
        addedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getCombinedWeather = mockk(relaxed = true)
        getSavedLocations = mockk()
        saveLocation = mockk(relaxed = true)
        locationService = mockk()
        settingsRepository = mockk()
        connectivityObserver = mockk()
        notificationRepository = mockk(relaxed = true)
        fcmTokenManager = mockk(relaxed = true)

        every { getSavedLocations() } returns locationsFlow
        every { settingsRepository.getPreferences() } returns preferencesFlow
        every { connectivityObserver.connectivityStatus } returns connectivityFlow
        every { connectivityObserver.isConnected() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(
        getCombinedWeather = getCombinedWeather,
        getSavedLocations = getSavedLocations,
        saveLocation = saveLocation,
        locationService = locationService,
        settingsRepository = settingsRepository,
        connectivityObserver = connectivityObserver,
        notificationRepository = notificationRepository,
        fcmTokenManager = fcmTokenManager
    )

    @Test
    fun `initial state is loading with defaults`() = runTest {
        val vm = createViewModel()
        val state = vm.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertNull(state.weather)
        assertEquals(0, state.selectedLocationIndex)
        verify { fcmTokenManager.registerIfNeeded() }
    }

    @Test
    fun `hasLocationPermission delegates to locationService`() = runTest {
        every { locationService.hasLocationPermission() } returns true
        val vm = createViewModel()
        assertTrue(vm.hasLocationPermission())

        every { locationService.hasLocationPermission() } returns false
        assertFalse(vm.hasLocationPermission())
    }

    @Test
    fun `onPageChanged updates selected index and current location`() = runTest {
        val location2 = testLocation.copy(id = 2L, name = "City 2")
        locationsFlow.value = listOf(testLocation, location2)

        coEvery {
            getCombinedWeather(any(), any(), any(), any())
        } returns Result.success(mockk(relaxed = true) to mockk(relaxed = true))
        coEvery {
            notificationRepository.getActiveAlerts(any(), any())
        } returns Result.success(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onPageChanged(1)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.selectedLocationIndex)
        assertEquals("City 2", vm.uiState.value.currentLocation?.name)
    }

    @Test
    fun `detectCurrentLocation does nothing without permission`() = runTest {
        every { locationService.hasLocationPermission() } returns false

        val vm = createViewModel()
        vm.detectCurrentLocation()
        advanceUntilIdle()

        coVerify(exactly = 0) { locationService.getCurrentLocation() }
    }

    @Test
    fun `onRefresh clears cache and reloads`() = runTest {
        locationsFlow.value = listOf(testLocation)

        coEvery {
            getCombinedWeather(any(), any(), any(), any())
        } returns Result.success(mockk(relaxed = true) to mockk(relaxed = true))
        coEvery {
            notificationRepository.getActiveAlerts(any(), any())
        } returns Result.success(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onRefresh()
        advanceUntilIdle()

        coVerify(atLeast = 2) { getCombinedWeather(any(), any(), any(), any()) }
    }

    @Test
    fun `connectivity change to offline updates state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        connectivityFlow.value = ConnectivityStatus.Unavailable
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isOffline)
    }

    @Test
    fun `observePreferences updates preferences in state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val newPrefs = UserPreferences(temperatureUnit = TemperatureUnit.FAHRENHEIT)
        preferencesFlow.value = newPrefs
        advanceUntilIdle()

        assertEquals(TemperatureUnit.FAHRENHEIT, vm.uiState.value.preferences.temperatureUnit)
    }

    @Test
    fun `load failure shows error when no cache`() = runTest {
        locationsFlow.value = listOf(testLocation)

        coEvery {
            getCombinedWeather(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Network error"))

        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.contains("Network error"))
    }
}
