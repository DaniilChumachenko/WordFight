package com.chvma.wordfight.speech

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class IosPermissionManager : PermissionManager {
    override suspend fun hasPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                val audioStatus = AVAudioSession.sharedInstance().recordPermission()
                val speechStatus = SFSpeechRecognizer.authorizationStatus()
                val granted = audioStatus == AVAudioSessionRecordPermissionGranted &&
                    speechStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
                continuation.resume(granted)
            }
        }
    }

    override suspend fun requestPermission(): Boolean {
        // Permission request is handled in SpeechEngine.start()
        return hasPermission()
    }
}

actual fun createPermissionManager(): PermissionManager {
    return IosPermissionManager()
}
