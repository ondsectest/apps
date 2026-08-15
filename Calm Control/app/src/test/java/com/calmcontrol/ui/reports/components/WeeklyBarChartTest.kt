package com.calmcontrol.ui.reports.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.calmcontrol.domain.DayBucket
import com.calmcontrol.ui.theme.CalmControlTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WeeklyBarChartTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val monday = LocalDate.of(2026, 8, 10)

    private fun emptyWeek() = (0 until 7).map { offset ->
        DayBucket(
            date = monday.plusDays(offset.toLong()),
            label = monday.plusDays(offset.toLong()).dayOfWeek.name.take(3),
            controlledCount = 0,
            angerCount = 0,
            isInFuture = false,
        )
    }

    @Test
    fun `a week with nothing logged shows the empty-week message`() {
        composeRule.setContent {
            CalmControlTheme { WeeklyBarChart(emptyWeek()) }
        }

        composeRule.onNodeWithText("This week's picture starts with your first logged moment.")
            .assertIsDisplayed()
    }

    @Test
    fun `a week with data prompts to tap a day rather than printing every value`() {
        val week = emptyWeek().toMutableList()
        week[0] = week[0].copy(controlledCount = 3, angerCount = 1)

        composeRule.setContent {
            CalmControlTheme { WeeklyBarChart(week) }
        }

        composeRule.onNodeWithText("Tap a day for its numbers").assertIsDisplayed()
    }
}
