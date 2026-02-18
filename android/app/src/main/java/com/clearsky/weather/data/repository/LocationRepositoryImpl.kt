package com.clearsky.weather.data.repository

import com.clearsky.weather.data.local.dao.LocationDao
import com.clearsky.weather.data.mapper.toDomain
import com.clearsky.weather.data.mapper.toEntity
import com.clearsky.weather.data.remote.ClearSkyApi
import com.clearsky.weather.domain.model.GeocodingResult
import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val api: ClearSkyApi,
    private val locationDao: LocationDao
) : LocationRepository {

    override fun getSavedLocations(): Flow<List<SavedLocation>> =
        locationDao.getAllLocations().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getLocationById(id: Long): SavedLocation? =
        locationDao.getLocationById(id)?.toDomain()

    override suspend fun getCurrentLocation(): SavedLocation? =
        locationDao.getCurrentLocation()?.toDomain()

    override suspend fun saveLocation(location: SavedLocation): Long =
        locationDao.insertLocation(location.toEntity())

    override suspend fun updateLocation(location: SavedLocation) {
        locationDao.updateLocation(location.toEntity())
    }

    override suspend fun deleteLocation(id: Long) {
        locationDao.deleteLocationById(id)
    }

    override suspend fun searchLocations(query: String): Result<List<GeocodingResult>> =
        runCatching {
            val response = api.searchLocations(query)
            response.data.results.map { it.toDomain() }
        }
}
