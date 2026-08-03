package com.surestep.app.data.prefs

enum class ThemeMode(val label: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark"),
}

data class AppSettings(
    /**
     * Photo capture is a setting, not a fixture. Some users find an automatic
     * selfie reassuring; for others it turns one record into another ritual.
     * Turning it off leaves the timestamp record fully intact.
     */
    val captureSelfie: Boolean = true,
    val recordLocation: Boolean = true,
    val recordBattery: Boolean = true,
    val reverseGeocode: Boolean = true,
    val countdownSeconds: Int = 3,

    val notificationsEnabled: Boolean = true,

    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,

    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val highContrast: Boolean = false,

    val hasSeenWelcome: Boolean = false,
)
