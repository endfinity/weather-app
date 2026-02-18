package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.repository.LocationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSavedLocationsUseCaseTest {

    private lateinit var repository: LocationRepository
    private lateinit var useCase: GetSavedLocationsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSavedLocationsUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val locations = listOf(
            mockk<SavedLocation>(relaxed = true),
            mockk<SavedLocation>(relaxed = true)
        )
        every { repository.getSavedLocations() } returns flowOf(locations)

        val result = useCase().first()

        assertEquals(2, result.size)
        verify(exactly = 1) { repository.getSavedLocations() }
    }

    @Test
    fun `invoke returns empty flow when no saved locations`() = runTest {
        every { repository.getSavedLocations() } returns flowOf(emptyList())

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }
}
