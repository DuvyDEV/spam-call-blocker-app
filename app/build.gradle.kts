plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.addev.listaspam"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.addev.listaspam"
        minSdk = 29
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 23
        versionName = "2.5.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = (project.findProperty("RELEASE_STORE_FILE") as? String)
                ?: System.getenv("ANDROID_KEYSTORE_FILE")

            if (!keystorePath.isNullOrBlank()) {
                val storeFileCandidate = rootProject.file(keystorePath)
                if (storeFileCandidate.exists()) {
                    storeFile = storeFileCandidate
                    storePassword = (project.findProperty("RELEASE_STORE_PASSWORD") as? String)
                        ?: System.getenv("ANDROID_KEYSTORE_PASSWORD")
                    keyAlias = (project.findProperty("RELEASE_KEY_ALIAS") as? String)
                        ?: System.getenv("ANDROID_KEY_ALIAS")
                    keyPassword = (project.findProperty("RELEASE_KEY_PASSWORD") as? String)
                        ?: System.getenv("ANDROID_KEY_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        release {
            signingConfig = if (signingConfigs.getByName("release").storeFile != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.jsoup)
    implementation(libs.libphonenumber)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
