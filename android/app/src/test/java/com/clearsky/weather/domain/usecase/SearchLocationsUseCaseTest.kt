package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.GeocodingResult
import com.clearsky.weather.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchLocationsUseCaseTest {

    private lateinit var repository: LocationRepository
    private lateinit var useCase: SearchLocationsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchLocationsUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository searchLocations`() = runTest {
        val mockResults = listOf(
            mockk<GeocodingResult>(relaxed = true)
        )
        coEvery { repository.searchLocations("London") } returns Result.success(mockResults)

        val result = useCase("London")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        coVerify(exactly = 1) { repository.searchLocations("London") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery { repository.searchLocations(any()) } returns Result.failure(RuntimeException("Network error"))

        val result = useCase("Paris")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns empty list for no matches`() = runTest {
        coEvery { repository.searchLocations("xyznonexistent") } returns Result.success(emptyList())

        val result = useCase("xyznonexistent")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}
