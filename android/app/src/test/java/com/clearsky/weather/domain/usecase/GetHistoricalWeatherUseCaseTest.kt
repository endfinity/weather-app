package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.HistoricalWeatherData
import com.clearsky.weather.domain.repository.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetHistoricalWeatherUseCaseTest {

    private lateinit var repository: PremiumRepository
    private lateinit var useCase: GetHistoricalWeatherUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetHistoricalWeatherUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository getHistoricalWeather`() = runTest {
        val mockData = mockk<HistoricalWeatherData>(relaxed = true)
        coEvery {
            repository.getHistoricalWeather(40.0, -74.0, "2024-01-01", "2024-01-31", "metric")
        } returns Result.success(mockData)

        val result = useCase(40.0, -74.0, "2024-01-01", "2024-01-31")

        assertTrue(result.isSuccess)
        assertEquals(mockData, result.getOrNull())
        coVerify(exactly = 1) {
            repository.getHistoricalWeather(40.0, -74.0, "2024-01-01", "2024-01-31", "metric")
        }
    }

    @Test
    fun `invoke uses default units metric`() = runTest {
        coEvery {
            repository.getHistoricalWeather(10.0, 20.0, "2024-06-01", "2024-06-30", "metric")
        } returns Result.success(mockk(relaxed = true))

        useCase(10.0, 20.0, "2024-06-01", "2024-06-30")

        coVerify { repository.getHistoricalWeather(10.0, 20.0, "2024-06-01", "2024-06-30", "metric") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery {
            repository.getHistoricalWeather(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Premium required"))

        val result = useCase(0.0, 0.0, "2024-01-01", "2024-01-31")

        assertTrue(result.isFailure)
        assertEquals("Premium required", result.exceptionOrNull()?.message)
    }
}
