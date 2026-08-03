package com.surestep.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surestep.app.data.prefs.ThemeMode
import com.surestep.app.export.ExportFormat
import com.surestep.app.export.ExportResult
import com.surestep.app.ui.components.SureStepCard
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun SettingsScreen(
    onManageTasks: () -> Unit,
    onManageReminders: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val recordCount by viewModel.recordCount.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showPinDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var photoStorage by remember { mutableStateOf("—") }

    // Recomputed whenever the record count changes, so deleting history is
    // reflected here without a manual refresh.
    LaunchedEffect(recordCount) {
        photoStorage = formatBytes(viewModel.photoStorageBytes())
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setNotificationsEnabled(granted)
    }

    val biometricAvailable = remember {
        BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SettingsEvent.Message -> snackbarHostState.showSnackbar(event.text)
                is SettingsEvent.Exported -> when (val result = event.result) {
                    is ExportResult.Success -> {
                        val action = snackbarHostState.showSnackbar(
                            message = "Exported ${result.recordCount} records",
                            actionLabel = "Share",
                        )
                        if (action == SnackbarResult.ActionPerformed) {
                            runCatching {
                                context.startActivity(
                                    viewModel.shareIntent(result.file, event.format),
                                )
                            }
                        }
                    }
                    ExportResult.NoRecords ->
                        snackbarHostState.showSnackbar("There are no records to export yet")
                    is ExportResult.Failed ->
                        snackbarHostState.showSnackbar("Export failed: ${result.message}")
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Settings", style = MaterialTheme.typography.headlineMedium)
            }

            item {
                SectionCard("What gets recorded") {
                    SettingSwitch(
                        title = "Take a selfie",
                        subtitle = "A short countdown, then one photo. Turning this off " +
                            "still records the time, and can make the app feel calmer.",
                        checked = settings.captureSelfie,
                        onCheckedChange = viewModel::setCaptureSelfie,
                    )
                    SettingSwitch(
                        title = "Record location",
                        subtitle = "Coordinates from the device's own GPS. Never sent anywhere.",
                        checked = settings.recordLocation,
                        onCheckedChange = viewModel::setRecordLocation,
                    )
                    SettingSwitch(
                        title = "Look up the address",
                        subtitle = "Turns coordinates into a street address using Android's " +
                            "built-in geocoder. Needs the phone to be online; the record " +
                            "keeps its coordinates either way.",
                        checked = settings.reverseGeocode,
                        enabled = settings.recordLocation,
                        onCheckedChange = viewModel::setReverseGeocode,
                    )
                    SettingSwitch(
                        title = "Record battery level",
                        subtitle = "Useful context if you ever question a record.",
                        checked = settings.recordBattery,
                        onCheckedChange = viewModel::setRecordBattery,
                    )

                    if (settings.captureSelfie) {
                        Spacer(Modifier.height(8.dp))
                        Text("Countdown", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 3, 5).forEach { seconds ->
                                FilterChip(
                                    selected = settings.countdownSeconds == seconds,
                                    onClick = { viewModel.setCountdownSeconds(seconds) },
                                    label = {
                                        Text(if (seconds == 0) "None" else "${seconds}s")
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard("Reminders") {
                    SettingSwitch(
                        title = "Allow reminders",
                        subtitle = "Reminders stay quiet when everything is already recorded.",
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { wanted ->
                            if (wanted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )
                            } else {
                                viewModel.setNotificationsEnabled(wanted)
                            }
                        },
                    )
                    NavigationRow(
                        title = "Scheduled reminders",
                        subtitle = "Choose your own times and days",
                        onClick = onManageReminders,
                    )
                }
            }

            item {
                SectionCard("Your checklist") {
                    NavigationRow(
                        title = "Manage tasks",
                        subtitle = "Add, rename, reorder, group, or remove tasks",
                        onClick = onManageTasks,
                    )
                }
            }

            item {
                SectionCard("Appearance") {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(mode.label) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SettingSwitch(
                        title = "High contrast",
                        subtitle = "Stronger text and outlines against plain backgrounds.",
                        checked = settings.highContrast,
                        onCheckedChange = viewModel::setHighContrast,
                    )
                }
            }

            item {
                SectionCard("Privacy") {
                    SettingSwitch(
                        title = "Require a PIN",
                        subtitle = if (settings.pinEnabled) {
                            "The app locks whenever it leaves the screen."
                        } else {
                            "Ask for a 4–8 digit PIN when the app opens."
                        },
                        checked = settings.pinEnabled,
                        onCheckedChange = { wanted ->
                            if (wanted) showPinDialog = true else viewModel.clearPin()
                        },
                    )
                    if (settings.pinEnabled) {
                        SettingSwitch(
                            title = "Unlock with fingerprint or face",
                            subtitle = if (biometricAvailable) {
                                "Uses the biometrics already set up on this phone."
                            } else {
                                "No biometrics are enrolled on this device."
                            },
                            checked = settings.biometricEnabled,
                            enabled = biometricAvailable,
                            onCheckedChange = viewModel::setBiometricEnabled,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SureStep has no internet permission. Records, photos and " +
                            "exports stay in this app's private storage until you share " +
                            "them yourself.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                SectionCard("Your data") {
                    Text(
                        text = if (recordCount == 1) "1 record stored" else "$recordCount records stored",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "$photoStorage of photos in this app's private storage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { viewModel.export(ExportFormat.CSV) },
                            label = { Text("Export CSV") },
                        )
                        AssistChip(
                            onClick = { viewModel.export(ExportFormat.PDF) },
                            label = { Text("Export PDF") },
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text(
                            text = "Delete all records",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            item {
                SectionCard("About this app") {
                    Text(
                        text = "SureStep is for recording a task once, on purpose, so that " +
                            "you have something to look at later instead of going back to " +
                            "check.\n\nIf you notice yourself opening a record over and over, " +
                            "or adding extra confirmations to feel sure, that is worth " +
                            "mentioning to a doctor or therapist. This app is a memory aid, " +
                            "not a treatment, and it is not designed to settle repeated doubt.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                viewModel.setPin(pin)
                showPinDialog = false
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete every record?") },
            text = {
                Text(
                    "All $recordCount records and their photos will be removed from this " +
                        "phone. There is no backup anywhere, so this cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAllHistory()
                    },
                ) { Text("Delete everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    SureStepCard {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            content()
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outline
                },
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun NavigationRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.width(16.dp),
        )
    }
}

@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val valid = pin.length in 4..8 && pin == confirmation

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set a PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(8) },
                    label = { Text("PIN (4–8 digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it.filter(Char::isDigit).take(8) },
                    label = { Text("Enter it again") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = confirmation.isNotEmpty() && confirmation != pin,
                )
                Text(
                    text = "There is no way to reset this PIN — nothing is stored off the " +
                        "device, so nobody can recover it for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pin) }, enabled = valid) { Text("Set PIN") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
