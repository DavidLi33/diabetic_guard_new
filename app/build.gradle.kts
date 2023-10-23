plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.diabeticguard"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.diabeticguard"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:18.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-common:+")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.firebase:firebase-auth:19.2.0")
    implementation("com.google.firebase:firebase-database:20.0.3")
    implementation("com.google.firebase:firebase-firestore:24.0.1")
    implementation("com.google.firebase:firebase-bom:32.4.0")
    implementation("com.opencsv:opencsv:5.8")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
