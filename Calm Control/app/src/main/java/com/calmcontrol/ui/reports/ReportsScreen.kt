package com.calmcontrol.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calmcontrol.ui.reports.components.DailySummaryCard
import com.calmcontrol.ui.reports.components.ReflectionCard
import com.calmcontrol.ui.reports.components.SectionCard
import com.calmcontrol.ui.reports.components.TrendLineChart
import com.calmcontrol.ui.reports.components.TriggerBreakdownCard
import com.calmcontrol.ui.reports.components.WeeklyBarChart

/**
 * The Reports screen.
 *
 * Ordered from immediate to reflective: today, this week, the long trend, patterns, then words.
 * A user opening this after a hard afternoon meets today's ring first, and today's ring leads
 * with what they managed rather than what they didn't.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = viewModel(factory = ReportsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        // The root already applies the navigation bar inset; the top app bar handles the status
        // bar on its own.
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeTopAppBar(
                title = { Text("Your progress") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!state.hasAnyData) {
                    item { WelcomeCard() }
                }

                item {
                    state.daily?.let { DailySummaryCard(it) }
                }
                item {
                    WeeklyBarChart(state.week)
                }
                item {
                    TrendLineChart(
                        points = state.trend,
                        range = state.trendRange,
                        onRangeChange = viewModel::selectRange,
                    )
                }
                item {
                    TriggerBreakdownCard(
                        triggers = state.triggers,
                        strongestArea = state.strongestArea,
                    )
                }
                item {
                    ReflectionCard(state.reflection)
                }
            }
        }
    }
}

/**
 * Shown before there is anything to chart. The charts below stay on screen in their own empty
 * states rather than being hidden, so the shape of what's coming is already familiar.
 */
@Composable
private fun WelcomeCard(modifier: Modifier = Modifier) {
    SectionCard(
        title = "Your first moment starts the picture",
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Every trigger you log — whether you stayed calm or not — adds to what you " +
                "can see here. Noticing is the whole skill.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
