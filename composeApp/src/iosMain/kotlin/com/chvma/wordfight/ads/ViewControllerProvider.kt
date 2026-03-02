package com.chvma.wordfight.ads

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@OptIn(ExperimentalForeignApi::class)
object ViewControllerProvider {
    private var viewControllerRef: UIViewController? = null

    fun setViewController(viewController: UIViewController) {
        viewControllerRef = viewController
    }

    fun getViewController(): UIViewController? {
        return viewControllerRef ?: getKeyWindowRootViewController()
    }

    private fun getKeyWindowRootViewController(): UIViewController? {
        val scenes = UIApplication.sharedApplication.connectedScenes
        for (scene in scenes) {
            val windowScene = scene as? UIWindowScene ?: continue
            if (windowScene.activationState == UISceneActivationStateForegroundActive) {
                val windows = windowScene.windows
                for (window in windows) {
                    val uiWindow = window as? UIWindow ?: continue
                    if (uiWindow.isKeyWindow()) {
                        return uiWindow.rootViewController
                    }
                }
            }
        }
        return null
    }
}
