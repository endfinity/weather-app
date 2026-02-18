package com.clearsky.weather.di

import android.content.Context
import androidx.room.Room
import com.clearsky.weather.data.local.ClearSkyDatabase
import com.clearsky.weather.data.local.dao.LocationDao
import com.clearsky.weather.data.local.dao.WeatherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ClearSkyDatabase =
        Room.databaseBuilder(
            context,
            ClearSkyDatabase::class.java,
            "clearsky_database"
        )
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()

    @Provides
    fun provideLocationDao(database: ClearSkyDatabase): LocationDao =
        database.locationDao()

    @Provides
    fun provideWeatherDao(database: ClearSkyDatabase): WeatherDao =
        database.weatherDao()
}
