package com.chvma.wordfight.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

lateinit var appContext: Context

class AndroidSpeechEngine : SpeechEngine {
    private val _partialFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val partialFlow: Flow<String> = _partialFlow.asSharedFlow()

    private var recognizer: SpeechRecognizer? = null
    private var currentLanguage = "en-US"
    private var isRunning = false

    override fun start(language: String) {
        currentLanguage = language
        isRunning = true
        startListening()
    }

    private fun startListening() {
        if (!isRunning) return
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    if (isRunning) startListening()
                }
                override fun onResults(results: Bundle?) {
                    // Final results are most accurate — emit all hypotheses
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.forEach { _partialFlow.tryEmit(it) }
                    if (isRunning) startListening()
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    // Emit all hypotheses, not just the first one
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.forEach { _partialFlow.tryEmit(it) }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // WEB_SEARCH is optimised for short queries/single words
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            // Respond faster after silence: finalise after 500 ms of silence instead of default ~1.5 s
            putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH", 500L)
            putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH", 300L)
            putExtra("android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH", 200L)
        }
        recognizer?.startListening(intent)
    }

    override fun stop() {
        isRunning = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }
}

actual fun createSpeechEngine(): SpeechEngine = AndroidSpeechEngine()
