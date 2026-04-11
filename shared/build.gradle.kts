plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.21"
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.2")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("app.cash.sqldelight:runtime:2.3.2")
            api("com.russhwolf:multiplatform-settings:1.1.1")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
        }
        androidMain.dependencies {
            implementation("androidx.appcompat:appcompat:1.7.0")
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("app.cash.sqldelight:android-driver:2.3.2")
        }
        iosMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:2.3.2")
        }
    }
}

sqldelight {
    databases {
        create("TimetableDatabase") {
            packageName.set("com.example.timetable.db")
        }
    }
}

android {
    namespace = "com.example.timetable.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
