package com.chvma.wordfight.speech

interface SpeechPlayer {
    fun speak(text: String, language: String = "en-US")
    fun stop()
}

expect fun createSpeechPlayer(): SpeechPlayer
