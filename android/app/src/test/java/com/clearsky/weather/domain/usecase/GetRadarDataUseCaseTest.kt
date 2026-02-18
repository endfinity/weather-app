package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.RadarData
import com.clearsky.weather.domain.repository.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRadarDataUseCaseTest {

    private lateinit var repository: PremiumRepository
    private lateinit var useCase: GetRadarDataUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetRadarDataUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository getRadarFrames`() = runTest {
        val mockData = mockk<RadarData>(relaxed = true)
        coEvery { repository.getRadarFrames() } returns Result.success(mockData)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(mockData, result.getOrNull())
        coVerify(exactly = 1) { repository.getRadarFrames() }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery { repository.getRadarFrames() } returns Result.failure(RuntimeException("Service unavailable"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Service unavailable", result.exceptionOrNull()?.message)
    }
}
