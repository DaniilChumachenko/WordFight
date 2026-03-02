package com.chvma.wordfight.ads

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined
import platform.Foundation.NSLog
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
object ATTManager {
    fun requestTrackingAuthorization(completion: () -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
            val status = ATTrackingManager.trackingAuthorizationStatus()

            if (status == ATTrackingManagerAuthorizationStatusNotDetermined) {
                ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler { newStatus ->
                    NSLog("ATT status: $newStatus")
                    dispatch_async(dispatch_get_main_queue()) {
                        completion()
                    }
                }
            } else {
                completion()
            }
        }
    }
}
