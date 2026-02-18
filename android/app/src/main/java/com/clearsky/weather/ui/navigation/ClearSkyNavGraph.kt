package com.clearsky.weather.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.clearsky.weather.ui.alerts.AlertDetailScreen
import com.clearsky.weather.ui.historical.OnThisDayScreen
import com.clearsky.weather.ui.home.HomeScreen
import com.clearsky.weather.ui.location.LocationManagementScreen
import com.clearsky.weather.ui.pollen.PollenDetailScreen
import com.clearsky.weather.ui.premium.PremiumScreen
import com.clearsky.weather.ui.radar.RadarScreen
import com.clearsky.weather.ui.search.SearchScreen
import com.clearsky.weather.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object SearchRoute

@Serializable
object ManageLocationsRoute

@Serializable
object SettingsRoute

@Serializable
data class AlertDetailRoute(val alertId: String = "")

@Serializable
object OnThisDayRoute

@Serializable
object RadarRoute

@Serializable
object PollenDetailRoute

@Serializable
object PremiumRoute

private const val NAV_ANIM_DURATION = 300

@Composable
fun ClearSkyNavGraph(
    initialAlertId: String? = null,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAV_ANIM_DURATION)
            ) + fadeIn(tween(NAV_ANIM_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAV_ANIM_DURATION)
            ) + fadeOut(tween(NAV_ANIM_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAV_ANIM_DURATION)
            ) + fadeIn(tween(NAV_ANIM_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAV_ANIM_DURATION)
            ) + fadeOut(tween(NAV_ANIM_DURATION))
        }
    ) {
        composable<HomeRoute>(
            enterTransition = { fadeIn(tween(NAV_ANIM_DURATION)) },
            exitTransition = { fadeOut(tween(NAV_ANIM_DURATION / 2)) }
        ) {
            HomeScreen(
                onNavigateToSearch = {
                    navController.navigate(SearchRoute)
                },
                onNavigateToManageLocations = {
                    navController.navigate(ManageLocationsRoute)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute)
                },
                onNavigateToAlertDetail = { alertId ->
                    navController.navigate(AlertDetailRoute(alertId))
                },
                onNavigateToOnThisDay = {
                    navController.navigate(OnThisDayRoute)
                },
                onNavigateToRadar = {
                    navController.navigate(RadarRoute)
                },
                onNavigateToPollenDetail = {
                    navController.navigate(PollenDetailRoute)
                },
                onNavigateToPremium = {
                    navController.navigate(PremiumRoute)
                }
            )
        }

        composable<SearchRoute> {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ManageLocationsRoute> {
            LocationManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSearch = {
                    navController.navigate(SearchRoute)
                }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<AlertDetailRoute> {
            AlertDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<OnThisDayRoute> {
            OnThisDayScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<RadarRoute> {
            RadarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<PollenDetailRoute> {
            PollenDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<PremiumRoute> {
            PremiumScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    if (initialAlertId != null) {
        androidx.compose.runtime.LaunchedEffect(initialAlertId) {
            navController.navigate(AlertDetailRoute(initialAlertId))
        }
    }
}
