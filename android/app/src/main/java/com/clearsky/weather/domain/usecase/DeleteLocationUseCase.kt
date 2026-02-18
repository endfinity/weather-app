package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.repository.LocationRepository
import javax.inject.Inject

class DeleteLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteLocation(id)
}
