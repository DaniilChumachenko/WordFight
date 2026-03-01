package com.chvma.wordfight.speech

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

class IosPermissionManager : PermissionManager {
    override suspend fun hasPermission(): Boolean {
        val audioStatus = AVAudioSession.sharedInstance().recordPermission()
        val speechStatus = SFSpeechRecognizer.authorizationStatus()
        
        return audioStatus == AVAudioSessionRecordPermissionGranted &&
                speechStatus == SFSpeechRecognizerAuthorizationStatusAuthorized
    }

    override suspend fun requestPermission(): Boolean {
        // Permission request is handled in SpeechEngine.start()
        return hasPermission()
    }
}

actual fun createPermissionManager(): PermissionManager {
    return IosPermissionManager()
}
