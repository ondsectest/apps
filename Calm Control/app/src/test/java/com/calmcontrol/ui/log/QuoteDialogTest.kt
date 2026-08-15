package com.calmcontrol.ui.log

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.calmcontrol.data.local.Outcome
import com.calmcontrol.domain.Quote
import com.calmcontrol.ui.theme.CalmControlTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuoteDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val quote = Quote(
        text = "The greatest remedy for anger is delay.",
        author = "Seneca",
        source = "On Anger, Book III",
    )

    @Test
    fun `a controlled moment is affirmed, never framed as the good answer over the other`() {
        composeRule.setContent {
            CalmControlTheme {
                QuoteDialog(moment = QuoteMoment(Outcome.CONTROLLED, quote), onDismiss = {})
            }
        }

        composeRule.onNodeWithText("That one counts").assertIsDisplayed()
        composeRule.onNodeWithText(quote.text).assertIsDisplayed()
        composeRule.onNodeWithText(quote.author).assertIsDisplayed()
        composeRule.onNodeWithText(quote.source!!).assertIsDisplayed()
    }

    @Test
    fun `an anger moment is met with honesty, not a reprimand`() {
        composeRule.setContent {
            CalmControlTheme {
                QuoteDialog(moment = QuoteMoment(Outcome.ANGER_EXPRESSED, quote), onDismiss = {})
            }
        }

        // The headline after "Got angry" is the app's whole honesty-doesn't-cost-you-something
        // promise. If this ever reads as a scold, people stop logging the red ones.
        composeRule.onNodeWithText("Thank you for being honest").assertIsDisplayed()
    }
}
