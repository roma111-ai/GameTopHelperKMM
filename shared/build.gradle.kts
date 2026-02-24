plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget()

    // Правильные таргеты для iOS
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("io.ktor:ktor-client-core:2.3.0")
                implementation("io.ktor:ktor-client-json:2.3.0")
                implementation("io.ktor:ktor-client-serialization:2.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
            }
        }

        // ВАЖНО: Имена source sets должны соответствовать таргетам
        val iosX64Main by getting {
            dependencies {
                // зависимости для iOS симулятора (Intel)
            }
        }

        val iosArm64Main by getting {
            dependencies {
                // зависимости для реальных устройств
            }
        }

        val iosSimulatorArm64Main by getting {
            dependencies {
                // зависимости для симулятора (Apple Silicon)
            }
        }

        // Или можно создать общий iosMain, если нужны общие зависимости для всех iOS таргетов
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // общие зависимости для всех iOS таргетов
            }
        }
    }
}

android {
    namespace = "com.example.gametophelper.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}