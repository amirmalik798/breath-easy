package com.amirmalik.breatheasy

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First-launch smoke coverage: verifies Activity/Compose startup, database
 * initialization, and that Android can resolve the declared launcher icon.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesInitialScreenWithoutCrashing() {
        composeRule.onNodeWithText("Your day").assertIsDisplayed()
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
