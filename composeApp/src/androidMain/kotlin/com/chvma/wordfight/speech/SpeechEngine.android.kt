package com.chvma.wordfight.speech

import ai.moonshine.voice.JNI
import ai.moonshine.voice.Transcriber
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
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
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

    // Moonshine caches JNI references tied to the thread that created the
    // transcriber. ALL native calls (loadFromMemory / start / addAudio / stop)
    // MUST happen on this single dedicated thread, otherwise the native layer
    // dereferences stale JNI refs (0xebadde09) and crashes with SIGSEGV.
    private val nativeExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "MoonshineNative")
    }
    private val nativeDispatcher = nativeExecutor.asCoroutineDispatcher()
    private val nativeScope = CoroutineScope(SupervisorJob() + nativeDispatcher)

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

    // VAD/utterance segmentation: finalize an utterance after this many
    // consecutive silent reads (~each read is ~100 ms), and never let a single
    // utterance grow past the cap (forces a transcription and bounds memory).
    private val silentReadsToFinalize = 8
    private val maxUtteranceSamples = sampleRate * 15

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

        // Load on the dedicated native thread so the transcriber's JNI refs
        // are bound to the same thread that will later feed it audio.
        nativeScope.launch {
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
            // Non-streaming TINY model: transcribed in one shot per utterance via
            // transcribeWithoutStreaming(), so no listener is needed (it only
            // fires on the streaming path).
            moonshine.loadFromMemory(encoderModel, decoderModel, tokenizer, JNI.MOONSHINE_MODEL_ARCH_TINY)
        }
    }

    private fun startRecordingIfReady() {
        val loadedTranscriber = synchronized(lock) {
            if (!isRunning || recordJob?.isActive == true) return
            transcriber ?: return
        }
        // Native start/addAudio/stop must run on the same thread that loaded
        // the model, so the loop lives on the dedicated native dispatcher.
        recordJob = nativeScope.launch {
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

        // Accumulate audio for the current utterance, then transcribe the whole
        // buffer in one shot once a pause is detected. This uses the non-streaming
        // API (transcribeWithoutStreaming) the TINY model is built for, and the
        // buffer is cleared after every utterance so native memory stays bounded.
        val utterance = ArrayList<FloatArray>()
        var utteranceSamples = 0
        var hasSpeech = false
        var silentReads = 0

        try {
            recorder.startRecording()
            while (currentCoroutineContext().isActive && synchronized(lock) { isRunning }) {
                val readCount = recorder.read(readBuffer, 0, readBuffer.size, AudioRecord.READ_BLOCKING)
                if (readCount <= 0) {
                    if (readCount < 0) Log.w("AndroidSpeechEngine", "AudioRecord read failed: $readCount")
                    continue
                }

                val chunk = FloatArray(readCount)
                var averageAmplitude = 0f
                for (i in 0 until readCount) {
                    val sample = readBuffer[i] / 32768f
                    chunk[i] = sample
                    averageAmplitude += abs(sample)
                }
                averageAmplitude /= readCount
                val isVoiced = averageAmplitude >= voiceActivityThreshold

                if (isVoiced) {
                    markProcessing()
                    hasSpeech = true
                    silentReads = 0
                    utterance.add(chunk)
                    utteranceSamples += readCount
                } else if (hasSpeech) {
                    // Keep a little trailing silence so word endings aren't clipped.
                    utterance.add(chunk)
                    utteranceSamples += readCount
                    silentReads++
                    if (silentReads >= silentReadsToFinalize) {
                        transcribeUtterance(loadedTranscriber, utterance, utteranceSamples)
                        utterance.clear()
                        utteranceSamples = 0
                        hasSpeech = false
                        silentReads = 0
                    }
                }

                if (utteranceSamples >= maxUtteranceSamples) {
                    transcribeUtterance(loadedTranscriber, utterance, utteranceSamples)
                    utterance.clear()
                    utteranceSamples = 0
                    hasSpeech = false
                    silentReads = 0
                }
            }
            // Flush whatever speech remains when recording stops normally.
            if (hasSpeech && utteranceSamples > 0) {
                transcribeUtterance(loadedTranscriber, utterance, utteranceSamples)
            }
        } catch (error: Throwable) {
            if (error !is CancellationException) {
                Log.e("AndroidSpeechEngine", "Moonshine recording loop failed", error)
            }
        }

        runCatching { recorder.stop() }
        recorder.release()
        synchronized(lock) {
            if (audioRecord === recorder) {
                audioRecord = null
            }
        }
        _processingFlow.value = false
    }

    /** Flattens the buffered chunks and transcribes them on the native thread. */
    private fun transcribeUtterance(
        loadedTranscriber: Transcriber,
        chunks: List<FloatArray>,
        totalSamples: Int,
    ) {
        if (totalSamples <= 0) return
        val full = FloatArray(totalSamples)
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(full, offset)
            offset += chunk.size
        }
        runCatching { loadedTranscriber.transcribeWithoutStreaming(full, sampleRate) }
            .onSuccess { transcript -> emitRecognized(transcript?.text()) }
            .onFailure { error -> Log.e("AndroidSpeechEngine", "Transcription failed", error) }
        _processingFlow.value = false
    }

    override fun stop() {
        synchronized(lock) {
            isRunning = false
        }
        processingResetJob?.cancel()
        processingResetJob = null
        _processingFlow.value = false
        // Stop the recorder to unblock recorder.read() so the recording loop
        // exits and frees native resources ON the native thread itself.
        // We must NOT call transcriber.stop() here: this runs on the caller
        // thread, and Moonshine's native layer only tolerates calls from the
        // thread that created the transcriber (0xebadde09 / SIGSEGV otherwise).
        audioRecord?.let { recorder ->
            runCatching { recorder.stop() }
        }
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

actual fun createSpeechEngine(): SpeechEngine =
    // Moonshine's native lib (libmoonshine / libonnxruntime) crashes with SIGSEGV
    // on older devices (e.g. Galaxy S9 / Android 9). Fall back to the platform
    // recognizer there; keep Moonshine's offline recognition on Android 10+.
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
        AndroidSpeechRecognizerEngine()
    } else {
        AndroidSpeechEngine()
    }
