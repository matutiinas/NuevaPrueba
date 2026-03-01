plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("app.cash.sqldelight:runtime:2.0.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.0.2")
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android {
    namespace = "com.sportswipe.shared"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
}

sqldelight {
    databases {
        create("SportSwipeDatabase") {
            packageName.set("com.sportswipe.shared.db")
        }
    }
}
