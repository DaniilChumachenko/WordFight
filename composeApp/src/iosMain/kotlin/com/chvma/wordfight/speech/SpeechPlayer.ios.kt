package com.chvma.wordfight.speech

import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

class IosSpeechPlayer : SpeechPlayer {
    private val synthesizer = AVSpeechSynthesizer()

    override fun speak(text: String, language: String) {
        if (text.isBlank()) return
        val utterance = AVSpeechUtterance(string = text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(language)
        utterance.rate = 0.5f
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        if (synthesizer.isSpeaking()) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
    }
}

actual fun createSpeechPlayer(): SpeechPlayer = IosSpeechPlayer()
