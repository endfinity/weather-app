package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<SavedLocation>> = repository.getSavedLocations()
}
