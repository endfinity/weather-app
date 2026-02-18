package com.clearsky.weather.data.repository

import com.clearsky.weather.data.local.PreferencesManager
import com.clearsky.weather.domain.model.UserPreferences
import com.clearsky.weather.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : SettingsRepository {

    override fun getPreferences(): Flow<UserPreferences> =
        preferencesManager.preferencesFlow

    override suspend fun updatePreferences(preferences: UserPreferences) {
        preferencesManager.updatePreferences(preferences)
    }
}
