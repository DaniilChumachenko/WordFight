package com.chvma.wordfight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import cocoapods.Google_Mobile_Ads_SDK.GADMobileAds
import com.chvma.wordfight.ads.ATTManager
import com.chvma.wordfight.ads.ViewControllerProvider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private var isSdkReadyState by mutableStateOf(false)
private var isSdkInitStarted = false

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    if (!isSdkInitStarted) {
        isSdkInitStarted = true
        ATTManager.requestTrackingAuthorization {
            dispatch_async(dispatch_get_main_queue()) {
                GADMobileAds.sharedInstance().startWithCompletionHandler {
                    NSLog("Google Mobile Ads SDK initialized (iOS)")
                    dispatch_async(dispatch_get_main_queue()) {
                        isSdkReadyState = true
                    }
                }
            }
        }
    }

    val viewController = ComposeUIViewController { App(isSdkReady = isSdkReadyState) }
    ViewControllerProvider.setViewController(viewController)
    return viewController
}
