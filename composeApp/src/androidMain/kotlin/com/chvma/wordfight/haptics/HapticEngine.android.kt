package com.chvma.wordfight.haptics

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.chvma.wordfight.speech.appContext

class AndroidHapticEngine : HapticEngine {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun perform(type: HapticType) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        when (type) {
            // Crisp light double-tick — a "reward" feel, distinct from damage.
            HapticType.Correct -> vibratePattern(longArrayOf(0, 20, 45, 20), intArrayOf(0, 160, 0, 220))
            HapticType.LifeLost -> vibrate(90)
            HapticType.GameOver -> vibrate(250)
        }
    }

    private fun vibrate(durationMs: Long) {
        val v = vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(durationMs, 255))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (_: SecurityException) {
            // No permission or haptics disabled at system level
        }
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        } catch (_: SecurityException) {
            // No permission or haptics disabled at system level
        }
    }
}

actual fun createHapticEngine(): HapticEngine = AndroidHapticEngine()
