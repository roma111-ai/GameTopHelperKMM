plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {

//
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
            }
        }
    }
}

android {
    namespace = "com.example.gametophelper.ios"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}