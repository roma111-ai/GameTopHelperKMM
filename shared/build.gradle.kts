plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.9.0"
}

kotlin {
    androidTarget()

//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("io.ktor:ktor-client-core:2.3.0")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
                implementation("io.ktor:ktor-client-android:2.3.0")
            }
        }

//        val iosX64Main by getting {
//            dependencies {
//                implementation("io.ktor:ktor-client-darwin:2.3.0")
//            }
//        }
//
//        val iosArm64Main by getting {
//            dependencies {
//                implementation("io.ktor:ktor-client-darwin:2.3.0")
//            }
//        }
//
//        val iosSimulatorArm64Main by getting {
//            dependencies {
//                implementation("io.ktor:ktor-client-darwin:2.3.0")
//            }
//        }

    }

}

android {
    namespace = "com.example.gametophelper.shared"
    compileSdk = 34

    buildFeatures {
        prefab = true
    }

    defaultConfig {
        minSdk = 24
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
        freeCompilerArgs += "-Xopt-in=kotlinx.serialization.InternalSerializationApi"
    }
}