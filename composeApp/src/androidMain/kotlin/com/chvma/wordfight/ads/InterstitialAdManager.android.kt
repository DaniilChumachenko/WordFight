package com.chvma.wordfight.ads

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

actual class InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    private val adUnitId = "ca-app-pub-6335318016225303/7850161006"

    actual fun loadAd() {
        if (isLoading || interstitialAd != null) return

        val activity = ActivityProvider.getActivity() ?: run {
            Log.e("InterstitialAdManager", "Activity is null, cannot load ad")
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("InterstitialAdManager", "Ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }
            },
        )
    }

    actual fun showAd(onAdDismissed: () -> Unit) {
        val activity = ActivityProvider.getActivity()
        val ad = interstitialAd

        if (activity == null || ad == null) {
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadAd()
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadAd()
                onAdDismissed()
            }
        }

        ad.show(activity)
    }

    actual fun isAdLoaded(): Boolean = interstitialAd != null
}

actual fun createInterstitialAdManager(): InterstitialAdManager = InterstitialAdManager()
