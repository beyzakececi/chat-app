plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")             // <<< KSP eklentisini burada version belirtmeden sadece “id” olarak ekliyoruz
    id("com.google.gms.google-services")
}

android {
    namespace = "com.beyzakececi.chatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.beyzakececi.chatapp"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        // Derleme hedefi Java 17 kalacak
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ==================================================
    // 1) Firebase (BOM)
    // ==================================================
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-appcheck-debug")

    // ==================================================
    // 2) AndroidX + UI
    // ==================================================
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // ==================================================
    // 3) Room (KSP üzerinden)
    // ==================================================
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")      // <<< “ksp” kullanıyoruz; kapt değil

    // ==================================================
    // 4) Retrofit, OkHttp + JSON
    // ==================================================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")

    // ==================================================
    // 5) Coroutines
    // ==================================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ==================================================
    // 6) Diğer Kütüphaneler (örnek: SDP/SSP, RoundedImageView)
    // ==================================================
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.intuit.ssp:ssp-android:1.0.6")
    implementation("com.makeramen:roundedimageview:2.3.0")

    // ==================================================
    // 7) Test Kütüphaneleri
    // ==================================================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
