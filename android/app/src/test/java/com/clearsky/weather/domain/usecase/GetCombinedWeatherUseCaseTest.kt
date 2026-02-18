package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.model.WeatherData
import com.clearsky.weather.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetCombinedWeatherUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: GetCombinedWeatherUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetCombinedWeatherUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository getCombinedWeather`() = runTest {
        val mockWeather = mockk<WeatherData>(relaxed = true)
        val mockAirQuality = mockk<AirQualityData>(relaxed = true)
        val expected = Result.success(mockWeather to mockAirQuality)

        coEvery {
            repository.getCombinedWeather(40.0, -74.0, "metric", false)
        } returns expected

        val result = useCase(40.0, -74.0, "metric", false)

        assertTrue(result.isSuccess)
        assertEquals(mockWeather, result.getOrNull()?.first)
        assertEquals(mockAirQuality, result.getOrNull()?.second)
        coVerify(exactly = 1) { repository.getCombinedWeather(40.0, -74.0, "metric", false) }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery {
            repository.getCombinedWeather(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Server error"))

        val result = useCase(0.0, 0.0)

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke uses default parameters`() = runTest {
        coEvery {
            repository.getCombinedWeather(10.0, 20.0, "metric", false)
        } returns Result.success(mockk(relaxed = true) to mockk(relaxed = true))

        useCase(10.0, 20.0)

        coVerify { repository.getCombinedWeather(10.0, 20.0, "metric", false) }
    }

    @Test
    fun `invoke passes forceRefresh flag`() = runTest {
        coEvery {
            repository.getCombinedWeather(10.0, 20.0, "metric", true)
        } returns Result.success(mockk(relaxed = true) to mockk(relaxed = true))

        useCase(10.0, 20.0, forceRefresh = true)

        coVerify { repository.getCombinedWeather(10.0, 20.0, "metric", true) }
    }
}
