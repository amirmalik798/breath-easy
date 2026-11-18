buildscript {
    dependencies {
        // AGP 9 has built-in Kotlin support; pin its Kotlin Gradle plugin runtime explicitly.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.5.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
