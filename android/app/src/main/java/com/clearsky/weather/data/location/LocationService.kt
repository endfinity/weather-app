package com.clearsky.weather.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String?,
    val adminArea: String?,
    val countryCode: String?,
    val timezone: String
)

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    @Suppress("MissingPermission")
    suspend fun getCurrentLocation(): Result<DeviceLocation> = runCatching {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        val location = suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { loc ->
                if (loc != null) {
                    continuation.resume(loc)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("Location unavailable")
                    )
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }

        val geocodedInfo = reverseGeocode(location.latitude, location.longitude)

        DeviceLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = geocodedInfo?.first,
            adminArea = geocodedInfo?.second,
            countryCode = geocodedInfo?.third,
            timezone = java.util.TimeZone.getDefault().id
        )
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(lat: Double, lon: Double): Triple<String?, String?, String?>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                Triple(
                    address.locality ?: address.subAdminArea ?: address.adminArea,
                    address.adminArea,
                    address.countryCode
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
