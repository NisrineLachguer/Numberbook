plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.lachguer.numbook"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lachguer.numbook"
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
}

dependencies {

    //implementation(libs.appcompat)
    implementation(libs.material)
    //implementation(libs.activity)
    //implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.mikhaellopez:circularimageview:4.3.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.material)  // Version controlled by libs.versions.toml
    implementation("com.google.android.material:material:1.11.0")
    implementation ("com.google.android.material:material:1.2.1")  // Stable version
}