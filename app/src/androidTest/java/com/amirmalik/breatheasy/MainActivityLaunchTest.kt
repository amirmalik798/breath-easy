package com.amirmalik.breatheasy

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.waitUntil
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First-launch smoke test: exercises Activity creation, ViewModel construction,
 * SQLite initialization, receiver registration and the initial Compose screen.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesInitialScreenWithoutCrashing() {
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithText("Your day").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Your day").assertIsDisplayed()
        composeRule.onNodeWithText("Log cigarette").assertIsDisplayed()
    }
}
