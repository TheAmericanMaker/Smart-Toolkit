package com.smarttoolkit.app.ui.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.smarttoolkit.app.BuildConfig

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (!BuildConfig.ADS_ENABLED || BuildConfig.ADMOB_BANNER_AD_UNIT_ID.isBlank()) {
        return
    }

    val configuration = LocalConfiguration.current

    key(configuration.screenWidthDp) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context: Context ->
                AdView(context).apply {
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            context,
                            configuration.screenWidthDp
                        )
                    )
                    adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
