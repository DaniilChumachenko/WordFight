package com.chvma.wordfight.ads

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

actual class RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    private val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    actual fun loadAd() {
        if (isLoading || rewardedAd != null) return

        val activity = ActivityProvider.getActivity() ?: run {
            Log.e("RewardedAdManager", "Activity is null, cannot load ad")
            return
        }

        isLoading = true
        RewardedAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e("RewardedAdManager", "Ad failed to load: ${error.message}")
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                }
            },
        )
    }

    actual fun showAd(onAdClosed: (rewarded: Boolean) -> Unit) {
        val activity = ActivityProvider.getActivity()
        val ad = rewardedAd

        if (activity == null || ad == null) {
            loadAd()
            onAdClosed(false)
            return
        }

        var rewardEarned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()
                onAdClosed(rewardEarned)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadAd()
                onAdClosed(false)
            }
        }

        ad.show(activity) {
            rewardEarned = true
        }
    }

    actual fun isAdLoaded(): Boolean = rewardedAd != null
}

actual fun createRewardedAdManager(): RewardedAdManager = RewardedAdManager()
