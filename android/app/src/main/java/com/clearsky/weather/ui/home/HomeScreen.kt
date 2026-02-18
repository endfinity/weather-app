package com.clearsky.weather.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clearsky.weather.domain.model.HomeUiState
import com.clearsky.weather.domain.model.UserPreferences
import com.clearsky.weather.R
import com.clearsky.weather.ui.common.PagerIndicator
import com.clearsky.weather.ui.home.components.AirQualityCard
import com.clearsky.weather.ui.home.components.AlertBanner
import com.clearsky.weather.ui.home.components.CurrentConditionsHero
import com.clearsky.weather.ui.home.components.DailyForecastCard
import com.clearsky.weather.ui.home.components.ErrorState
import com.clearsky.weather.ui.home.components.FeelsLikeCard
import com.clearsky.weather.ui.home.components.HourlyForecastStrip
import com.clearsky.weather.ui.home.components.HumidityCard
import com.clearsky.weather.ui.home.components.LoadingState
import com.clearsky.weather.ui.home.components.PrecipitationNowcastCard
import com.clearsky.weather.ui.home.components.PressureCard
import com.clearsky.weather.ui.home.components.SunriseSunsetCard
import com.clearsky.weather.ui.home.components.UvIndexCard
import com.clearsky.weather.ui.home.components.VisibilityCard
import com.clearsky.weather.ui.home.components.WindCard
import com.clearsky.weather.ui.util.FormatUtil
import com.clearsky.weather.ui.util.WeatherCodeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToManageLocations: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAlertDetail: (String) -> Unit = {},
    onNavigateToOnThisDay: () -> Unit = {},
    onNavigateToRadar: () -> Unit = {},
    onNavigateToPollenDetail: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val gradientColors = if (uiState.weather != null) {
        WeatherCodeUtil.getGradient(
            uiState.weather!!.current.weatherCode,
            uiState.weather!!.current.isDay
        )
    } else {
        listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        when {
            uiState.savedLocations.isEmpty() && !uiState.isLoading -> {
                NoLocationsState(
                    onAddLocation = onNavigateToSearch,
                    onDetectLocation = {
                        viewModel.detectCurrentLocation()
                    },
                    hasLocationPermission = viewModel.hasLocationPermission()
                )
            }
            uiState.isLoading && uiState.weather == null -> {
                LoadingState()
            }
            uiState.error != null && uiState.weather == null -> {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.onRefresh() }
                )
            }
            else -> {
                PagerWeatherContent(
                    uiState = uiState,
                    onRefresh = { viewModel.onRefresh() },
                    onPageChanged = { viewModel.onPageChanged(it) },
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToManageLocations = onNavigateToManageLocations,
                    onNavigateToSettings = onNavigateToSettings,
                    onAlertClick = onNavigateToAlertDetail,
                    onNavigateToOnThisDay = onNavigateToOnThisDay,
                    onNavigateToRadar = onNavigateToRadar,
                    onNavigateToPollenDetail = onNavigateToPollenDetail,
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PagerWeatherContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToManageLocations: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAlertClick: (String) -> Unit,
    onNavigateToOnThisDay: () -> Unit = {},
    onNavigateToRadar: () -> Unit = {},
    onNavigateToPollenDetail: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pageCount = uiState.savedLocations.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedLocationIndex,
        pageCount = { pageCount }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page)
        }
    }

    LaunchedEffect(uiState.savedLocations.size) {
        if (pagerState.currentPage >= uiState.savedLocations.size && uiState.savedLocations.isNotEmpty()) {
            pagerState.scrollToPage(uiState.savedLocations.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LocationToolbar(
            locationName = uiState.currentLocation?.name ?: "",
            isCurrentLocation = uiState.currentLocation?.isCurrentLocation == true,
            onSearchClick = onNavigateToSearch,
            onManageClick = onNavigateToManageLocations,
            onSettingsClick = onNavigateToSettings
        )

        if (uiState.isOffline) {
            OfflineBanner()
        }

        if (uiState.activeAlerts.isNotEmpty()) {
            AlertBanner(
                alerts = uiState.activeAlerts,
                onAlertClick = onAlertClick,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        PagerIndicator(
            pageCount = pageCount,
            currentPage = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { page ->
            if (page == pagerState.currentPage) {
                WeatherPage(
                    uiState = uiState,
                    preferences = uiState.preferences,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    onNavigateToOnThisDay = onNavigateToOnThisDay,
                    onNavigateToRadar = onNavigateToRadar,
                    onNavigateToPollenDetail = onNavigateToPollenDetail,
                    onNavigateToPremium = onNavigateToPremium
                )
            } else {
                LoadingState()
            }
        }
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = stringResource(R.string.offline_indicator),
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.offline_banner),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun LocationToolbar(
    locationName: String,
    isCurrentLocation: Boolean,
    onSearchClick: () -> Unit,
    onManageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onManageClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.manage_locations_icon),
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onManageClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCurrentLocation) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = stringResource(R.string.current_location),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = locationName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.search_city_icon),
                tint = Color.White
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings_icon),
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherPage(
    uiState: HomeUiState,
    preferences: UserPreferences,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToOnThisDay: () -> Unit = {},
    onNavigateToRadar: () -> Unit = {},
    onNavigateToPollenDetail: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val weather = uiState.weather ?: return
    val airQuality = uiState.airQuality
    val locationName = uiState.currentLocation?.name ?: "Unknown"

    val cardsVisible = remember { mutableStateOf(false) }
    LaunchedEffect(weather) { cardsVisible.value = true }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CurrentConditionsHero(
                current = weather.current,
                todayForecast = weather.daily.firstOrNull(),
                locationName = locationName,
                temperatureUnit = preferences.temperatureUnit,
                modifier = Modifier.graphicsLayer {
                    translationY = scrollState.value * 0.4f
                    alpha = 1f - (scrollState.value / 800f).coerceIn(0f, 0.3f)
                }
            )

            StaggeredCard(visible = cardsVisible.value, index = 0) {
                HourlyForecastStrip(
                    hourlyForecasts = weather.hourly,
                    temperatureUnit = preferences.temperatureUnit,
                    timeFormat = preferences.timeFormat,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            StaggeredCard(visible = cardsVisible.value, index = 1) {
                DailyForecastCard(
                    dailyForecasts = weather.daily,
                    temperatureUnit = preferences.temperatureUnit,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (weather.minutely15Available && !weather.minutely15.isNullOrEmpty()) {
                StaggeredCard(visible = cardsVisible.value, index = 2) {
                    PrecipitationNowcastCard(
                        minutely15 = weather.minutely15,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            StaggeredCard(visible = cardsVisible.value, index = 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UvIndexCard(
                        uvIndex = weather.hourly.firstOrNull()?.uvIndex ?: 0.0,
                        modifier = Modifier.weight(1f)
                    )
                    FeelsLikeCard(
                        feelsLike = weather.current.feelsLike,
                        actual = weather.current.temperature,
                        temperatureUnit = preferences.temperatureUnit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            StaggeredCard(visible = cardsVisible.value, index = 4) {
                WindCard(
                    windSpeed = weather.current.windSpeed,
                    windDirection = weather.current.windDirection,
                    windGusts = weather.current.windGusts,
                    windSpeedUnit = preferences.windSpeedUnit,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (airQuality != null) {
                StaggeredCard(visible = cardsVisible.value, index = 5) {
                    AirQualityCard(
                        airQuality = airQuality.current,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            StaggeredCard(visible = cardsVisible.value, index = 6) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HumidityCard(
                        humidity = weather.current.humidity,
                        dewPoint = weather.hourly.firstOrNull()?.dewPoint ?: 0.0,
                        temperatureUnit = preferences.temperatureUnit,
                        modifier = Modifier.weight(1f)
                    )
                    PressureCard(
                        pressureMsl = weather.current.pressureMsl,
                        surfacePressure = weather.current.surfacePressure,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val todayForecast = weather.daily.firstOrNull()
            if (todayForecast != null) {
                StaggeredCard(visible = cardsVisible.value, index = 7) {
                    SunriseSunsetCard(
                        sunrise = todayForecast.sunrise,
                        sunset = todayForecast.sunset,
                        currentTime = weather.current.time,
                        timeFormat = preferences.timeFormat,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            StaggeredCard(visible = cardsVisible.value, index = 8) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VisibilityCard(
                        visibilityMeters = weather.hourly.firstOrNull()?.visibility ?: 10000,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            StaggeredCard(visible = cardsVisible.value, index = 9) {
                PremiumFeaturesRow(
                    onRadarClick = onNavigateToRadar,
                    onOnThisDayClick = onNavigateToOnThisDay,
                    onPollenClick = onNavigateToPollenDetail,
                    onPremiumClick = onNavigateToPremium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (uiState.lastUpdated != null) {
                Text(
                    text = stringResource(R.string.updated_format, FormatUtil.formatRelativeTime(uiState.lastUpdated)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PremiumFeaturesRow(
    onRadarClick: () -> Unit,
    onOnThisDayClick: () -> Unit,
    onPollenClick: () -> Unit,
    onPremiumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = stringResource(R.string.premium_features_icon),
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.premium_features),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PremiumFeatureChip(
                    icon = Icons.Filled.Radar,
                    label = stringResource(R.string.radar),
                    onClick = onRadarClick
                )
                PremiumFeatureChip(
                    icon = Icons.Filled.History,
                    label = stringResource(R.string.on_this_day),
                    onClick = onOnThisDayClick
                )
                PremiumFeatureChip(
                    icon = Icons.Filled.Grass,
                    label = stringResource(R.string.pollen),
                    onClick = onPollenClick
                )
                PremiumFeatureChip(
                    icon = Icons.Filled.Star,
                    label = stringResource(R.string.upgrade),
                    onClick = onPremiumClick,
                    highlight = true
                )
            }
        }
    }
}

@Composable
private fun PremiumFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (highlight) Color(0xFFFFD700) else Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun NoLocationsState(
    onAddLocation: () -> Unit,
    onDetectLocation: () -> Unit,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onDetectLocation()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = stringResource(R.string.location_pin),
            modifier = Modifier.size(72.dp),
            tint = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (hasLocationPermission) {
                    onDetectLocation()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Filled.MyLocation, contentDescription = stringResource(R.string.use_current_location))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.use_current_location))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onAddLocation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = stringResource(R.string.search_for_a_city))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.search_for_a_city))
        }
    }
}

private const val STAGGER_DELAY_MS = 50

@Composable
private fun StaggeredCard(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = index * STAGGER_DELAY_MS
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = index * STAGGER_DELAY_MS
            ),
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}
