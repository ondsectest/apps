package com.surestep.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surestep.app.domain.model.DayStatus
import com.surestep.app.domain.model.DaySummary
import com.surestep.app.ui.components.Formatters
import com.surestep.app.ui.theme.LocalStatusColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onOpenDay: (LocalDate) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Calendar",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
            }
            Text(
                text = Formatters.monthTitle(month.atDay(1)),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = viewModel::nextMonth,
                enabled = viewModel.canGoForward(),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
            }
        }

        WeekdayHeader()

        // Leading blanks so the 1st lands under the right weekday.
        val firstOfMonth = month.atDay(1)
        val leadingBlanks = (firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        val cells = buildList<LocalDate?> {
            repeat(leadingBlanks) { add(null) }
            (1..month.lengthOfMonth()).forEach { day -> add(month.atDay(day)) }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(cells) { date ->
                if (date == null) {
                    Spacer(Modifier.aspectRatio(1f))
                } else {
                    DayCell(
                        date = date,
                        summary = summaries[date],
                        isToday = date == today,
                        onClick = { onOpenDay(date) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Legend()
    }
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    summary: DaySummary?,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val statusColors = LocalStatusColors.current
    val status = summary?.status ?: DayStatus.NONE
    val fill = when (status) {
        DayStatus.COMPLETE -> statusColors.complete
        DayStatus.PARTIAL -> statusColors.partial
        DayStatus.MISSED -> statusColors.missed
        DayStatus.NONE -> Color.Transparent
    }
    val hasFill = status != DayStatus.NONE

    val description = buildString {
        append(Formatters.date(date))
        append(", ")
        append(
            when (status) {
                DayStatus.COMPLETE -> "all tasks recorded"
                DayStatus.PARTIAL -> "${summary?.completed} of ${summary?.total} recorded"
                DayStatus.MISSED -> "nothing recorded"
                DayStatus.NONE -> "no records"
            },
        )
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (hasFill) fill.copy(alpha = 0.22f) else Color.Transparent)
            .then(
                if (isToday) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            )
            if (hasFill) {
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(fill),
                )
            }
        }
    }
}

/**
 * Colour alone never carries the meaning — each day cell also announces its
 * state to a screen reader, and the legend spells the colours out.
 */
@Composable
private fun Legend() {
    val statusColors = LocalStatusColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LegendItem(statusColors.complete, "All done")
        LegendItem(statusColors.partial, "Some done")
        LegendItem(statusColors.missed, "None")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
