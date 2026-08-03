package com.surestep.app.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.surestep.app.domain.model.TaskLog
import com.surestep.app.ui.components.EmptyState
import com.surestep.app.ui.components.Formatters
import com.surestep.app.ui.components.SureStepCard
import com.surestep.app.ui.components.TaskIconBadge
import com.surestep.app.ui.theme.toAccentColor

@Composable
fun HistoryScreen(
    onOpenLog: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val total by viewModel.totalRecords.collectAsStateWithLifecycle()
    val logs = viewModel.logs.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = if (total == 1) "1 record" else "$total records",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                placeholder = { Text("Search task, note, place or date") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
            )
        }

        val refreshing = logs.loadState.refresh is LoadState.Loading
        val isEmpty = logs.itemCount == 0 && !refreshing

        when {
            refreshing && logs.itemCount == 0 -> Box(
                Modifier.fillMaxSize(),
                Alignment.Center,
            ) { CircularProgressIndicator() }

            isEmpty -> EmptyState(
                icon = Icons.Filled.HistoryToggleOff,
                title = if (query.isBlank()) "No records yet" else "Nothing matches \"$query\"",
                body = if (query.isBlank()) {
                    "Confirm a task on the Home tab and it will appear here with the time, " +
                        "and the place and photo if you have those switched on."
                } else {
                    "Try a task name, a date like 2026-08-03, or a word from a note."
                },
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    count = logs.itemCount,
                    key = logs.itemKey { it.id },
                ) { index ->
                    val log = logs[index]
                    if (log != null) {
                        HistoryCard(log = log, onClick = { onOpenLog(log.id) })
                    }
                }

                if (logs.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            Alignment.Center,
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(log: TaskLog, onClick: () -> Unit) {
    val accent = log.colorArgb.toAccentColor()
    val recordedAt = log.recordedAt

    SureStepCard(
        modifier = Modifier
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = buildString {
                    append("${log.taskTitle}, recorded ")
                    append(Formatters.relativeDay(log.localDate))
                    append(" at ${Formatters.time(recordedAt)}")
                    log.address?.let { append(", at $it") }
                }
            },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TaskIconBadge(iconKey = log.iconKey, accent = accent)
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = log.taskTitle,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${Formatters.relativeDay(log.localDate)} · ${Formatters.time(recordedAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (log.photoPath != null) {
                        MetaChip(icon = Icons.Filled.PhotoCamera, label = "Photo")
                    }
                    if (log.hasLocation) {
                        MetaChip(
                            icon = Icons.Filled.Place,
                            label = log.address?.substringBefore(",") ?: "Location",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
