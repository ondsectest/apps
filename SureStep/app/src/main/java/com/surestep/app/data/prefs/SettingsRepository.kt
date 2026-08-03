package com.surestep.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "surestep_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val CAPTURE_SELFIE = booleanPreferencesKey("capture_selfie")
        val RECORD_LOCATION = booleanPreferencesKey("record_location")
        val RECORD_BATTERY = booleanPreferencesKey("record_battery")
        val REVERSE_GEOCODE = booleanPreferencesKey("reverse_geocode")
        val COUNTDOWN_SECONDS = intPreferencesKey("countdown_seconds")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC = booleanPreferencesKey("biometric_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val SEEN_WELCOME = booleanPreferencesKey("seen_welcome")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { cause ->
            // A corrupt preferences file should not stop the app from opening —
            // the records in Room are the data that matters.
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }
        .map { prefs ->
            val defaults = AppSettings()
            AppSettings(
                captureSelfie = prefs[Keys.CAPTURE_SELFIE] ?: defaults.captureSelfie,
                recordLocation = prefs[Keys.RECORD_LOCATION] ?: defaults.recordLocation,
                recordBattery = prefs[Keys.RECORD_BATTERY] ?: defaults.recordBattery,
                reverseGeocode = prefs[Keys.REVERSE_GEOCODE] ?: defaults.reverseGeocode,
                countdownSeconds = prefs[Keys.COUNTDOWN_SECONDS] ?: defaults.countdownSeconds,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: defaults.notificationsEnabled,
                pinEnabled = prefs[Keys.PIN_ENABLED] ?: defaults.pinEnabled,
                biometricEnabled = prefs[Keys.BIOMETRIC] ?: defaults.biometricEnabled,
                themeMode = prefs[Keys.THEME_MODE]
                    ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                    ?: defaults.themeMode,
                highContrast = prefs[Keys.HIGH_CONTRAST] ?: defaults.highContrast,
                hasSeenWelcome = prefs[Keys.SEEN_WELCOME] ?: defaults.hasSeenWelcome,
            )
        }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setCaptureSelfie(enabled: Boolean) = put(Keys.CAPTURE_SELFIE, enabled)
    suspend fun setRecordLocation(enabled: Boolean) = put(Keys.RECORD_LOCATION, enabled)
    suspend fun setRecordBattery(enabled: Boolean) = put(Keys.RECORD_BATTERY, enabled)
    suspend fun setReverseGeocode(enabled: Boolean) = put(Keys.REVERSE_GEOCODE, enabled)
    suspend fun setNotificationsEnabled(enabled: Boolean) = put(Keys.NOTIFICATIONS, enabled)
    suspend fun setBiometricEnabled(enabled: Boolean) = put(Keys.BIOMETRIC, enabled)
    suspend fun setHighContrast(enabled: Boolean) = put(Keys.HIGH_CONTRAST, enabled)
    suspend fun setSeenWelcome(seen: Boolean) = put(Keys.SEEN_WELCOME, seen)

    suspend fun setCountdownSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.COUNTDOWN_SECONDS] = seconds.coerceIn(0, 10) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    // --- PIN ---------------------------------------------------------------

    /**
     * Stores a salted SHA-256 digest of the PIN. The PIN itself is never
     * written to disk, and there is nowhere for it to go off-device.
     */
    suspend fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_SALT] = salt.toHex()
            prefs[Keys.PIN_HASH] = hash(pin, salt)
            prefs[Keys.PIN_ENABLED] = true
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.PIN_HASH)
            prefs.remove(Keys.PIN_SALT)
            prefs[Keys.PIN_ENABLED] = false
            prefs[Keys.BIOMETRIC] = false
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val storedHash = prefs[Keys.PIN_HASH] ?: return false
        val salt = prefs[Keys.PIN_SALT]?.fromHex() ?: return false
        return constantTimeEquals(storedHash, hash(pin, salt))
    }

    private suspend fun put(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(pin.toByteArray(Charsets.UTF_8)).toHex()
    }

    /** Comparison that does not leak how many leading characters matched. */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { byte -> "%02x".format(byte) }

    private fun String.fromHex(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
