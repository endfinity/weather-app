package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.WeatherData
import com.clearsky.weather.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetWeatherUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: GetWeatherUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetWeatherUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository getWeather`() = runTest {
        val mockWeather = mockk<WeatherData>(relaxed = true)
        coEvery { repository.getWeather(40.0, -74.0, "metric", false) } returns Result.success(mockWeather)

        val result = useCase(40.0, -74.0)

        assertTrue(result.isSuccess)
        assertEquals(mockWeather, result.getOrNull())
        coVerify(exactly = 1) { repository.getWeather(40.0, -74.0, "metric", false) }
    }

    @Test
    fun `invoke uses default parameters`() = runTest {
        coEvery { repository.getWeather(10.0, 20.0, "metric", false) } returns Result.success(mockk(relaxed = true))

        useCase(10.0, 20.0)

        coVerify { repository.getWeather(10.0, 20.0, "metric", false) }
    }

    @Test
    fun `invoke passes forceRefresh flag`() = runTest {
        coEvery { repository.getWeather(10.0, 20.0, "metric", true) } returns Result.success(mockk(relaxed = true))

        useCase(10.0, 20.0, forceRefresh = true)

        coVerify { repository.getWeather(10.0, 20.0, "metric", true) }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery { repository.getWeather(any(), any(), any(), any()) } returns Result.failure(RuntimeException("Timeout"))

        val result = useCase(0.0, 0.0)

        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }
}
