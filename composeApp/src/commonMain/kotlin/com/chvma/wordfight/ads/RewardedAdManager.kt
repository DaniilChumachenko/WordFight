package com.chvma.wordfight.ads

expect class RewardedAdManager {
    fun loadAd()
    fun showAd(onAdClosed: (rewarded: Boolean) -> Unit)
    fun isAdLoaded(): Boolean
}

expect fun createRewardedAdManager(): RewardedAdManager
