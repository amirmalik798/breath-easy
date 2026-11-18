plugins {
    // AGP 9.5 provides built-in Kotlin support for Android modules.
    id("com.android.application") version "9.5.0" apply false
    // Keep the Compose compiler plugin on the Kotlin line documented for AGP 9.5.
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}
