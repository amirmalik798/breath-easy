package com.amirmalik.breatheasy

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.waitUntil
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies launcher metadata and exercises Activity creation, ViewModel
 * construction, SQLite initialization and the first Compose screen.
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

    @Test
    fun packageExposesResolvableLauncherIconAndLaunchIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)

        assertTrue("Application icon resource must be declared", applicationInfo.icon != 0)
        assertNotNull("Launcher icon must resolve", packageManager.getApplicationIcon(applicationInfo))
        assertNotNull("Package must expose a launcher intent", packageManager.getLaunchIntentForPackage(context.packageName))
    }
}
