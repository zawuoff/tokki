package com.fouwaz.tokki_learn.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Manager class for Text-to-Speech functionality
 * Handles audio playback for language learning
 */
class TTSManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    /**
     * Initialize the TTS engine
     */
    fun initialize(onInitialized: (Boolean) -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                _isInitialized.value = true

                // Configure TTS for language learning (slower speech rate)
                tts?.setSpeechRate(0.7f) // Slower than normal (1.0f is normal)
                tts?.setPitch(1.0f)

                // Set up utterance progress listener
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })

                onInitialized(true)
            } else {
                _isInitialized.value = false
                onInitialized(false)
            }
        }
    }

    /**
     * Speak the given text in the specified language
     * @param text The text to speak
     * @param languageCode Language code (e.g., "es" for Spanish, "en" for English)
     * @param countryCode Optional country code (e.g., "ES" for Spain, "MX" for Mexico)
     */
    fun speak(
        text: String,
        languageCode: String = "es",
        countryCode: String = ""
    ) {
        if (!_isInitialized.value) return

        val locale = if (countryCode.isNotEmpty()) {
            Locale(languageCode, countryCode)
        } else {
            Locale(languageCode)
        }

        val result = tts?.setLanguage(locale)

        when (result) {
            TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                // Language not supported, fallback to default
                tts?.language = Locale.US
            }
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_UTTERANCE_ID")
    }

    /**
     * Stop any ongoing speech
     */
    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    /**
     * Check if a language is available
     * @return true if language is available, false otherwise
     */
    fun isLanguageAvailable(languageCode: String, countryCode: String = ""): Boolean {
        if (!_isInitialized.value) return false

        val locale = if (countryCode.isNotEmpty()) {
            Locale(languageCode, countryCode)
        } else {
            Locale(languageCode)
        }

        val result = tts?.isLanguageAvailable(locale)
        return result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE
    }

    /**
     * Release TTS resources
     * Should be called when the TTS is no longer needed
     */
    fun shutdown() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        tts?.shutdown()
        tts = null
        _isInitialized.value = false
        _isSpeaking.value = false
    }
}

/**
 * Supported languages for learning
 */
enum class LearningLanguage(val code: String, val displayName: String) {
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    CHINESE("zh", "Chinese"),
    ENGLISH("en", "English")
}
