package com.clearsky.weather.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clearsky.weather.data.local.ClearSkyDatabase
import com.clearsky.weather.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationDaoTest {

    private lateinit var database: ClearSkyDatabase
    private lateinit var locationDao: LocationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ClearSkyDatabase::class.java
        ).allowMainThreadQueries().build()
        locationDao = database.locationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createLocation(
        name: String = "New York",
        lat: Double = 40.7128,
        lon: Double = -74.0060,
        sortOrder: Int = 0,
        isCurrentLocation: Boolean = false
    ) = SavedLocationEntity(
        name = name,
        latitude = lat,
        longitude = lon,
        countryCode = "US",
        admin1 = "New York",
        timezone = "America/New_York",
        isCurrentLocation = isCurrentLocation,
        sortOrder = sortOrder,
        addedAt = System.currentTimeMillis()
    )

    @Test
    fun insertAndGetLocation() = runTest {
        val location = createLocation()
        val id = locationDao.insertLocation(location)

        val result = locationDao.getLocationById(id)
        assertNotNull(result)
        assertEquals("New York", result!!.name)
        assertEquals(40.7128, result.latitude, 0.0001)
    }

    @Test
    fun getAllLocations_orderedBySortOrder() = runTest {
        locationDao.insertLocation(createLocation(name = "C", sortOrder = 2))
        locationDao.insertLocation(createLocation(name = "A", sortOrder = 0))
        locationDao.insertLocation(createLocation(name = "B", sortOrder = 1))

        val locations = locationDao.getAllLocations().first()
        assertEquals(3, locations.size)
        assertEquals("A", locations[0].name)
        assertEquals("B", locations[1].name)
        assertEquals("C", locations[2].name)
    }

    @Test
    fun getAllLocationsList_returnsSuspendResult() = runTest {
        locationDao.insertLocation(createLocation(name = "NYC", sortOrder = 0))
        locationDao.insertLocation(createLocation(name = "LA", sortOrder = 1))

        val locations = locationDao.getAllLocationsList()
        assertEquals(2, locations.size)
    }

    @Test
    fun getCurrentLocation_returnsCurrentOnly() = runTest {
        locationDao.insertLocation(createLocation(name = "Regular", isCurrentLocation = false))
        locationDao.insertLocation(createLocation(name = "Current", isCurrentLocation = true))

        val current = locationDao.getCurrentLocation()
        assertNotNull(current)
        assertEquals("Current", current!!.name)
        assertTrue(current.isCurrentLocation)
    }

    @Test
    fun getCurrentLocation_returnsNullWhenNoCurrent() = runTest {
        locationDao.insertLocation(createLocation(isCurrentLocation = false))

        val result = locationDao.getCurrentLocation()
        assertNull(result)
    }

    @Test
    fun updateLocation() = runTest {
        val id = locationDao.insertLocation(createLocation(name = "Old Name"))
        val existing = locationDao.getLocationById(id)!!

        locationDao.updateLocation(existing.copy(name = "New Name"))

        val updated = locationDao.getLocationById(id)
        assertEquals("New Name", updated!!.name)
    }

    @Test
    fun deleteLocation() = runTest {
        val id = locationDao.insertLocation(createLocation())
        val location = locationDao.getLocationById(id)!!

        locationDao.deleteLocation(location)

        assertNull(locationDao.getLocationById(id))
    }

    @Test
    fun deleteLocationById() = runTest {
        val id = locationDao.insertLocation(createLocation())
        assertNotNull(locationDao.getLocationById(id))

        locationDao.deleteLocationById(id)

        assertNull(locationDao.getLocationById(id))
    }

    @Test
    fun getLocationCount() = runTest {
        assertEquals(0, locationDao.getLocationCount())

        locationDao.insertLocation(createLocation(name = "A"))
        locationDao.insertLocation(createLocation(name = "B"))

        assertEquals(2, locationDao.getLocationCount())
    }

    @Test
    fun deleteAllLocations() = runTest {
        locationDao.insertLocation(createLocation(name = "A"))
        locationDao.insertLocation(createLocation(name = "B"))
        assertEquals(2, locationDao.getLocationCount())

        locationDao.deleteAllLocations()

        assertEquals(0, locationDao.getLocationCount())
    }

    @Test
    fun replaceCurrentLocation_removesOldCurrent() = runTest {
        locationDao.insertLocation(
            createLocation(name = "Old Current", isCurrentLocation = true)
        )

        val newLocation = createLocation(name = "New Current", isCurrentLocation = false)
        locationDao.replaceCurrentLocation(newLocation)

        val locations = locationDao.getAllLocationsList()
        val currentLocations = locations.filter { it.isCurrentLocation }
        assertEquals(1, currentLocations.size)
        assertEquals("New Current", currentLocations[0].name)
    }

    @Test
    fun replaceCurrentLocation_worksWhenNoExistingCurrent() = runTest {
        val location = createLocation(name = "First Current")
        locationDao.replaceCurrentLocation(location)

        val current = locationDao.getCurrentLocation()
        assertNotNull(current)
        assertEquals("First Current", current!!.name)
        assertTrue(current.isCurrentLocation)
    }

    @Test
    fun reorderLocations_updatesSortOrder() = runTest {
        val id1 = locationDao.insertLocation(createLocation(name = "A", sortOrder = 0))
        val id2 = locationDao.insertLocation(createLocation(name = "B", sortOrder = 1))
        val id3 = locationDao.insertLocation(createLocation(name = "C", sortOrder = 2))

        val locations = locationDao.getAllLocationsList()
        val reordered = listOf(locations[2], locations[0], locations[1])
        locationDao.reorderLocations(reordered)

        val result = locationDao.getAllLocations().first()
        assertEquals("C", result[0].name)
        assertEquals(0, result[0].sortOrder)
        assertEquals("A", result[1].name)
        assertEquals(1, result[1].sortOrder)
        assertEquals("B", result[2].name)
        assertEquals(2, result[2].sortOrder)
    }
}
