package com.chvma.wordfight.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.Foundation.NSLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatusAuthorized

class IosSpeechEngine : SpeechEngine {
    private val _partialFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val partialFlow: Flow<String> = _partialFlow.asSharedFlow()

    private var speechRecognizer: SFSpeechRecognizer? = null
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var recognitionTask: platform.Speech.SFSpeechRecognitionTask? = null
    private val audioEngine = AVAudioEngine()
    private var currentLanguage = "en-US"
    private var isRunning = false

    override fun start(language: String) {
        currentLanguage = language
        isRunning = true
        SFSpeechRecognizer.requestAuthorization { status ->
            if (status == SFSpeechRecognizerAuthorizationStatusAuthorized) {
                startRecognition()
            }
        }
    }

    private fun startRecognition() {
        if (!isRunning) return
        try {
            recognitionTask?.cancel()
            recognitionTask = null

            val locale = NSLocale(localeIdentifier = currentLanguage)
            speechRecognizer = SFSpeechRecognizer(locale = locale)

            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryRecord, error = null)
            audioSession.setMode(AVAudioSessionModeMeasurement, error = null)
            audioSession.setActive(
                true,
                withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation,
                error = null
            )

            val request = SFSpeechAudioBufferRecognitionRequest()
            recognitionRequest = request
            request.shouldReportPartialResults = true

            val inputNode = audioEngine.inputNode
            recognitionTask = speechRecognizer?.recognitionTaskWithRequest(request) { result, error ->
                result?.let {
                    _partialFlow.tryEmit(it.bestTranscription.formattedString)
                }
                if (error != null || result?.isFinal == true) {
                    audioEngine.stop()
                    inputNode.removeTapOnBus(0u)
                    recognitionRequest = null
                    recognitionTask = null
                    if (isRunning) startRecognition()
                }
            }

            val format = inputNode.outputFormatForBus(0u)
            inputNode.installTapOnBus(0u, bufferSize = 1024u, format = format) { buffer, _ ->
                buffer?.let { request.appendAudioPCMBuffer(it) }
            }

            audioEngine.prepare()
            audioEngine.startAndReturnError(null)
        } catch (_: Exception) {}
    }

    override fun stop() {
        isRunning = false
        audioEngine.stop()
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()
        recognitionRequest = null
        recognitionTask = null
    }
}

actual fun createSpeechEngine(): SpeechEngine = IosSpeechEngine()
