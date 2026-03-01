package com.chvma.wordfight.speech

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream

lateinit var appContext: Context

class AndroidSpeechEngine : SpeechEngine, RecognitionListener {
    private val _partialFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val partialFlow: Flow<String> = _partialFlow.asSharedFlow()
    private val _processingFlow = MutableStateFlow(false)
    override val processingFlow: Flow<Boolean> = _processingFlow.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var silenceRunnable: Runnable? = null

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null
    private var isRunning = false
    private var isLoading = false

    private val modelAssetPath = "vosk-model"
    private val modelDirName = "vosk-model"
    private val sampleRate = 16000f

    override fun start(language: String) {
        isRunning = true
        ensureModelLoaded()
    }

    private fun ensureModelLoaded() {
        if (model != null || isLoading) {
            startListeningIfReady()
            return
        }
        isLoading = true
        scope.launch {
            runCatching {
                val modelDir = prepareModelDir() ?: return@runCatching
                val m = Model(modelDir.absolutePath)
                val r = Recognizer(m, sampleRate)
                withContext(Dispatchers.Main) {
                    model = m
                    recognizer = r
                    isLoading = false
                    startListeningIfReady()
                }
            }.onFailure { e ->
                Log.e("AndroidSpeechEngine", "Failed to load Vosk model", e)
                isLoading = false
            }
        }
    }

    private fun startListeningIfReady() {
        if (!isRunning) return
        val r = recognizer ?: return
        if (speechService != null) return
        speechService = SpeechService(r, sampleRate).apply {
            startListening(this@AndroidSpeechEngine)
        }
    }

    private fun prepareModelDir(): File? {
        val targetDir = File(appContext.filesDir, modelDirName)
        if (targetDir.exists() && targetDir.list()?.isNotEmpty() == true) {
            return targetDir
        }
        val assets = appContext.assets
        val nestedList = assets.list(modelAssetPath)
        if (nestedList != null && nestedList.isNotEmpty()) {
            copyAssetDir(modelAssetPath, targetDir)
            return targetDir
        }

        val rootList = assets.list("") ?: emptyArray()
        val hasModelAtRoot = rootList.contains("am") && rootList.contains("conf") && rootList.contains("graph")
        if (!hasModelAtRoot) {
            Log.e("AndroidSpeechEngine", "Missing model assets at assets/$modelAssetPath or root")
            return null
        }
        if (!targetDir.exists()) targetDir.mkdirs()
        rootList.forEach { child ->
            copyAssetDir(child, File(targetDir, child))
        }
        return targetDir
    }

    private fun copyAssetDir(assetPath: String, target: File) {
        val assets = appContext.assets
        val children = assets.list(assetPath) ?: return
        if (children.isEmpty()) {
            target.parentFile?.mkdirs()
            assets.open(assetPath).use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            if (!target.exists()) target.mkdirs()
            children.forEach { child ->
                val childPath = if (assetPath.isEmpty()) child else "$assetPath/$child"
                copyAssetDir(childPath, File(target, child))
            }
        }
    }

    override fun stop() {
        isRunning = false
        _processingFlow.value = false
        silenceRunnable?.let { mainHandler.removeCallbacks(it) }
        silenceRunnable = null
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
    }

    override fun onPartialResult(hypothesis: String?) {
        val text = extractPartial(hypothesis)
        if (text.isNotBlank()) {
            _partialFlow.tryEmit(text)
            markProcessing()
        }
    }

    override fun onResult(hypothesis: String?) {
        val text = extractFinal(hypothesis)
        if (text.isNotBlank()) _partialFlow.tryEmit(text)
        _processingFlow.value = false
    }

    override fun onFinalResult(hypothesis: String?) {
        val text = extractFinal(hypothesis)
        if (text.isNotBlank()) _partialFlow.tryEmit(text)
        _processingFlow.value = false
    }

    override fun onError(e: Exception?) {
        _processingFlow.value = false
    }

    override fun onTimeout() {
        _processingFlow.value = false
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        startListeningIfReady()
    }

    private fun extractPartial(json: String?): String {
        if (json.isNullOrBlank()) return ""
        return runCatching {
            JSONObject(json).optString("partial")
        }.getOrDefault("")
    }

    private fun extractFinal(json: String?): String {
        if (json.isNullOrBlank()) return ""
        return runCatching {
            JSONObject(json).optString("text")
        }.getOrDefault("")
    }

    private fun markProcessing() {
        _processingFlow.value = true
        silenceRunnable?.let { mainHandler.removeCallbacks(it) }
        val r = Runnable { _processingFlow.value = false }
        silenceRunnable = r
        mainHandler.postDelayed(r, 700L)
    }
}

actual fun createSpeechEngine(): SpeechEngine = AndroidSpeechEngine()
