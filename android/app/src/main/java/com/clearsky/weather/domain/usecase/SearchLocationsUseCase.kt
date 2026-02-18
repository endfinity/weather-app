package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.GeocodingResult
import com.clearsky.weather.domain.repository.LocationRepository
import javax.inject.Inject

class SearchLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(query: String): Result<List<GeocodingResult>> =
        repository.searchLocations(query)
}
