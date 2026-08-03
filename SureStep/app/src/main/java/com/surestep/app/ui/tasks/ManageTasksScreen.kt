package com.surestep.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surestep.app.domain.model.Task
import com.surestep.app.domain.model.TaskGroup
import com.surestep.app.ui.components.SureStepCard
import com.surestep.app.ui.components.TaskIconBadge
import com.surestep.app.ui.components.TaskIcons
import com.surestep.app.ui.theme.TaskPalette
import com.surestep.app.ui.theme.toAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTasksScreen(
    onBack: () -> Unit,
    viewModel: ManageTasksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Task?>(null) }
    var creating by remember { mutableStateOf(false) }
    var confirmArchive by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add a task")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(state.active, key = { _, task -> task.id }) { index, task ->
                TaskRow(
                    task = task,
                    canMoveUp = index > 0,
                    canMoveDown = index < state.active.lastIndex,
                    onMoveUp = { viewModel.move(task, -1) },
                    onMoveDown = { viewModel.move(task, 1) },
                    onEdit = { editing = task },
                    onArchive = { confirmArchive = task },
                )
            }

            if (state.archived.isNotEmpty()) {
                item {
                    Text(
                        text = "Removed from the checklist",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                    Text(
                        text = "Their past records are still in History.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(state.archived, key = { "archived_${it.id}" }) { task ->
                    SureStepCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(task.title, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.restore(task) }) {
                                Icon(
                                    Icons.Filled.Restore,
                                    contentDescription = "Put ${task.title} back on the checklist",
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (creating) {
        TaskEditorDialog(
            initial = null,
            onDismiss = { creating = false },
            onConfirm = { title, icon, color, group ->
                viewModel.add(title, icon, color, group)
                creating = false
            },
        )
    }

    editing?.let { task ->
        TaskEditorDialog(
            initial = task,
            onDismiss = { editing = null },
            onConfirm = { title, icon, color, group ->
                viewModel.update(task, title, icon, color, group)
                editing = null
            },
        )
    }

    confirmArchive?.let { task ->
        AlertDialog(
            onDismissRequest = { confirmArchive = null },
            title = { Text("Remove \"${task.title}\"?") },
            text = {
                Text(
                    "It comes off today's checklist. Every record you already made for " +
                        "it stays in History, and you can put it back later.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.archive(task)
                        confirmArchive = null
                    },
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { confirmArchive = null }) { Text("Cancel") }
            },
        )
    }
}

/**
 * Reordering uses explicit up/down buttons rather than drag-and-drop. They work
 * with a screen reader and with unsteady hands, which drag targets do not.
 */
@Composable
private fun TaskRow(
    task: Task,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
) {
    SureStepCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TaskIconBadge(iconKey = task.iconKey, accent = task.colorArgb.toAccentColor())
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = task.group.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = "Move ${task.title} up",
                    )
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(
                        Icons.Filled.ArrowDownward,
                        contentDescription = "Move ${task.title} down",
                    )
                }
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit ${task.title}")
                }
                IconButton(onClick = onArchive) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove ${task.title}")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskEditorDialog(
    initial: Task?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, iconKey: String, colorArgb: Long, group: TaskGroup) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: TaskIcons.defaultKey) }
    var colorArgb by remember { mutableLongStateOf(initial?.colorArgb ?: TaskPalette.options.first()) }
    var group by remember { mutableStateOf(initial?.group ?: TaskGroup.HOME) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New task" else "Edit task") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Group", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskGroup.entries.forEach { option ->
                        FilterChip(
                            selected = group == option,
                            onClick = { group = option },
                            label = { Text(option.label) },
                        )
                    }
                }

                Text("Colour", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPalette.options.forEach { option ->
                        val selected = option == colorArgb
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(option.toAccentColor())
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable { colorArgb = option },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Selected colour",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                Text("Icon", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskIcons.all.forEach { (key, _) ->
                        val selected = key == iconKey
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable { iconKey = key },
                        ) {
                            TaskIconBadge(
                                iconKey = key,
                                accent = colorArgb.toAccentColor(),
                                size = 42,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, iconKey, colorArgb, group) },
                enabled = title.isNotBlank(),
            ) { Text(if (initial == null) "Add" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
