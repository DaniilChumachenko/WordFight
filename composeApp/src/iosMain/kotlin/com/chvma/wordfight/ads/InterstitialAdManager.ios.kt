package com.chvma.wordfight.ads

import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenContentDelegateProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenPresentingAdProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADInterstitialAd
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual class InterstitialAdManager {
    private var interstitialAd: GADInterstitialAd? = null
    private var isLoading = false
    private var currentDelegate: GADFullScreenContentDelegateProtocol? = null

    private val adUnitId = "ca-app-pub-3940256099942544/4411468910"

    actual fun loadAd() {
        dispatch_async(dispatch_get_main_queue()) {
            if (isLoading || interstitialAd != null) return@dispatch_async

            isLoading = true
            GADInterstitialAd.loadWithAdUnitID(
                adUnitID = adUnitId,
                request = GADRequest(),
                completionHandler = { ad, error ->
                    isLoading = false
                    if (error != null) {
                        interstitialAd = null
                        NSLog("Interstitial failed to load: ${error.localizedDescription}")
                    } else if (ad != null) {
                        interstitialAd = ad
                    }
                },
            )
        }
    }

    actual fun showAd(onAdDismissed: () -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
            val viewController = ViewControllerProvider.getViewController()
            val ad = interstitialAd

            if (viewController == null || ad == null) {
                onAdDismissed()
                return@dispatch_async
            }

            currentDelegate = object : NSObject(), GADFullScreenContentDelegateProtocol {
                override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
                    interstitialAd = null
                    currentDelegate = null
                    loadAd()
                    onAdDismissed()
                }

                override fun ad(
                    ad: GADFullScreenPresentingAdProtocol,
                    didFailToPresentFullScreenContentWithError: NSError,
                ) {
                    interstitialAd = null
                    currentDelegate = null
                    loadAd()
                    onAdDismissed()
                }

                override fun adWillPresentFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {}
                override fun adDidRecordImpression(ad: GADFullScreenPresentingAdProtocol) {}
                override fun adDidRecordClick(ad: GADFullScreenPresentingAdProtocol) {}
            }

            ad.fullScreenContentDelegate = currentDelegate
            ad.presentFromRootViewController(viewController)
        }
    }

    actual fun isAdLoaded(): Boolean = interstitialAd != null
}

actual fun createInterstitialAdManager(): InterstitialAdManager = InterstitialAdManager()
