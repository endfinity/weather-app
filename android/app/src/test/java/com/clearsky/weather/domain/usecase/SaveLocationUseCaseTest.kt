package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SaveLocationUseCaseTest {

    private lateinit var repository: LocationRepository
    private lateinit var useCase: SaveLocationUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SaveLocationUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository saveLocation and returns id`() = runTest {
        val location = SavedLocation(
            name = "London",
            latitude = 51.5,
            longitude = -0.12,
            countryCode = "GB",
            admin1 = "England",
            timezone = "Europe/London",
            isCurrentLocation = false,
            sortOrder = 0,
            addedAt = System.currentTimeMillis()
        )
        coEvery { repository.saveLocation(location) } returns 42L

        val result = useCase(location)

        assertEquals(42L, result)
        coVerify(exactly = 1) { repository.saveLocation(location) }
    }

    @Test
    fun `invoke passes location object unchanged`() = runTest {
        val location = SavedLocation(
            name = "Tokyo",
            latitude = 35.68,
            longitude = 139.69,
            countryCode = "JP",
            admin1 = null,
            timezone = "Asia/Tokyo",
            isCurrentLocation = true,
            sortOrder = 1,
            addedAt = 1000L
        )
        coEvery { repository.saveLocation(location) } returns 1L

        useCase(location)

        coVerify { repository.saveLocation(match { it.name == "Tokyo" && it.isCurrentLocation }) }
    }
}
