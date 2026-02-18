package com.clearsky.weather.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_locations",
    indices = [
        Index(value = ["isCurrentLocation"]),
        Index(value = ["sortOrder"])
    ]
)
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String,
    val admin1: String?,
    val timezone: String,
    val isCurrentLocation: Boolean,
    val sortOrder: Int,
    val addedAt: Long
)
