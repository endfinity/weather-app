package com.clearsky.weather.data.notification

import android.util.Log
import com.clearsky.weather.data.local.PreferencesManager
import com.clearsky.weather.data.remote.ClearSkyApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val api: ClearSkyApi,
    private val preferencesManager: PreferencesManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun registerIfNeeded() {
        scope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                registerTokenWithBackend(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token", e)
            }
        }
    }

    fun onTokenRefreshed(token: String) {
        scope.launch {
            registerTokenWithBackend(token)
        }
    }

    private suspend fun registerTokenWithBackend(token: String) {
        try {
            val savedToken = preferencesManager.getFcmToken()
            if (savedToken == token) return

            api.registerDevice(DeviceRegistrationRequest(fcmToken = token))
            preferencesManager.saveFcmToken(token)
            Log.d(TAG, "FCM token registered with backend")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register FCM token", e)
        }
    }

    suspend fun unregister() {
        try {
            val token = preferencesManager.getFcmToken() ?: return
            api.unregisterDevice(token)
            preferencesManager.saveFcmToken(null)
            Log.d(TAG, "FCM token unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister FCM token", e)
        }
    }

    companion object {
        private const val TAG = "FcmTokenManager"
    }
}
