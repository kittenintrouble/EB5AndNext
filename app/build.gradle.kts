plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.eb5.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.eb5.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // === BuildConfig fields that the form code uses ===
        buildConfigField("String", "PROJECT_FORM_ENDPOINT", "\"https://news-service.replit.app/api/project-form\"")
        buildConfigField("String", "PROJECT_FORM_API_KEY", "\"\"") // API key not required - public endpoint
        buildConfigField("String", "PROJECTS_BASE_URL", "\"https://news-service.replit.app/\"")
        buildConfigField("String", "NEWS_BASE_URL", "\"https://news-service.replit.app/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // при необходимости можно переопределить эндпоинт для debug:
        // debug {
        //     buildConfigField("String", "PROJECT_FORM_ENDPOINT", "\"https://staging.example.com/api/project-form\"")
        // }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM для синхронизации версий
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Navigation & ViewModel
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    // Material icons used across the app (Business, Quiz, Article, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Kotlinx Serialization (Json)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil for images (AsyncImage, SubcomposeAsyncImage)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Firebase Cloud Messaging
    val firebaseBom = platform("com.google.firebase:firebase-bom:33.3.0")
    implementation(firebaseBom)
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("com.google.android.material:material:1.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // JSON
    implementation("org.json:json:20240303")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Compose UI Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kotlin {
    jvmToolchain(17)
}