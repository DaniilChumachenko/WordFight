package com.chvma.wordfight.speech

import androidx.compose.runtime.Composable
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermission
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.Speech.SFSpeechRecognizerAuthorizationStatusAuthorized
import platform.Speech.SFSpeechRecognizerAuthorizationStatusDenied
import platform.Speech.SFSpeechRecognizerAuthorizationStatusNotDetermined
import platform.Speech.SFSpeechRecognizerAuthorizationStatusRestricted

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
                        val granted = speechStatus == SFSpeechRecognizerAuthorizationStatusAuthorized
                        onResult(granted)
                    }
                } else {
                    onResult(false)
                }
            }
        }
    }
}
