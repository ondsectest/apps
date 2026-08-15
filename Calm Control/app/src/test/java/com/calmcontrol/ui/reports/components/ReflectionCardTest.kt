package com.calmcontrol.ui.reports.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.calmcontrol.ui.theme.CalmControlTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReflectionCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `shows the placeholder when there is nothing to reflect on yet`() {
        composeRule.setContent {
            CalmControlTheme { ReflectionCard(lines = emptyList()) }
        }

        composeRule.onNodeWithText("Your first month of reflections is on its way.")
            .assertIsDisplayed()
    }

    @Test
    fun `renders every reflection line`() {
        val lines = listOf(
            "Your controlled responses increased by 20% this month.",
            "Family came up most often, and you met it calmly 3 times.",
        )

        composeRule.setContent {
            CalmControlTheme { ReflectionCard(lines = lines) }
        }

        lines.forEach { line -> composeRule.onNodeWithText(line).assertIsDisplayed() }
    }
}
