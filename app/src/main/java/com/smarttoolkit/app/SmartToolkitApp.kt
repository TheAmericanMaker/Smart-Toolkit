package com.smarttoolkit.app

import android.app.Application
// import com.google.android.gms.ads.MobileAds // Temporarily disabled for testing
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartToolkitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Temporarily disabled for testing
        // MobileAds.initialize(this)
    }
}
