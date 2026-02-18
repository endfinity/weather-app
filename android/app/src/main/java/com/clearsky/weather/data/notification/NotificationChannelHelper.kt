package com.clearsky.weather.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannelHelper {

    const val CHANNEL_SEVERE_ALERTS = "severe_alerts"
    const val CHANNEL_PRECIPITATION = "precipitation_alerts"
    const val CHANNEL_DAILY_SUMMARY = "daily_summary"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val severeChannel = NotificationChannel(
            CHANNEL_SEVERE_ALERTS,
            "Severe Weather Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical alerts for severe weather conditions"
            enableVibration(true)
            setShowBadge(true)
        }

        val precipitationChannel = NotificationChannel(
            CHANNEL_PRECIPITATION,
            "Precipitation Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about upcoming rain, snow, or storms"
            setShowBadge(true)
        }

        val dailySummaryChannel = NotificationChannel(
            CHANNEL_DAILY_SUMMARY,
            "Daily Summary",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Daily weather forecast summary"
        }

        manager.createNotificationChannels(
            listOf(severeChannel, precipitationChannel, dailySummaryChannel)
        )
    }
}
