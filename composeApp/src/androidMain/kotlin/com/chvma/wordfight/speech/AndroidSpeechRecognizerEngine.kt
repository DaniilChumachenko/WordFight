package com.chvma.wordfight.speech

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fallback speech engine backed by the platform [SpeechRecognizer].
 *
 * Used on devices where the bundled Moonshine native library crashes
 * (SIGSEGV inside libmoonshine/libonnxruntime — e.g. Galaxy S9 / Android 9).
 * SpeechRecognizer is one-shot, so we restart it on every result/error to
 * provide the same continuous, partial-result behaviour as [AndroidSpeechEngine].
 *
 * NOTE: [SpeechRecognizer] must be created and driven from the main thread.
 */
class AndroidSpeechRecognizerEngine : SpeechEngine {

    private val _partialFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val partialFlow: Flow<String> = _partialFlow.asSharedFlow()

    private val _processingFlow = MutableStateFlow(false)
    override val processingFlow: Flow<Boolean> = _processingFlow.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var isRunning = false
    private var recognizer: SpeechRecognizer? = null
    private var language: String = "en-US"

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() { _processingFlow.value = true }
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() { _processingFlow.value = false }
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onPartialResults(partialResults: Bundle?) {
            emitResults(partialResults)
        }

        override fun onResults(results: Bundle?) {
            emitResults(results)
            _processingFlow.value = false
            restartListening()
        }

        override fun onError(error: Int) {
            // ERROR_NO_MATCH / ERROR_SPEECH_TIMEOUT are normal in continuous mode.
            if (error != SpeechRecognizer.ERROR_NO_MATCH &&
                error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            ) {
                Log.w(TAG, "SpeechRecognizer error: $error")
            }
            _processingFlow.value = false
            restartListening()
        }
    }

    override fun start(language: String) {
        this.language = language
        if (!hasRecordAudioPermission()) {
            Log.w(TAG, "RECORD_AUDIO permission is not granted")
            return
        }
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                Log.e(TAG, "Speech recognition is not available on this device")
                return@post
            }
            isRunning = true
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                    setRecognitionListener(listener)
                }
            }
            startListening()
        }
    }

    override fun stop() {
        isRunning = false
        _processingFlow.value = false
        // Drop any pending restarts before tearing the recognizer down.
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.post {
            runCatching { recognizer?.stopListening() }
            runCatching { recognizer?.cancel() }
            runCatching { recognizer?.destroy() }
            recognizer = null
        }
    }

    private fun startListening() {
        if (!isRunning) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
        }
        runCatching { recognizer?.startListening(intent) }
            .onFailure { Log.e(TAG, "startListening failed", it) }
    }

    /** Re-arm the one-shot recognizer for continuous listening. */
    private fun restartListening() {
        if (!isRunning) return
        mainHandler.postDelayed({
            if (!isRunning) return@postDelayed
            runCatching { recognizer?.cancel() }
            startListening()
        }, RESTART_DELAY_MS)
    }

    private fun emitResults(bundle: Bundle?) {
        val text = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()
            .orEmpty()
        if (text.isBlank()) return
        Log.d(TAG, "Recognized: $text")
        _partialFlow.tryEmit(text)
    }

    private fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private companion object {
        const val TAG = "AndroidSttEngine"
        const val RESTART_DELAY_MS = 300L
    }
}
