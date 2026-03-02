package com.chvma.wordfight.ads

expect class InterstitialAdManager {
    fun loadAd()
    fun showAd(onAdDismissed: () -> Unit)
    fun isAdLoaded(): Boolean
}

expect fun createInterstitialAdManager(): InterstitialAdManager
