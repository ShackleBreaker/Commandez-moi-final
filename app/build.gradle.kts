plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.commandez_moi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.commandez_moi"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // JSON Parsing (replaces JSON.stringify/parse)
    implementation("com.google.code.gson:gson:2.10.1")
    // Image Loading (replaces <img src=...>)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // UI Components (Material Design)
    implementation("com.google.android.material:material:1.11.0")
    // GSON : Pour convertir les objets Java en JSON (remplace JSON.stringify)
    implementation("com.google.code.gson:gson:2.10.1")
// GLIDE : Pour afficher les images (Base64 ou URL)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Room Database (SQLite)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.3.0")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")
}