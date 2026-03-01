package com.chvma.wordfight.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val mainHandler = Handler(Looper.getMainLooper())
    private var restartRunnable: Runnable? = null
    private var currentLanguage = "en-US"
    private var isRunning = false
    private var isListening = false

    override fun start(language: String) {
        currentLanguage = language
        isRunning = true
        startListening()
    }

    private fun startListening() {
        if (!isRunning || isListening) return
        ensureRecognizer()
        isListening = true
        recognizer?.startListening(buildIntent())
    }

    private fun ensureRecognizer() {
        if (recognizer != null) return
        recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                    if (!isRunning) return
                    val delay = when (error) {
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                        SpeechRecognizer.ERROR_CLIENT -> 400L
                        else -> 150L
                    }
                    scheduleRestart(delay)
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    // Final results are most accurate — emit all hypotheses
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.forEach { _partialFlow.tryEmit(it) }
                    if (isRunning) scheduleRestart(150L)
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    // Emit all hypotheses, not just the first one
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.forEach { _partialFlow.tryEmit(it) }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        if (!isRunning) return
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        val r = Runnable { startListening() }
        restartRunnable = r
        mainHandler.postDelayed(r, delayMs)
    }

    private fun buildIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // WEB_SEARCH is optimised for short queries/single words
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
            // Respond faster after silence: finalise after 500 ms of silence instead of default ~1.5 s
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 300L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 200L)
        }
    }

    override fun stop() {
        isRunning = false
        isListening = false
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        restartRunnable = null
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }
}

actual fun createSpeechEngine(): SpeechEngine = AndroidSpeechEngine()
