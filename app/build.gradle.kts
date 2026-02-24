plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.secrets.plugin)
    kotlin("kapt")
}

android {
    namespace = "com.example.matheasy"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.matheasy"
        minSdk = 24
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity) // para viewModels()
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.maps)
    implementation(libs.glide)
    implementation(libs.recyclerview)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.swiperefreshlayout)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.viewmodel.ktx) // viewModels()
    implementation(libs.glide.transformations) // solo implement
    implementation(libs.konfetti)
    implementation(libs.rendering)
    implementation(libs.sceneform.ux)
    implementation(libs.assets)
    implementation(libs.arcore)
    implementation(libs.socketio)
    implementation(libs.biometric)
    implementation(libs.security.crypto)
    implementation(libs.androidx.cardview)
    implementation(libs.play.services.maps3d)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}