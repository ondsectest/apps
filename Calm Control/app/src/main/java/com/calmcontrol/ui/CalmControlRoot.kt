package com.calmcontrol.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.calmcontrol.ui.log.LogScreen
import com.calmcontrol.ui.reports.ReportsScreen

private enum class Destination(val label: String) {
    LOG("Log"),
    REPORTS("Progress"),
}

/**
 * Two destinations, no navigation library — the graph is a pair of screens with no arguments and
 * no back stack worth modelling.
 *
 * Log is the start destination. Opening straight onto the charts would put the reckoning before
 * the practice; opening onto the buttons means the app is always one tap from its actual job.
 */
@Composable
fun CalmControlRoot(modifier: Modifier = Modifier) {
    var destination by rememberSaveable { mutableStateOf(Destination.LOG) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    Destination.LOG -> Icons.Default.Add
                                    Destination.REPORTS -> Icons.Default.DateRange
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        // Only the bottom inset is passed down. Each screen keeps its own top app bar, which
        // handles the status bar itself.
        Box(Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            when (destination) {
                Destination.LOG -> LogScreen()
                Destination.REPORTS -> ReportsScreen()
            }
        }
    }
}
