plugins {
    id("com.android.application")
    kotlin("android")
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
        // Java ve Kotlin JVM hedefini uyumlu hâle getiriyoruz
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ==================================================
    // 1) BOM Tanımı: Tüm Firebase kütüphanelerinin uyumlu sürümünü buradan alacağız
    // ==================================================
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))

    // ==================================================
    // 2) Firebase Bağımlılıkları
    // --------------------------------------------------
    // – Burada ayrı ayrı sürüm numarası yazmıyoruz; BOM bunları otomatik belirliyor.
    // ==================================================

    // Firebase Authentication (versiyon BOM’dan geliyor)
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase Firestore (versiyon BOM’dan geliyor)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Firebase App Check Debug Provider
    // (Bu satır aynı zamanda interop paketini de getirir,
    // böylece `InternalAppCheckTokenProvider` sınıfı projenize dahil olur.)
    implementation("com.google.firebase:firebase-appcheck-debug")

    // ==================================================
    // 3) AndroidX ve Diğer Kütüphaneler
    // ==================================================
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // ==================================================
    // 4) Test Bağımlılıkları
    // ==================================================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
