package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteLocationUseCaseTest {

    private lateinit var repository: LocationRepository
    private lateinit var useCase: DeleteLocationUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteLocationUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository deleteLocation`() = runTest {
        coEvery { repository.deleteLocation(42L) } just runs

        useCase(42L)

        coVerify(exactly = 1) { repository.deleteLocation(42L) }
    }

    @Test
    fun `invoke passes correct id`() = runTest {
        coEvery { repository.deleteLocation(any()) } just runs

        useCase(99L)

        coVerify { repository.deleteLocation(99L) }
    }
}
