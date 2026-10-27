plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing comes from CI secrets (see .github/workflows/build.yml) or local env vars.
val keystorePath: String? = System.getenv("KP_KEYSTORE_FILE")
val keystorePassword: String? = System.getenv("KP_KEYSTORE_PASSWORD")
val buildNumber: Int = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
val repoSlug: String = System.getenv("GITHUB_REPOSITORY") ?: "ludwize/kravos-tab"

android {
    namespace = "net.kravos.tab"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.kravos.tab"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
        buildConfigField("String", "GITHUB_REPO", "\"$repoSlug\"")
        buildConfigField("int", "BUILD_NUMBER", "$buildNumber")
    }

    signingConfigs {
        create("release") {
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = System.getenv("KP_KEY_ALIAS") ?: "kravos"
                keyPassword = keystorePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (keystorePath != null) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
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
        buildConfig = true
    }
    androidResources {
        noCompress += listOf("sf3", "woff2", "js", "html")
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
