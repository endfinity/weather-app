package com.clearsky.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.clearsky.weather.domain.model.PrecipitationUnit
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.ThemeMode
import com.clearsky.weather.domain.model.TimeFormat
import com.clearsky.weather.domain.model.UserPreferences
import com.clearsky.weather.domain.model.WindSpeedUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clearsky_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
        val PRECIPITATION_UNIT = stringPreferencesKey("precipitation_unit")
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SEVERE_ALERTS_ENABLED = booleanPreferencesKey("severe_alerts_enabled")
        val PRECIPITATION_ALERTS_ENABLED = booleanPreferencesKey("precipitation_alerts_enabled")
        val DAILY_SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val REFRESH_INTERVAL = intPreferencesKey("refresh_interval_minutes")
        val DEFAULT_LOCATION_ID = longPreferencesKey("default_location_id")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            temperatureUnit = prefs[Keys.TEMPERATURE_UNIT]?.let { TemperatureUnit.valueOf(it) }
                ?: TemperatureUnit.CELSIUS,
            windSpeedUnit = prefs[Keys.WIND_SPEED_UNIT]?.let { WindSpeedUnit.valueOf(it) }
                ?: WindSpeedUnit.KMH,
            precipitationUnit = prefs[Keys.PRECIPITATION_UNIT]?.let { PrecipitationUnit.valueOf(it) }
                ?: PrecipitationUnit.MM,
            timeFormat = prefs[Keys.TIME_FORMAT]?.let { TimeFormat.valueOf(it) }
                ?: TimeFormat.H12,
            themeMode = prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) }
                ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            severeAlertsEnabled = prefs[Keys.SEVERE_ALERTS_ENABLED] ?: true,
            precipitationAlertsEnabled = prefs[Keys.PRECIPITATION_ALERTS_ENABLED] ?: true,
            dailySummaryEnabled = prefs[Keys.DAILY_SUMMARY_ENABLED] ?: false,
            refreshIntervalMinutes = prefs[Keys.REFRESH_INTERVAL] ?: 30,
            defaultLocationId = prefs[Keys.DEFAULT_LOCATION_ID]
        )
    }

    suspend fun updatePreferences(preferences: UserPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TEMPERATURE_UNIT] = preferences.temperatureUnit.name
            prefs[Keys.WIND_SPEED_UNIT] = preferences.windSpeedUnit.name
            prefs[Keys.PRECIPITATION_UNIT] = preferences.precipitationUnit.name
            prefs[Keys.TIME_FORMAT] = preferences.timeFormat.name
            prefs[Keys.THEME_MODE] = preferences.themeMode.name
            prefs[Keys.DYNAMIC_COLOR] = preferences.dynamicColor
            prefs[Keys.NOTIFICATIONS_ENABLED] = preferences.notificationsEnabled
            prefs[Keys.SEVERE_ALERTS_ENABLED] = preferences.severeAlertsEnabled
            prefs[Keys.PRECIPITATION_ALERTS_ENABLED] = preferences.precipitationAlertsEnabled
            prefs[Keys.DAILY_SUMMARY_ENABLED] = preferences.dailySummaryEnabled
            prefs[Keys.REFRESH_INTERVAL] = preferences.refreshIntervalMinutes
            preferences.defaultLocationId?.let { prefs[Keys.DEFAULT_LOCATION_ID] = it }
        }
    }

    suspend fun getFcmToken(): String? {
        return context.dataStore.data.first()[Keys.FCM_TOKEN]
    }

    suspend fun saveFcmToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token != null) {
                prefs[Keys.FCM_TOKEN] = token
            } else {
                prefs.remove(Keys.FCM_TOKEN)
            }
        }
    }
}
