package com.chvma.wordfight.speech

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.chvma.wordfight.content.WordRepository
import com.chvma.wordfight.engine.logRecognition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService

/**
 * Continuous offline recognizer constrained to the game's known vocabulary.
 *
 * Unlike Android SpeechRecognizer, Vosk owns one AudioRecord session for the
 * whole game, so it does not repeatedly toggle the system microphone service.
 */
class VoskSpeechEngine : SpeechEngine, RecognitionListener {

    private val _partialFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val partialFlow: Flow<String> = _partialFlow.asSharedFlow()

    private val _processingFlow = MutableStateFlow(false)
    override val processingFlow: Flow<Boolean> = _processingFlow.asStateFlow()

    @Volatile
    private var isRunning = false
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var lastPartialText = ""

    override fun start(language: String) {
        if (!hasRecordAudioPermission()) {
            logRecognition("Vosk cannot start because RECORD_AUDIO permission is missing")
            return
        }
        if (isRunning) return

        isRunning = true
        val loadedModel = model
        if (loadedModel != null) {
            startListening(loadedModel)
            return
        }

        logRecognition("Vosk unpacking offline model")
        StorageService.unpack(
            appContext,
            MODEL_ASSET_PATH,
            MODEL_STORAGE_PATH,
            { unpackedModel ->
                model = unpackedModel
                logRecognition("Vosk offline model ready")
                if (isRunning) startListening(unpackedModel)
            },
            { error ->
                isRunning = false
                _processingFlow.value = false
                logRecognition("Vosk model loading failed: ${error.message}")
            },
        )
    }

    override fun stop() {
        isRunning = false
        lastPartialText = ""
        _processingFlow.value = false
        speechService?.let { service ->
            service.stop()
            service.shutdown()
        }
        speechService = null
        logRecognition("Vosk continuous listening stopped")
    }

    override fun onPartialResult(hypothesis: String?) {
        val text = hypothesis.parseVoskText("partial")
        if (text.isBlank() || text == lastPartialText) return
        lastPartialText = text
        _processingFlow.value = true
        emit(text, source = "partial")
    }

    override fun onResult(hypothesis: String?) {
        val text = hypothesis.parseVoskText("text")
        lastPartialText = ""
        _processingFlow.value = false
        emit(text, source = "result")
    }

    override fun onFinalResult(hypothesis: String?) {
        val text = hypothesis.parseVoskText("text")
        lastPartialText = ""
        _processingFlow.value = false
        emit(text, source = "final")
    }

    override fun onError(error: Exception?) {
        _processingFlow.value = false
        logRecognition("Vosk recognition error: ${error?.message}")
    }

    override fun onTimeout() {
        _processingFlow.value = false
        logRecognition("Vosk recognition timeout")
    }

    private fun startListening(loadedModel: Model) {
        if (!isRunning || speechService != null) return

        runCatching {
            val recognizer = Recognizer(loadedModel, SAMPLE_RATE, vocabularyJson)
            SpeechService(recognizer, SAMPLE_RATE).also { service ->
                speechService = service
                service.startListening(this)
            }
        }.onSuccess {
            logRecognition("Vosk continuous listening started vocabulary=${WordRepository.words.size}")
        }.onFailure { error ->
            isRunning = false
            _processingFlow.value = false
            logRecognition("Vosk failed to start: ${error.message}")
        }
    }

    private fun emit(text: String, source: String) {
        if (text.isBlank()) return
        logRecognition("Vosk emitted $source transcript=\"$text\"")
        _partialFlow.tryEmit(text)
    }

    private fun String?.parseVoskText(key: String): String {
        if (this.isNullOrBlank()) return ""
        return runCatching { JSONObject(this).optString(key).trim() }
            .onFailure { error ->
                logRecognition("Vosk result parsing failed: ${error.message}; raw=$this")
            }
            .getOrDefault("")
    }

    private fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private companion object {
        const val MODEL_ASSET_PATH = "model-en-us"
        const val MODEL_STORAGE_PATH = "model-en-us"
        const val SAMPLE_RATE = 16_000f

        val vocabularyJson: String by lazy {
            JSONArray(
                WordRepository.words
                    .map { word -> word.word.lowercase().toVoskVocabularyEntry() }
                    .distinct()
                    .plus("[unk]"),
            ).toString()
        }

        // These compound words are absent from the mobile model's lexicon,
        // while their separate parts are supported. WordMatcher compacts them.
        fun String.toVoskVocabularyEntry(): String = when (this) {
            "trolleybus" -> "trolley bus"
            "lifebuoy" -> "life buoy"
            else -> this
        }
    }
}
