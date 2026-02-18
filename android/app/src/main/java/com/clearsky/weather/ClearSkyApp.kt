package com.clearsky.weather

import android.app.Application
import com.clearsky.weather.data.notification.NotificationChannelHelper
import com.clearsky.weather.ui.widget.WidgetUpdateWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClearSkyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationChannelHelper.createChannels(this)
        WidgetUpdateWorker.enqueue(this)
    }
}
