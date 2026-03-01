plugins {
    id("com.android.application")
    kotlin("android")
codex/generate-kotlin-multiplatform-tinder-like-app-wto2h7
    kotlin("plugin.compose")
    
 codex/generate-kotlin-multiplatform-tinder-like-app-g348hc
    kotlin("plugin.compose")

 main
 main
}

android {
    namespace = "com.sportswipe.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sportswipe"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }
 codex/generate-kotlin-multiplatform-tinder-like-app-wto2h7

 codex/generate-kotlin-multiplatform-tinder-like-app-g348hc

    composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
 main
 main
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
}
