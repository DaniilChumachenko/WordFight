package com.chvma.wordfight.speech

import android.speech.tts.TextToSpeech
import java.util.Locale

class AndroidSpeechPlayer : SpeechPlayer, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
    }

    override fun speak(text: String, language: String) {
        val engine = tts ?: return
        if (!ready || text.isBlank()) return
        val locale = Locale.forLanguageTag(language)
        engine.language = locale
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word_speak")
    }

    override fun stop() {
        val engine = tts ?: return
        engine.stop()
        engine.shutdown()
        tts = null
        ready = false
    }
}

actual fun createSpeechPlayer(): SpeechPlayer = AndroidSpeechPlayer()
