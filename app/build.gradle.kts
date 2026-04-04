plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

fun quote(value: String): String = "\"${value.replace("\"", "\\\"")}\""

val releaseAdMobAppId = providers.gradleProperty("SMART_TOOLKIT_ADMOB_APP_ID")
    .orElse(providers.environmentVariable("SMART_TOOLKIT_ADMOB_APP_ID"))
    .orNull ?: ""
val releaseBannerAdUnitId = providers.gradleProperty("SMART_TOOLKIT_ADMOB_BANNER_ID")
    .orElse(providers.environmentVariable("SMART_TOOLKIT_ADMOB_BANNER_ID"))
    .orNull ?: ""
val releaseRemoveAdsProductId = providers.gradleProperty("SMART_TOOLKIT_REMOVE_ADS_PRODUCT_ID")
    .orElse(providers.environmentVariable("SMART_TOOLKIT_REMOVE_ADS_PRODUCT_ID"))
    .orNull ?: ""
val releaseAdsEnabled = releaseAdMobAppId.isNotBlank() && releaseBannerAdUnitId.isNotBlank()
val releaseBillingEnabled = releaseAdsEnabled && releaseRemoveAdsProductId.isNotBlank()

android {
    namespace = "com.smarttoolkit.app"
    compileSdk = 35

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.smarttoolkit.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField(
                "String",
                "ADMOB_BANNER_AD_UNIT_ID",
                quote("ca-app-pub-3940256099942544/9214589741")
            )
            buildConfigField("boolean", "REMOVE_ADS_PURCHASE_ENABLED", "false")
            buildConfigField("String", "REMOVE_ADS_PRODUCT_ID", quote(""))
        }
        release {
            isMinifyEnabled = true
            manifestPlaceholders["admobAppId"] = releaseAdMobAppId
            buildConfigField("boolean", "ADS_ENABLED", releaseAdsEnabled.toString())
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", quote(releaseBannerAdUnitId))
            buildConfigField(
                "boolean",
                "REMOVE_ADS_PURCHASE_ENABLED",
                releaseBillingEnabled.toString()
            )
            buildConfigField("String", "REMOVE_ADS_PRODUCT_ID", quote(releaseRemoveAdsProductId))
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // CameraX + ML Kit
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.concurrent.futures)
    implementation(libs.concurrent.futures.ktx)
    implementation(libs.guava)
    implementation(libs.zxing.core)

    // Coroutines
    implementation(libs.coroutines.android)

    // Ads
    implementation(libs.play.services.ads)

    // Billing
    implementation(libs.billing)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
}
