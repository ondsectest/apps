package com.surestep.app.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surestep.app.ui.components.Formatters
import com.surestep.app.ui.components.SureStepCard
import com.surestep.app.ui.components.TaskIconBadge
import com.surestep.app.ui.theme.toAccentColor
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    date: LocalDate,
    onBack: () -> Unit,
    onOpenLog: (Long) -> Unit,
    viewModel: DayDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Formatters.relativeDay(date)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = if (state.logs.isEmpty()) {
                        "No records on this day"
                    } else {
                        "${state.logs.size} recorded"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            items(state.logs, key = { it.id }) { log ->
                val accent = log.colorArgb.toAccentColor()
                SureStepCard(modifier = Modifier.clickable { onOpenLog(log.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TaskIconBadge(iconKey = log.iconKey, accent = accent)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(log.taskTitle, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = Formatters.timeWithSeconds(log.recordedAt),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (state.unrecorded.isNotEmpty()) {
                item {
                    Text(
                        text = "Not recorded",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                items(state.unrecorded, key = { "missing_${it.id}" }) { task ->
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    Text(
                        text = "A blank here means nothing was recorded — not that the task " +
                            "wasn't done. Past days can't be filled in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}
