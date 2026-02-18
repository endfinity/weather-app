package com.clearsky.weather.di

import com.clearsky.weather.data.repository.LocationRepositoryImpl
import com.clearsky.weather.data.repository.NotificationRepositoryImpl
import com.clearsky.weather.data.repository.PremiumRepositoryImpl
import com.clearsky.weather.data.repository.SettingsRepositoryImpl
import com.clearsky.weather.data.repository.WeatherRepositoryImpl
import com.clearsky.weather.domain.repository.LocationRepository
import com.clearsky.weather.domain.repository.NotificationRepository
import com.clearsky.weather.domain.repository.PremiumRepository
import com.clearsky.weather.domain.repository.SettingsRepository
import com.clearsky.weather.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindPremiumRepository(impl: PremiumRepositoryImpl): PremiumRepository
}
