package com.clearsky.weather.domain.repository

import com.clearsky.weather.domain.model.GeocodingResult
import com.clearsky.weather.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getSavedLocations(): Flow<List<SavedLocation>>

    suspend fun getLocationById(id: Long): SavedLocation?

    suspend fun getCurrentLocation(): SavedLocation?

    suspend fun saveLocation(location: SavedLocation): Long

    suspend fun updateLocation(location: SavedLocation)

    suspend fun deleteLocation(id: Long)

    suspend fun searchLocations(query: String): Result<List<GeocodingResult>>
}
