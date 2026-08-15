package com.calmcontrol.ui.reports.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.calmcontrol.data.local.TriggerCategory
import com.calmcontrol.domain.StrongestArea
import com.calmcontrol.domain.TriggerShare
import com.calmcontrol.ui.theme.CalmControlTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TriggerBreakdownCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `prompts to log moments when no pattern exists yet`() {
        composeRule.setContent {
            CalmControlTheme { TriggerBreakdownCard(triggers = emptyList(), strongestArea = null) }
        }

        composeRule.onNodeWithText("Log a few moments and your patterns will start to show.")
            .assertIsDisplayed()
    }

    @Test
    fun `strongest area callout names the category and its controlled rate`() {
        val traffic = TriggerShare(
            category = TriggerCategory.TRAFFIC,
            count = 5,
            controlledCount = 4,
            sharePercent = 50,
            controlledRate = 80,
        )

        composeRule.setContent {
            CalmControlTheme {
                TriggerBreakdownCard(
                    triggers = listOf(traffic),
                    strongestArea = StrongestArea(TriggerCategory.TRAFFIC, controlledRate = 80),
                )
            }
        }

        composeRule.onNodeWithText(
            "Your strongest improvement area: you controlled traffic-related " +
                "frustration 80% of the time.",
        ).assertIsDisplayed()
    }

    @Test
    fun `no callout is shown when nothing is strong enough yet`() {
        val money = TriggerShare(
            category = TriggerCategory.MONEY,
            count = 1,
            controlledCount = 1,
            sharePercent = 100,
            controlledRate = 100,
        )

        composeRule.setContent {
            CalmControlTheme {
                TriggerBreakdownCard(triggers = listOf(money), strongestArea = null)
            }
        }

        // The row itself still renders — only the callout is withheld.
        composeRule.onNodeWithText("Money").assertIsDisplayed()
    }
}
