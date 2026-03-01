package com.chvma.wordfight.speech

import androidx.compose.runtime.Composable
import platform.AVFAudio.AVAudioSession
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus

@Composable
actual fun rememberPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): PermissionRequester {
    return object : PermissionRequester {
        override fun requestPermission(onResult: (Boolean) -> Unit) {
            // Request audio permission
            AVAudioSession.sharedInstance().requestRecordPermission { audioGranted ->
                if (audioGranted) {
                    // Request speech recognition permission
                    SFSpeechRecognizer.requestAuthorization { speechStatus ->
                        val granted = speechStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
                        onResult(granted)
                    }
                } else {
                    onResult(false)
                }
            }
        }
    }
}
