package com.calmcontrol.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calmcontrol.data.local.Outcome
import com.calmcontrol.data.local.TriggerCategory
import com.calmcontrol.ui.theme.calmColors

/**
 * Where a moment gets recorded. Two orbs, one question.
 *
 * This screen is used seconds after something went wrong, by someone who is not calm. That rules
 * out forms. Tap what happened, tap what set it off, done — the trigger step is one tap and can
 * be skipped entirely, because a logged moment with a vague cause beats no logged moment.
 *
 * Neither orb is styled as the wrong answer. "Got angry" is the same size, the same finish and
 * the same prominence as "Stayed calm"; only the hue differs. The instant this screen makes
 * honesty feel like confession, people stop logging the red ones and every chart starts lying.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = viewModel(factory = LogViewModel.Factory),
) {
    val summary by viewModel.todaySummary.collectAsStateWithLifecycle()

    var pendingOutcome by remember { mutableStateOf<Outcome?>(null) }
    var loggedMoment by remember { mutableStateOf<QuoteMoment?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calm Control") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "How did that moment go?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Logging it is the whole practice. There is no wrong button.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                MomentOrb(
                    label = "Stayed\ncalm",
                    baseColor = calmColors.controlled,
                    onClick = { pendingOutcome = Outcome.CONTROLLED },
                    modifier = Modifier.weight(1f),
                )
                MomentOrb(
                    label = "Got\nangry",
                    baseColor = calmColors.anger,
                    onClick = { pendingOutcome = Outcome.ANGER_EXPRESSED },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(48.dp))

            summary?.let { today ->
                Text(
                    text = if (today.hasData) {
                        "Today: ${today.controlledCount} calm · ${today.angerCount} expressed"
                    } else {
                        "Nothing logged yet today."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    val outcome = pendingOutcome
    if (outcome != null) {
        ModalBottomSheet(
            onDismissRequest = { pendingOutcome = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            TriggerPicker(
                onPick = { category ->
                    loggedMoment = viewModel.log(outcome, category)
                    pendingOutcome = null
                },
            )
        }
    }

    loggedMoment?.let { moment ->
        QuoteDialog(moment = moment, onDismiss = { loggedMoment = null })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TriggerPicker(onPick: (TriggerCategory) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "What set it off?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(16.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TriggerCategory.entries.forEach { category ->
                AssistChip(
                    onClick = { onPick(category) },
                    label = { Text(category.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onPick(TriggerCategory.OTHER) }) {
            Text("Skip — just log it")
        }
    }
}
