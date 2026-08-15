package com.calmcontrol.ui.reports.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.calmcontrol.domain.DailySummary
import com.calmcontrol.ui.theme.CalmControlTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DailySummaryCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 8, 15)

    @Test
    fun `ring centre shows the self-control rate, never the anger rate`() {
        val summary = DailySummary(date = today, total = 10, controlledCount = 7, angerCount = 3)

        composeRule.setContent {
            CalmControlTheme { DailySummaryCard(summary) }
        }

        // 70% controlled, not 30% anger — the centre number is the most consequential decision
        // on this screen, per the README's design rules.
        composeRule.onNodeWithText("70%").assertIsDisplayed()
        composeRule.onNodeWithText("7").assertIsDisplayed()
        composeRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun `shows a quiet-day message instead of a zero-value ring when nothing was logged`() {
        composeRule.setContent {
            CalmControlTheme { DailySummaryCard(DailySummary.empty(today)) }
        }

        composeRule.onNodeWithText("No moments logged yet today. A quiet day counts too.")
            .assertIsDisplayed()
    }
}
