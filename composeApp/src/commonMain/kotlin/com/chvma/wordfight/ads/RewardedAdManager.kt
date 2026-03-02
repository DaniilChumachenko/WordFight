package com.chvma.wordfight.ads

expect class RewardedAdManager {
    fun loadAd()
    fun showAd(onAdDismissed: () -> Unit)
    fun isAdLoaded(): Boolean
}

expect fun createRewardedAdManager(): RewardedAdManager
