package com.chvma.wordfight.haptics

import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy

class IosHapticEngine : HapticEngine {
    override fun perform(type: HapticType) {
        when (type) {
            HapticType.Correct -> {
                val generator = UINotificationFeedbackGenerator()
                generator.prepare()
                generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            }
            HapticType.LifeLost -> {
                val generator = UINotificationFeedbackGenerator()
                generator.prepare()
                generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
            }
            HapticType.GameOver -> {
                val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyleHeavy)
                generator.prepare()
                generator.impactOccurred()
            }
        }
    }
}

actual fun createHapticEngine(): HapticEngine = IosHapticEngine()
