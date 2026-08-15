plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.calmcontrol"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.calmcontrol"
        // minSdk 26 gives us java.time natively, so no core-library desugaring needed.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    /*
     * Signs release builds with Android's standard debug key so the APK can be sideloaded onto a
     * real device for testing without any setup.
     *
     * This is NOT a distribution key. Its password is public and every Android install shares it,
     * so anyone could sign an update over this app. Before Play Store or any real release,
     * generate a private upload key and replace this block.
     *
     * The keystore is created automatically the first time you build or run anything from
     * Android Studio, but it will not exist on a clean machine or on CI — so the config is only
     * wired up when the file is actually there. Without it, `assembleRelease` still succeeds and
     * produces an unsigned APK rather than failing the build.
     */
    val debugKeystore = File(System.getProperty("user.home"), ".android/debug.keystore")
    if (debugKeystore.exists()) {
        signingConfigs.create("sideload") {
            storeFile = debugKeystore
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("sideload")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            // Compose UI tests run as local JVM tests under Robolectric, which needs the real
            // Android resource/manifest merge, not the default stub.
            isIncludeAndroidResources = true
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.robolectric)
}
