package com.smarttoolkit.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class SmartToolkitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.ADS_ENABLED) {
            CoroutineScope(Dispatchers.IO).launch {
                MobileAds.initialize(this@SmartToolkitApp) {}
            }
        }
    }
}
