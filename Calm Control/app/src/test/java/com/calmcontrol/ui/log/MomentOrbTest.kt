package com.calmcontrol.ui.log

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.calmcontrol.ui.theme.CalmControlTheme
import com.calmcontrol.ui.theme.calmColors
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MomentOrbTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `tapping the orb fires onClick exactly once`() {
        var clicks = 0

        composeRule.setContent {
            CalmControlTheme {
                MomentOrb(
                    label = "Stayed\ncalm",
                    baseColor = calmColors.controlled,
                    onClick = { clicks++ },
                    modifier = Modifier.size(120.dp),
                )
            }
        }

        composeRule.onNodeWithText("Stayed\ncalm").performClick()

        assertEquals(1, clicks)
    }
}
