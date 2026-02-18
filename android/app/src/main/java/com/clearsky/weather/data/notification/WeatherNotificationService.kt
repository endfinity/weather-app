package com.clearsky.weather.data.notification

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.clearsky.weather.MainActivity
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.AlertSeverity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WeatherNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenManager: FcmTokenManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")
        tokenManager.onTokenRefreshed(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        val data = message.data
        if (data.isEmpty()) {
            message.notification?.let { showSimpleNotification(it) }
            return
        }

        val alertId = data["alertId"] ?: return
        val title = data["title"] ?: "Weather Alert"
        val body = data["body"] ?: ""
        val severity = data["severity"] ?: "MINOR"
        val channelType = data["channel"] ?: "severe_alerts"

        if (isDuplicate(alertId)) {
            Log.d(TAG, "Duplicate alert skipped: $alertId")
            return
        }

        markSeen(alertId)
        showAlertNotification(alertId, title, body, severity, channelType)
    }

    private fun showSimpleNotification(notification: RemoteMessage.Notification) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NotificationChannelHelper.CHANNEL_SEVERE_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(this).notify(
                notification.hashCode(), builder.build()
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted", e)
        }
    }

    private fun showAlertNotification(
        alertId: String,
        title: String,
        body: String,
        severity: String,
        channelType: String
    ) {
        val deepLinkIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_ALERT_ID, alertId)
            action = ACTION_VIEW_ALERT
        }

        val pendingIntent = PendingIntent.getActivity(
            this, alertId.hashCode(), deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (channelType) {
            "precipitation" -> NotificationChannelHelper.CHANNEL_PRECIPITATION
            "daily_summary" -> NotificationChannelHelper.CHANNEL_DAILY_SUMMARY
            else -> NotificationChannelHelper.CHANNEL_SEVERE_ALERTS
        }

        val priority = when (severity.uppercase()) {
            AlertSeverity.EXTREME.name,
            AlertSeverity.SEVERE.name -> NotificationCompat.PRIORITY_HIGH
            AlertSeverity.MODERATE.name -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        try {
            NotificationManagerCompat.from(this).notify(
                alertId.hashCode(), builder.build()
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted", e)
        }
    }

    private fun isDuplicate(alertId: String): Boolean {
        return seenAlerts.contains(alertId)
    }

    private fun markSeen(alertId: String) {
        seenAlerts.add(alertId)
        if (seenAlerts.size > MAX_SEEN_ALERTS) {
            val excess = seenAlerts.size - MAX_SEEN_ALERTS
            repeat(excess) { seenAlerts.removeAt(0) }
        }
    }

    companion object {
        private const val TAG = "WeatherNotifService"
        const val EXTRA_ALERT_ID = "extra_alert_id"
        const val ACTION_VIEW_ALERT = "com.clearsky.weather.VIEW_ALERT"
        private const val MAX_SEEN_ALERTS = 100
        private val seenAlerts = mutableListOf<String>()
    }
}
