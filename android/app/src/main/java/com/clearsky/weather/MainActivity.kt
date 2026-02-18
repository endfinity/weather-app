package com.clearsky.weather

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.clearsky.weather.data.local.PreferencesManager
import com.clearsky.weather.data.notification.WeatherNotificationService
import com.clearsky.weather.domain.model.UserPreferences
import com.clearsky.weather.ui.navigation.ClearSkyNavGraph
import com.clearsky.weather.ui.theme.ClearSkyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var pendingAlertId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleAlertIntent(intent)

        setContent {
            val preferences by preferencesManager.preferencesFlow.collectAsState(
                initial = UserPreferences()
            )

            ClearSkyTheme(
                themeMode = preferences.themeMode,
                dynamicColor = preferences.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ClearSkyNavGraph(initialAlertId = pendingAlertId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAlertIntent(intent)
    }

    private fun handleAlertIntent(intent: Intent?) {
        if (intent?.action == WeatherNotificationService.ACTION_VIEW_ALERT) {
            pendingAlertId = intent.getStringExtra(WeatherNotificationService.EXTRA_ALERT_ID)
        }
    }
}
