package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.OnThisDayData
import com.clearsky.weather.domain.repository.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetOnThisDayUseCaseTest {

    private lateinit var repository: PremiumRepository
    private lateinit var useCase: GetOnThisDayUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetOnThisDayUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository getOnThisDay`() = runTest {
        val mockData = mockk<OnThisDayData>(relaxed = true)
        coEvery { repository.getOnThisDay(40.0, -74.0, 5, "metric") } returns Result.success(mockData)

        val result = useCase(40.0, -74.0)

        assertTrue(result.isSuccess)
        assertEquals(mockData, result.getOrNull())
        coVerify(exactly = 1) { repository.getOnThisDay(40.0, -74.0, 5, "metric") }
    }

    @Test
    fun `invoke uses default parameters`() = runTest {
        coEvery { repository.getOnThisDay(10.0, 20.0, 5, "metric") } returns Result.success(mockk(relaxed = true))

        useCase(10.0, 20.0)

        coVerify { repository.getOnThisDay(10.0, 20.0, 5, "metric") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery { repository.getOnThisDay(any(), any(), any(), any()) } returns Result.failure(RuntimeException("Not found"))

        val result = useCase(0.0, 0.0)

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke passes custom years parameter`() = runTest {
        coEvery { repository.getOnThisDay(10.0, 20.0, 10, "metric") } returns Result.success(mockk(relaxed = true))

        useCase(10.0, 20.0, years = 10)

        coVerify { repository.getOnThisDay(10.0, 20.0, 10, "metric") }
    }
}
