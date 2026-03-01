package com.chvma.wordfight.speech

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus

class IosPermissionManager : PermissionManager {
    override suspend fun hasPermission(): Boolean {
        val audioStatus = AVAudioSession.sharedInstance().recordPermission()
        val speechStatus = SFSpeechRecognizer.authorizationStatus()
        
        return audioStatus == AVAudioSessionRecordPermissionGranted &&
                speechStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
    }

    override suspend fun requestPermission(): Boolean {
        // Permission request is handled in SpeechEngine.start()
        return hasPermission()
    }
}

actual fun createPermissionManager(): PermissionManager {
    return IosPermissionManager()
}
