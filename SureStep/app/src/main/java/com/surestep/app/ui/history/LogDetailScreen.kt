package com.surestep.app.ui.history

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.surestep.app.domain.model.TaskLog
import com.surestep.app.ui.components.Formatters
import com.surestep.app.ui.components.LabeledRow
import com.surestep.app.ui.components.SureStepCard
import com.surestep.app.ui.components.TaskIconBadge
import com.surestep.app.ui.theme.toAccentColor
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    onBack: () -> Unit,
    viewModel: LogDetailViewModel = hiltViewModel(),
) {
    val log by viewModel.log.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete this record")
                    }
                },
            )
        },
    ) { padding ->
        val current = log
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("This record is no longer available.")
            }
            return@Scaffold
        }

        DetailContent(
            log = current,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            onSaveNotes = viewModel::saveNotes,
            onOpenInMaps = {
                if (current.hasLocation) {
                    val label = Uri.encode(current.taskTitle)
                    val uri = String.format(
                        Locale.ROOT,
                        "geo:%f,%f?q=%f,%f(%s)",
                        current.latitude, current.longitude,
                        current.latitude, current.longitude, label,
                    ).toUri()
                    // Hands off to whatever map app the user already has. No map
                    // SDK is bundled, so nothing is fetched by SureStep itself.
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this record?") },
            text = {
                Text(
                    "The photo and everything saved with it will be removed from this " +
                        "phone. This cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecord(onBack)
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Keep") }
            },
        )
    }
}

@Composable
private fun DetailContent(
    log: TaskLog,
    modifier: Modifier,
    onSaveNotes: (String) -> Unit,
    onOpenInMaps: () -> Unit,
) {
    val accent = log.colorArgb.toAccentColor()
    var notes by remember(log.id) { mutableStateOf(log.notes.orEmpty()) }

    // Notes save themselves shortly after typing stops; there is no save button
    // to forget to press.
    LaunchedEffect(notes) {
        if (notes != log.notes.orEmpty()) {
            kotlinx.coroutines.delay(600)
            onSaveNotes(notes)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TaskIconBadge(iconKey = log.iconKey, accent = accent, size = 54)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(log.taskTitle, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = Formatters.date(log.recordedAt),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (log.photoPath != null && File(log.photoPath).exists()) {
            AsyncImage(
                model = File(log.photoPath),
                contentDescription = "Photo taken when ${log.taskTitle} was recorded",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop,
            )
        }

        SureStepCard {
            Column {
                Text("When", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                LabeledRow("Time", Formatters.preciseTime(log.recordedAt))
                LabeledRow("Date", Formatters.date(log.recordedAt))
                LabeledRow("Time zone", log.zoneId)
            }
        }

        if (log.hasLocation) {
            SureStepCard {
                Column {
                    Text("Where", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    log.address?.let { LabeledRow("Address", it) }
                    LabeledRow(
                        "Coordinates",
                        Formatters.coordinates(log.latitude!!, log.longitude!!),
                    )
                    log.accuracyMeters?.let {
                        LabeledRow("Accuracy", "±${it.toInt()} m")
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = onOpenInMaps, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Map, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open in your map app")
                    }
                }
            }
        }

        SureStepCard {
            Column {
                Text("Device", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                log.deviceModel?.let { LabeledRow("Model", it) }
                log.batteryPercent?.let { LabeledRow("Battery", "$it%") }
                log.networkSummary?.let { LabeledRow("Network", it) }
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Text(
                    text = "Saved only on this phone. Nothing here has been uploaded anywhere.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            placeholder = { Text("Anything you want to remember about this one") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )

        Spacer(Modifier.height(24.dp))
    }
}
