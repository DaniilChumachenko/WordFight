package com.chvma.wordfight.speech

import android.content.Context

lateinit var appContext: Context

actual fun createSpeechEngine(): SpeechEngine = VoskSpeechEngine()
