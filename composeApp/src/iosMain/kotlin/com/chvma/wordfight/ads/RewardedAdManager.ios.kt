package com.chvma.wordfight.ads

import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenContentDelegateProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenPresentingAdProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import cocoapods.Google_Mobile_Ads_SDK.GADRewardedAd
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual class RewardedAdManager {
    private var rewardedAd: GADRewardedAd? = null
    private var isLoading = false
    private var currentDelegate: GADFullScreenContentDelegateProtocol? = null

    private val adUnitId = "ca-app-pub-3940256099942544/1712485313"

    actual fun loadAd() {
        dispatch_async(dispatch_get_main_queue()) {
            if (isLoading || rewardedAd != null) return@dispatch_async

            isLoading = true
            GADRewardedAd.loadWithAdUnitID(
                adUnitID = adUnitId,
                request = GADRequest(),
                completionHandler = { ad, error ->
                    isLoading = false
                    if (error != null) {
                        rewardedAd = null
                        NSLog("Rewarded failed to load: ${error.localizedDescription}")
                    } else if (ad != null) {
                        rewardedAd = ad
                    }
                },
            )
        }
    }

    actual fun showAd(onAdClosed: (rewarded: Boolean) -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
            val viewController = ViewControllerProvider.getViewController()
            val ad = rewardedAd

            if (viewController == null || ad == null) {
                loadAd()
                onAdClosed(false)
                return@dispatch_async
            }

            var rewardEarned = false
            currentDelegate = object : NSObject(), GADFullScreenContentDelegateProtocol {
                override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
                    rewardedAd = null
                    currentDelegate = null
                    loadAd()
                    onAdClosed(rewardEarned)
                }

                override fun ad(
                    ad: GADFullScreenPresentingAdProtocol,
                    didFailToPresentFullScreenContentWithError: NSError,
                ) {
                    rewardedAd = null
                    currentDelegate = null
                    loadAd()
                    onAdClosed(false)
                }

                override fun adWillPresentFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {}
                override fun adDidRecordImpression(ad: GADFullScreenPresentingAdProtocol) {}
                override fun adDidRecordClick(ad: GADFullScreenPresentingAdProtocol) {}
            }

            ad.fullScreenContentDelegate = currentDelegate
            ad.presentFromRootViewController(viewController) {
                rewardEarned = true
            }
        }
    }

    actual fun isAdLoaded(): Boolean = rewardedAd != null
}

actual fun createRewardedAdManager(): RewardedAdManager = RewardedAdManager()
