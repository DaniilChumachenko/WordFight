package com.chvma.wordfight.speech

import kotlinx.coroutines.flow.Flow

interface SpeechEngine {
    fun start(language: String = "en-US")
    fun stop()
    val partialFlow: Flow<String>
    val processingFlow: Flow<Boolean>
}

expect fun createSpeechEngine(): SpeechEngine
