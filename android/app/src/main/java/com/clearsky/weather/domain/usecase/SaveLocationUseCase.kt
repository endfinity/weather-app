package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.repository.LocationRepository
import javax.inject.Inject

class SaveLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(location: SavedLocation) = repository.saveLocation(location)
}
