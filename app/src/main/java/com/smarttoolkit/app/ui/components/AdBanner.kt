package com.smarttoolkit.app.ui.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Test banner ad unit ID — replace with your real ad unit ID before publishing
private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context: Context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
