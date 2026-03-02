package com.chvma.wordfight.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import cocoapods.Google_Mobile_Ads_SDK.GADAdSizeBanner
import cocoapods.Google_Mobile_Ads_SDK.GADBannerView
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSLog
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BannerAdView(modifier: Modifier) {
    UIKitView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = {
            val bannerView = GADBannerView(CGRectZero.readValue())
            bannerView.adUnitID = "ca-app-pub-3940256099942544/2934735716"

            val viewController = ViewControllerProvider.getViewController()
            if (viewController != null) {
                bannerView.rootViewController = viewController
                bannerView.setAdSize(GADAdSizeBanner.readValue())
                dispatch_async(dispatch_get_main_queue()) {
                    bannerView.loadRequest(GADRequest())
                }
            } else {
                NSLog("BannerAdView: rootViewController not available yet")
            }

            bannerView
        },
    )
}
