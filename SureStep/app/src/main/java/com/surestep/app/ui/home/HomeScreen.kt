package com.surestep.app.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surestep.app.domain.model.Task
import com.surestep.app.domain.model.TaskGroup
import com.surestep.app.ui.components.Formatters
import com.surestep.app.ui.components.StatTile
import com.surestep.app.ui.components.SureStepCard
import com.surestep.app.ui.components.TaskIconBadge
import com.surestep.app.ui.theme.onAccentColor
import com.surestep.app.ui.theme.toAccentColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(
    onOpenCapture: () -> Unit,
    onManageTasks: () -> Unit,
    onOpenLog: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var successBanner by remember { mutableStateOf<String?>(null) }
    var awaitingPermissionFor by remember { mutableStateOf<Task?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        // Whatever the user decided, the confirmation still gets recorded — with
        // a selfie and a location if allowed, without them if not.
        val task = awaitingPermissionFor
        awaitingPermissionFor = null
        if (task != null) {
            viewModel.confirm(task, cameraAvailable = context.canUseCamera())
        } else {
            viewModel.releaseConfirmationLock()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                HomeEvent.OpenCamera -> onOpenCapture()
                is HomeEvent.Recorded -> {
                    successBanner = event.taskTitle
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
                is HomeEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    // The confirmation flourish is brief on purpose: acknowledge, then get out
    // of the way.
    LaunchedEffect(successBanner) {
        if (successBanner != null) {
            kotlinx.coroutines.delay(1_800)
            successBanner = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "header") {
                    HomeHeader(onManageTasks = onManageTasks)
                }

                item(key = "dashboard") {
                    DashboardCard(state = state)
                }

                if (state.loading) {
                    item(key = "loading") {
                        Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                state.grouped.forEach { (group, items) ->
                    item(key = "group_${group.name}") {
                        GroupHeading(group = group, items = items)
                    }
                    items(items, key = { it.task.id }) { item ->
                        ChecklistRow(
                            item = item,
                            captureSelfieEnabled = state.settings.captureSelfie,
                            onConfirm = {
                                val missing = context.missingCapturePermissions(
                                    wantsCamera = state.settings.captureSelfie,
                                    wantsLocation = state.settings.recordLocation,
                                )
                                if (missing.isEmpty()) {
                                    viewModel.confirm(
                                        item.task,
                                        cameraAvailable = context.canUseCamera(),
                                    )
                                } else {
                                    awaitingPermissionFor = item.task
                                    permissionLauncher.launch(missing.toTypedArray())
                                }
                            },
                            onOpenRecord = { item.lastLogId?.let(onOpenLog) },
                        )
                    }
                }

                item(key = "add_task") {
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add custom task")
                    }
                }

                item(key = "principle") {
                    ReassuranceNote()
                }
            }

            RecordedOverlay(taskTitle = successBanner)
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title ->
                viewModel.addQuickTask(title)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun HomeHeader(onManageTasks: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Today's checklist",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = Formatters.date(LocalDate.now()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onManageTasks) {
            Icon(Icons.Filled.Tune, contentDescription = "Manage tasks")
        }
    }
}

@Composable
private fun DashboardCard(state: HomeUiState) {
    val stats = state.stats
    val progress by animateFloatAsState(
        targetValue = if (stats.totalToday == 0) 0f else stats.completedToday.toFloat() / stats.totalToday,
        label = "completion",
    )

    SureStepCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${stats.completionPercent}%",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (stats.totalToday == 0) {
                        "No tasks on your checklist yet"
                    } else {
                        "${stats.completedToday} of ${stats.totalToday} recorded today"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .semantics {
                        contentDescription = "Today's completion: ${stats.completionPercent} percent"
                    },
                color = MaterialTheme.colorScheme.primary,
                // An explicit muted track; the default container colour is close
                // enough to the fill that an empty bar reads as a full one.
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatTile(value = stats.completedToday.toString(), label = "Recorded")
                StatTile(value = stats.remainingToday.toString(), label = "Remaining")
                StatTile(
                    value = stats.streakDays.toString(),
                    label = "Day streak",
                    accent = MaterialTheme.colorScheme.secondary,
                )
                StatTile(
                    value = stats.missedYesterday.toString(),
                    label = "Missed\nyesterday",
                    accent = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GroupHeading(group: TaskGroup, items: List<ChecklistItem>) {
    val done = items.count { it.isRecordedToday }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = group.label,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "$done / ${items.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Two distinct states, on purpose.
 *
 * Before: one large, obvious button. After: the record itself, with no button
 * inviting the user to do it again. Re-recording is still possible from the
 * record's own screen, but it is not offered here — repeating the confirmation
 * is the habit this app is meant to replace, not encourage.
 */
@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    captureSelfieEnabled: Boolean,
    onConfirm: () -> Unit,
    onOpenRecord: () -> Unit,
) {
    val accent = item.task.colorArgb.toAccentColor()
    SureStepCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TaskIconBadge(iconKey = item.task.iconKey, accent = accent)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.task.title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (item.isRecordedToday) {
                        val time = Instant.ofEpochMilli(item.recordedAtMillis!!)
                            .atZone(ZoneId.systemDefault())
                        Text(
                            text = "Recorded at ${Formatters.timeWithSeconds(time)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                if (item.isRecordedToday) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            if (item.isRecordedToday) {
                OutlinedButton(
                    onClick = onOpenRecord,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) {
                    Text("View the record")
                }
            } else {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .semantics {
                            contentDescription =
                                "Confirm ${item.task.title} is done and save a record"
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = onAccentColor(accent),
                    ),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Record as done",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (captureSelfieEnabled) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReassuranceNote() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
    ) {
        Text(
            text = "Record each task once. If you find yourself wondering later, " +
                "open History and read the record instead of going back to check.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun RecordedOverlay(taskTitle: String?) {
    AnimatedVisibility(
        visible = taskTitle != null,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(12.dp).size(36.dp),
                        )
                    }
                    Text(
                        text = "Task recorded successfully",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    if (taskTitle != null) {
                        Text(
                            text = taskTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a task") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What do you want to record?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

// --- permission helpers ----------------------------------------------------

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * FEATURE_CAMERA_ANY rather than FEATURE_CAMERA_FRONT: the capture screen
 * prefers the front lens but falls back to the rear one, so a device without a
 * selfie camera should still get a photo on the record.
 */
private fun Context.canUseCamera(): Boolean =
    isGranted(Manifest.permission.CAMERA) &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

/**
 * Permissions asked for at the moment they first matter — on the first
 * confirmation — rather than in an upfront wall the user has no context for.
 */
private fun Context.missingCapturePermissions(
    wantsCamera: Boolean,
    wantsLocation: Boolean,
): List<String> = buildList {
    if (wantsCamera &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) &&
        !isGranted(Manifest.permission.CAMERA)
    ) {
        add(Manifest.permission.CAMERA)
    }
    if (wantsLocation && !isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}
