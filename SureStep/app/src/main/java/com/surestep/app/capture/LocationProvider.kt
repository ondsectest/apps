package com.surestep.app.capture

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class CapturedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
)

/**
 * Location via the platform [LocationManager] only.
 *
 * Play Services' fused provider would give slightly better fixes, but it pulls
 * in a Google dependency, and this app's promise is that nothing about a record
 * leaves the device. The platform provider is enough for "roughly where I was".
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val locationManager: LocationManager? get() = context.getSystemService()

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Best available fix within [timeoutMillis], falling back to the most recent
     * cached fix. Returns null rather than blocking the capture: a record with a
     * timestamp and no location is still a good record.
     */
    suspend fun currentLocation(timeoutMillis: Long = FIX_TIMEOUT_MS): CapturedLocation? {
        if (!hasPermission()) return null
        val manager = locationManager ?: return null

        val fresh = withTimeoutOrNull(timeoutMillis) { requestFreshFix(manager) }
        val location = fresh ?: lastKnown(manager)
        return location?.let {
            CapturedLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                accuracyMeters = if (it.hasAccuracy()) it.accuracy else null,
            )
        }
    }

    @SuppressLint("MissingPermission") // Guarded by hasPermission() above.
    private suspend fun requestFreshFix(manager: LocationManager): Location? {
        val provider = preferredProvider(manager) ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            suspendCancellableCoroutine { continuation ->
                val signal = CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                val executor = Executor { command -> command.run() }
                manager.getCurrentLocation(provider, signal, executor) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            }
        } else {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }

                    // Required on API 28; no-ops are correct for a one-shot fix.
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                    override fun onProviderEnabled(provider: String) = Unit
                    override fun onProviderDisabled(provider: String) = Unit
                }
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
                manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
        }
    }

    @SuppressLint("MissingPermission") // Guarded by hasPermission() above.
    private fun lastKnown(manager: LocationManager): Location? =
        PROVIDER_PREFERENCE
            .filter { manager.allProviders.contains(it) }
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }

    private fun preferredProvider(manager: LocationManager): String? =
        PROVIDER_PREFERENCE.firstOrNull { provider ->
            manager.allProviders.contains(provider) &&
                runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
        }

    /**
     * Reverse geocoding uses the platform [Geocoder], which runs in a system
     * process — the app itself holds no INTERNET permission. When the device is
     * offline the lookup simply returns nothing and the record keeps its
     * coordinates.
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            runCatching {
                @Suppress("DEPRECATION")
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.let { address ->
                        (0..address.maxAddressLineIndex)
                            .mapNotNull { address.getAddressLine(it) }
                            .joinToString(", ")
                            .takeIf { it.isNotBlank() }
                    }
            }.getOrNull()
        }

    private companion object {
        const val FIX_TIMEOUT_MS = 6_000L
        val PROVIDER_PREFERENCE = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}
