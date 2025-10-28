package com.fouwaz.tokki_learn.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager class for Speech Recognition functionality
 * Handles voice input and pronunciation checking
 */
class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognitionResult = MutableStateFlow<RecognitionResult?>(null)
    val recognitionResult: StateFlow<RecognitionResult?> = _recognitionResult.asStateFlow()

    /**
     * Initialize the speech recognizer
     */
    fun initialize() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    /**
     * Start listening for speech input
     * @param languageCode Language to recognize (e.g., "es-ES" for Spanish)
     */
    fun startListening(languageCode: String = "es-ES") {
        if (speechRecognizer == null) {
            initialize()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {
                // User started speaking
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed (can be used for visual feedback)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Buffer received
            }

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Unknown error"
                }
                _recognitionResult.value = RecognitionResult.Error(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    _recognitionResult.value = RecognitionResult.Success(matches)
                } else {
                    _recognitionResult.value = RecognitionResult.Error("No speech detected")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Partial results available
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Reserved for future events
            }
        })

        speechRecognizer?.startListening(intent)
    }

    /**
     * Stop listening for speech input
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    /**
     * Clear the last recognition result
     */
    fun clearResult() {
        _recognitionResult.value = null
    }

    /**
     * Release speech recognizer resources
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
        _recognitionResult.value = null
    }

    /**
     * Check if speech recognition is available
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
}

/**
 * Sealed class representing speech recognition results
 */
sealed class RecognitionResult {
    data class Success(val matches: List<String>) : RecognitionResult()
    data class Error(val message: String) : RecognitionResult()
}

/**
 * Language codes for speech recognition
 */
object RecognitionLanguages {
    const val SPANISH_SPAIN = "es-ES"
    const val SPANISH_MEXICO = "es-MX"
    const val FRENCH = "fr-FR"
    const val GERMAN = "de-DE"
    const val ITALIAN = "it-IT"
    const val PORTUGUESE = "pt-PT"
    const val JAPANESE = "ja-JP"
    const val KOREAN = "ko-KR"
    const val CHINESE = "zh-CN"
    const val ENGLISH_US = "en-US"
}
