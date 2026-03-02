package com.chvma.wordfight.speech

import androidx.compose.runtime.Composable
import platform.AVFAudio.AVAudioSession
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): PermissionRequester {
    return object : PermissionRequester {
        override fun requestPermission(onResult: (Boolean) -> Unit) {
            dispatch_async(dispatch_get_main_queue()) {
                // Request audio permission
                AVAudioSession.sharedInstance().requestRecordPermission { audioGranted ->
                    if (audioGranted) {
                        // Request speech recognition permission
                        SFSpeechRecognizer.requestAuthorization { speechStatus ->
                            val granted = speechStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
                            dispatch_async(dispatch_get_main_queue()) {
                                onResult(granted)
                            }
                        }
                    } else {
                        dispatch_async(dispatch_get_main_queue()) {
                            onResult(false)
                        }
                    }
                }
            }
        }
    }
}
