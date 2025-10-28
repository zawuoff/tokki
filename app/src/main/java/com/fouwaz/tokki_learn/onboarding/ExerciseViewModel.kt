package com.fouwaz.tokki_learn.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fouwaz.tokki_learn.speech.PronunciationScore
import com.fouwaz.tokki_learn.speech.PronunciationScorer
import com.fouwaz.tokki_learn.speech.RecognitionResult
import com.fouwaz.tokki_learn.speech.SpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tutorial steps for the exercise screen
 */
enum class TutorialStep {
    LISTEN,          // User must click sound button and listen
    PRONOUNCE,       // User must click mic button and speak correctly
    FAVORITE,        // User must click like button
    COMPLETED        // All steps completed
}

/**
 * ViewModel for managing exercise screen state and speech functionality
 */
class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsManager = SpeechManager.getTTSManager(application)
    private val speechRecognitionManager = SpeechManager.getSpeechRecognitionManager(application)

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    init {
        // Initialize TTS
        ttsManager.initialize { success ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isTtsReady = true
                )
            }
        }

        // Initialize Speech Recognition
        speechRecognitionManager.initialize()

        // Observe speech recognition results
        viewModelScope.launch {
            speechRecognitionManager.recognitionResult.collect { result ->
                when (result) {
                    is RecognitionResult.Success -> {
                        handleRecognitionSuccess(result.matches)
                    }
                    is RecognitionResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            errorMessage = result.message
                        )
                    }
                    null -> {
                        // No result yet
                    }
                }
            }
        }

        // Observe listening state
        viewModelScope.launch {
            speechRecognitionManager.isListening.collect { isListening ->
                _uiState.value = _uiState.value.copy(isListening = isListening)
            }
        }

        // Observe speaking state
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { isSpeaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = isSpeaking)

                // When TTS finishes speaking, mark listen step as completed
                if (!isSpeaking && _uiState.value.currentTutorialStep == TutorialStep.LISTEN && _uiState.value.hasPlayedSound) {
                    viewModelScope.launch {
                        delay(500) // Brief delay for better UX
                        advanceToNextStep()
                    }
                }
            }
        }
    }

    /**
     * Play the target word using TTS
     */
    fun playWord(word: String, languageCode: String = "es") {
        if (_uiState.value.currentTutorialStep != TutorialStep.LISTEN) return

        ttsManager.speak(word, languageCode)
        _uiState.value = _uiState.value.copy(hasPlayedSound = true)
    }

    /**
     * Start listening for user pronunciation
     */
    fun startListening(languageCode: String = "es-ES") {
        if (_uiState.value.currentTutorialStep != TutorialStep.PRONOUNCE) return

        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            pronunciationScore = null
        )
        speechRecognitionManager.startListening(languageCode)
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        speechRecognitionManager.stopListening()
    }

    /**
     * Handle successful speech recognition
     */
    private fun handleRecognitionSuccess(matches: List<String>) {
        val targetWord = _uiState.value.targetWord

        // Find best match and calculate score
        val result = PronunciationScorer.findBestMatch(targetWord, matches)
        if (result != null) {
            val (bestMatch, score) = result
            _uiState.value = _uiState.value.copy(
                pronunciationScore = score,
                spokenWord = bestMatch,
                isListening = false,
                showFeedback = true
            )

            // If pronunciation is correct, advance to next step
            if (score.isPass) {
                viewModelScope.launch {
                    delay(2000) // Show feedback for 2 seconds
                    dismissFeedback()
                    advanceToNextStep()
                }
            }
        }
    }

    /**
     * Toggle favorite/like status
     */
    fun toggleFavorite() {
        if (_uiState.value.currentTutorialStep != TutorialStep.FAVORITE) return

        _uiState.value = _uiState.value.copy(
            isFavorited = true
        )

        // Advance to completed step after favoriting
        viewModelScope.launch {
            delay(500)
            advanceToNextStep()
        }
    }

    /**
     * Advance to the next tutorial step
     */
    private fun advanceToNextStep() {
        val nextStep = when (_uiState.value.currentTutorialStep) {
            TutorialStep.LISTEN -> TutorialStep.PRONOUNCE
            TutorialStep.PRONOUNCE -> TutorialStep.FAVORITE
            TutorialStep.FAVORITE -> TutorialStep.COMPLETED
            TutorialStep.COMPLETED -> TutorialStep.COMPLETED
        }

        _uiState.value = _uiState.value.copy(
            currentTutorialStep = nextStep
        )
    }

    /**
     * Dismiss feedback dialog
     */
    fun dismissFeedback() {
        _uiState.value = _uiState.value.copy(
            showFeedback = false
        )
    }

    /**
     * Get instruction text for current step
     */
    fun getInstructionText(): String {
        return when (_uiState.value.currentTutorialStep) {
            TutorialStep.LISTEN -> "Tap the sound button to hear the word"
            TutorialStep.PRONOUNCE -> "Now try saying it! Tap the microphone"
            TutorialStep.FAVORITE -> "Great job! Tap the heart to save this word"
            TutorialStep.COMPLETED -> "Excellent! Moving on..."
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Don't shutdown here as managers are app-wide singletons
    }
}

/**
 * UI state for the exercise screen
 */
data class ExerciseUiState(
    val targetWord: String = "Perdón",
    val translation: String = "Sorry",
    val languageCode: String = "es",
    val recognitionLanguageCode: String = "es-ES",
    val isTtsReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val isFavorited: Boolean = false,
    val pronunciationScore: PronunciationScore? = null,
    val spokenWord: String? = null,
    val errorMessage: String? = null,
    val showFeedback: Boolean = false,
    val hasPlayedSound: Boolean = false,

    // Tutorial state
    val currentTutorialStep: TutorialStep = TutorialStep.LISTEN
)
