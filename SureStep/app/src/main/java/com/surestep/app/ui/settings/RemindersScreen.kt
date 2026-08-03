package com.surestep.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surestep.app.data.local.entity.ReminderEntity
import com.surestep.app.ui.components.EmptyState
import com.surestep.app.ui.components.Formatters
import com.surestep.app.ui.components.SureStepCard
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add a reminder")
            }
        },
    ) { padding ->
        if (reminders.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.NotificationsNone,
                title = "No reminders yet",
                body = "Add a time — before you usually leave, or last thing at night. " +
                    "A reminder stays quiet if everything is already recorded.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { viewModel.setEnabled(reminder, it) },
                        onDelete = { viewModel.delete(reminder) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { label, hour, minute, mask ->
                viewModel.add(label, hour, minute, mask)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: ReminderEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    SureStepCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = Formatters.time(LocalTime.of(reminder.hour, reminder.minute)),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(reminder.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = describeDays(reminder.daysOfWeekMask),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = reminder.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Delete the ${reminder.label} reminder",
                )
            }
        }
    }
}

private fun describeDays(mask: Int): String = when (mask) {
    RemindersViewModel.EVERY_DAY_MASK -> "Every day"
    RemindersViewModel.WEEKDAYS_MASK -> "Weekdays"
    0 -> "No days selected"
    else -> DayOfWeek.entries
        .filter { day -> mask and (1 shl (day.value - 1)) != 0 }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, hour: Int, minute: Int, mask: Int) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    var hourText by remember { mutableStateOf("08") }
    var minuteText by remember { mutableStateOf("30") }
    var mask by remember { mutableIntStateOf(RemindersViewModel.EVERY_DAY_MASK) }

    val hour = hourText.toIntOrNull()
    val minute = minuteText.toIntOrNull()
    val valid = hour != null && minute != null &&
        hour in 0..23 && minute in 0..59 && mask != 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("What is it for?") },
                    placeholder = { Text("Prepare for work") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(96.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(96.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "24-hour",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("Days", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DayOfWeek.entries.forEach { day ->
                        val bit = 1 shl (day.value - 1)
                        FilterChip(
                            selected = mask and bit != 0,
                            onClick = { mask = mask xor bit },
                            label = {
                                Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                            },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(label, hour ?: 8, minute ?: 30, mask) },
                enabled = valid,
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
