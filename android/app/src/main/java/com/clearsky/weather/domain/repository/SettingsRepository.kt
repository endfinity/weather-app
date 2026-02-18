package com.clearsky.weather.domain.repository

import com.clearsky.weather.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getPreferences(): Flow<UserPreferences>

    suspend fun updatePreferences(preferences: UserPreferences)
}
