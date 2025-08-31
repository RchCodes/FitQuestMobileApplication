plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fitquest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fitquest"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Default UI + AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ✅ CameraX (needed for live camera feed)
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

    // ✅ ML Kit Pose Detection
    implementation("com.google.mlkit:pose-detection:18.0.0-beta4")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
