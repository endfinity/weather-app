package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAirQualityUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: GetAirQualityUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAirQualityUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository getAirQuality`() = runTest {
        val mockData = mockk<AirQualityData>(relaxed = true)
        coEvery { repository.getAirQuality(40.0, -74.0, false) } returns Result.success(mockData)

        val result = useCase(40.0, -74.0)

        assertTrue(result.isSuccess)
        assertEquals(mockData, result.getOrNull())
        coVerify(exactly = 1) { repository.getAirQuality(40.0, -74.0, false) }
    }

    @Test
    fun `invoke uses default forceRefresh false`() = runTest {
        coEvery { repository.getAirQuality(10.0, 20.0, false) } returns Result.success(mockk(relaxed = true))

        useCase(10.0, 20.0)

        coVerify { repository.getAirQuality(10.0, 20.0, false) }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery { repository.getAirQuality(any(), any(), any()) } returns Result.failure(RuntimeException("API error"))

        val result = useCase(0.0, 0.0)

        assertTrue(result.isFailure)
        assertEquals("API error", result.exceptionOrNull()?.message)
    }
}
