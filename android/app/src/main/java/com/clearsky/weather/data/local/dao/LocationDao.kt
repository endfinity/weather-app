package com.clearsky.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.clearsky.weather.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM saved_locations ORDER BY sortOrder ASC")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations ORDER BY sortOrder ASC")
    suspend fun getAllLocationsList(): List<SavedLocationEntity>

    @Query("SELECT * FROM saved_locations WHERE id = :id")
    suspend fun getLocationById(id: Long): SavedLocationEntity?

    @Query("SELECT * FROM saved_locations WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocation(): SavedLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity): Long

    @Update
    suspend fun updateLocation(location: SavedLocationEntity)

    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun getLocationCount(): Int

    @Query("DELETE FROM saved_locations")
    suspend fun deleteAllLocations()

    @Transaction
    suspend fun replaceCurrentLocation(location: SavedLocationEntity): Long {
        val existing = getCurrentLocation()
        if (existing != null) {
            deleteLocation(existing)
        }
        return insertLocation(location.copy(isCurrentLocation = true))
    }

    @Transaction
    suspend fun reorderLocations(locations: List<SavedLocationEntity>) {
        locations.forEachIndexed { index, loc ->
            updateLocation(loc.copy(sortOrder = index))
        }
    }
}
