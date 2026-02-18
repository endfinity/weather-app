package com.clearsky.weather.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearSkyDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ClearSkyDatabase::class.java
    )

    @Test
    fun createDatabaseV1() {
        val db = helper.createDatabase("test_db", 1).apply {
            execSQL(
                """INSERT INTO weather_cache (locationKey, weatherJson, fetchedAt)
                   VALUES ('40.7128_-74.0060', '{"temp":72}', 1000)"""
            )
            execSQL(
                """INSERT INTO saved_locations (name, latitude, longitude, countryCode, admin1, timezone, isCurrentLocation, sortOrder, addedAt)
                   VALUES ('New York', 40.7128, -74.006, 'US', 'New York', 'America/New_York', 0, 0, 1000)"""
            )
            close()
        }
    }
}
