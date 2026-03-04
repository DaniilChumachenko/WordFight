package com.chvma.wordfight.ads

expect class RewardedAdManager(adUnitId: String) {
    fun loadAd()
    fun showAd(onAdClosed: (rewarded: Boolean) -> Unit)
    fun isAdLoaded(): Boolean
}
