package com.fouwaz.tokki_learn.speech

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Singleton manager for app-wide TTS and Speech Recognition
 * This ensures proper resource management and prevents multiple instances
 */
object SpeechManager : DefaultLifecycleObserver {

    private var ttsManager: TTSManager? = null
    private var speechRecognitionManager: SpeechRecognitionManager? = null

    /**
     * Initialize TTS for the application
     */
    fun initializeTTS(context: Context, onInitialized: (Boolean) -> Unit = {}) {
        if (ttsManager == null) {
            ttsManager = TTSManager(context.applicationContext)
        }
        ttsManager?.initialize(onInitialized)
    }

    /**
     * Initialize Speech Recognition for the application
     */
    fun initializeSpeechRecognition(context: Context) {
        if (speechRecognitionManager == null) {
            speechRecognitionManager = SpeechRecognitionManager(context.applicationContext)
        }
        speechRecognitionManager?.initialize()
    }

    /**
     * Get the TTS manager instance
     */
    fun getTTSManager(context: Context): TTSManager {
        if (ttsManager == null) {
            ttsManager = TTSManager(context.applicationContext)
        }
        return ttsManager!!
    }

    /**
     * Get the Speech Recognition manager instance
     */
    fun getSpeechRecognitionManager(context: Context): SpeechRecognitionManager {
        if (speechRecognitionManager == null) {
            speechRecognitionManager = SpeechRecognitionManager(context.applicationContext)
        }
        return speechRecognitionManager!!
    }

    /**
     * Cleanup when app is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        ttsManager?.shutdown()
        speechRecognitionManager?.destroy()
        ttsManager = null
        speechRecognitionManager = null
    }
}
