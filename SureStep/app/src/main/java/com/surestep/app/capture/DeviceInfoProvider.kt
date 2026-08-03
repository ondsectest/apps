package com.surestep.app.capture

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** e.g. "Samsung SM-S921B". Manufacturer is dropped when the model already contains it. */
    fun deviceModel(): String {
        val manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() }
        val model = Build.MODEL.orEmpty()
        return when {
            model.isBlank() -> manufacturer.ifBlank { "Unknown device" }
            model.startsWith(manufacturer, ignoreCase = true) -> model
            else -> "$manufacturer $model".trim()
        }
    }

    fun batteryPercent(): Int? {
        val manager = context.getSystemService<BatteryManager>() ?: return null
        return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            .takeIf { it in 0..100 }
    }

    /**
     * A human-readable note about connectivity at record time. SureStep never
     * uses the network; this is recorded only because it helps explain why a
     * reverse-geocoded address may be missing from a given record.
     */
    fun networkSummary(): String {
        val manager = context.getSystemService<ConnectivityManager>() ?: return "Unknown"
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
            ?: return "Offline"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }
    }
}
