plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.crymzee.spenomatic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.crymzee.spenomatic"
        minSdk = 25
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://spenomatic-api.dev.crymzee.com/api/\"")

        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://spenomatic-api.dev.crymzee.com/api/\"")

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
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.viewpager2)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.simplexml)
    implementation(libs.logging.interceptor)

    // Hilt DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // UI Components
    implementation(libs.circleimageview)
    implementation(libs.dotsindicator)
    implementation(libs.circleindicator)
    implementation(libs.lottie)
    implementation(libs.ccp)

    // Utils
    implementation(libs.commons.io)
    implementation(libs.libphonenumber)


    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // sdp/ssp
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation ("np.com.susanthapa:curved_bottom_navigation:0.7.0")
    implementation ("androidx.work:work-runtime-ktx:2.8.1") // use latest stable
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    //maps
    implementation ("com.google.android.gms:play-services-maps:19.2.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")

}


kapt {
    correctErrorTypes = true
}