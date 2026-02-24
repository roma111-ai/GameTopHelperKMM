plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.gametophelper.old"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gametophelper.old"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}