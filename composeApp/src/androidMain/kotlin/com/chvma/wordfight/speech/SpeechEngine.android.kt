package com.chvma.wordfight.speech

import ai.moonshine.voice.JNI
import ai.moonshine.voice.Transcriber
import ai.moonshine.voice.TranscriptEvent
import ai.moonshine.voice.TranscriptEventListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max

lateinit var appContext: Context

class AndroidSpeechEngine : SpeechEngine {
    private val _partialFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val partialFlow: Flow<String> = _partialFlow.asSharedFlow()

    private val _processingFlow = MutableStateFlow(false)
    override val processingFlow: Flow<Boolean> = _processingFlow.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private val isLoading = AtomicBoolean(false)

    private var transcriber: Transcriber? = null
    private var recordJob: Job? = null
    private var processingResetJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var isRunning = false

    private val sampleRate = 16_000
    private val modelAssetPath = "tiny-en"
    private val voiceActivityThreshold = 0.008f

    private val transcriptVisitor = object : TranscriptEventListener() {
        override fun onLineTextChanged(event: TranscriptEvent.LineTextChanged) {
            emitRecognized(event.line.text)
        }

        override fun onLineCompleted(event: TranscriptEvent.LineCompleted) {
            emitRecognized(event.line.text)
            _processingFlow.value = false
        }

        override fun onError(event: TranscriptEvent.Error) {
            Log.e("AndroidSpeechEngine", "Moonshine transcription error", event.cause)
            _processingFlow.value = false
        }
    }

    override fun start(language: String) {
        if (!hasRecordAudioPermission()) {
            Log.w("AndroidSpeechEngine", "RECORD_AUDIO permission is not granted")
            return
        }
        if (!language.startsWith("en", ignoreCase = true)) {
            Log.w("AndroidSpeechEngine", "Moonshine tiny-en model is optimized for English, requested: $language")
        }
        synchronized(lock) {
            isRunning = true
        }
        ensureTranscriberLoaded()
    }

    private fun ensureTranscriberLoaded() {
        val alreadyLoaded = synchronized(lock) { transcriber != null }
        if (alreadyLoaded) {
            startRecordingIfReady()
            return
        }
        if (!isLoading.compareAndSet(false, true)) return

        scope.launch {
            runCatching {
                createMoonshineTranscriber()
            }.onSuccess { loadedTranscriber ->
                synchronized(lock) {
                    transcriber = loadedTranscriber
                }
                isLoading.set(false)
                startRecordingIfReady()
            }.onFailure { error ->
                synchronized(lock) {
                    isRunning = false
                }
                isLoading.set(false)
                _processingFlow.value = false
                Log.e("AndroidSpeechEngine", "Failed to load Moonshine model", error)
            }
        }
    }

    private fun createMoonshineTranscriber(): Transcriber {
        val assets = appContext.assets
        val encoderModel = assets.open("$modelAssetPath/encoder_model.ort").use { it.readBytes() }
        val decoderModel = assets.open("$modelAssetPath/decoder_model_merged.ort").use { it.readBytes() }
        val tokenizer = assets.open("$modelAssetPath/tokenizer.bin").use { it.readBytes() }

        return Transcriber().also { moonshine ->
            moonshine.addListener { event -> event.accept(transcriptVisitor) }
            moonshine.loadFromMemory(encoderModel, decoderModel, tokenizer, JNI.MOONSHINE_MODEL_ARCH_TINY)
        }
    }

    private fun startRecordingIfReady() {
        val loadedTranscriber = synchronized(lock) {
            if (!isRunning || recordJob?.isActive == true) return
            transcriber ?: return
        }
        recordJob = scope.launch {
            recordingLoop(loadedTranscriber)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun recordingLoop(loadedTranscriber: Transcriber) {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBufferSize <= 0) {
            Log.e("AndroidSpeechEngine", "Invalid AudioRecord min buffer size: $minBufferSize")
            synchronized(lock) { isRunning = false }
            return
        }

        val readBuffer = ShortArray(max(sampleRate / 10, minBufferSize / Short.SIZE_BYTES))
        val recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(max(minBufferSize, readBuffer.size * Short.SIZE_BYTES))
            .build()

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AndroidSpeechEngine", "AudioRecord failed to initialize")
            recorder.release()
            synchronized(lock) { isRunning = false }
            _processingFlow.value = false
            return
        }

        synchronized(lock) {
            audioRecord = recorder
        }

        try {
            loadedTranscriber.start()
            recorder.startRecording()
            while (currentCoroutineContext().isActive && synchronized(lock) { isRunning }) {
                val readCount = recorder.read(readBuffer, 0, readBuffer.size, AudioRecord.READ_BLOCKING)
                if (readCount > 0) {
                    val audioData = FloatArray(readCount)
                    var averageAmplitude = 0f
                    for (i in 0 until readCount) {
                        val sample = readBuffer[i] / 32768f
                        audioData[i] = sample
                        averageAmplitude += abs(sample)
                    }
                    averageAmplitude /= readCount
                    if (averageAmplitude >= voiceActivityThreshold) {
                        markProcessing()
                    }
                    loadedTranscriber.addAudio(audioData, sampleRate)
                } else if (readCount < 0) {
                    Log.w("AndroidSpeechEngine", "AudioRecord read failed: $readCount")
                }
            }
        } catch (error: Throwable) {
            if (error !is CancellationException) {
                Log.e("AndroidSpeechEngine", "Moonshine recording loop failed", error)
            }
        }

        runCatching { recorder.stop() }
        runCatching { loadedTranscriber.stop() }
        recorder.release()
        synchronized(lock) {
            if (audioRecord === recorder) {
                audioRecord = null
            }
        }
        _processingFlow.value = false
    }

    override fun stop() {
        synchronized(lock) {
            isRunning = false
        }
        processingResetJob?.cancel()
        processingResetJob = null
        _processingFlow.value = false
        audioRecord?.let { recorder ->
            runCatching { recorder.stop() }
        }
        recordJob?.cancel()
    }

    private fun emitRecognized(text: String?) {
        val normalizedText = text?.trim().orEmpty()
        if (normalizedText.isBlank()) return
        Log.d("AndroidSpeechEngine", "Recognized: $normalizedText")
        _partialFlow.tryEmit(normalizedText)
        markProcessing()
    }

    private fun markProcessing() {
        _processingFlow.value = true
        processingResetJob?.cancel()
        processingResetJob = scope.launch {
            delay(700L)
            _processingFlow.value = false
        }
    }

    private fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
}

actual fun createSpeechEngine(): SpeechEngine = AndroidSpeechEngine()
