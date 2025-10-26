package com.fouwaz.tokki_learn.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fouwaz.tokki_learn.lesson.Language
import com.fouwaz.tokki_learn.lesson.LessonRepository
import com.fouwaz.tokki_learn.lesson.model.Exercise
import com.fouwaz.tokki_learn.lesson.model.InputExercise
import com.fouwaz.tokki_learn.lesson.model.MultipleChoiceExercise
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class GateViewModel(
    private val targetPackageName: String,
    private val appLabel: String,
    private val lessonRepository: LessonRepository,
    private val language: Language
) : ViewModel() {

    private val _uiState: MutableStateFlow<GateUiState> =
        MutableStateFlow(GateUiState.Empty)
    val uiState: StateFlow<GateUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GateEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        loadExercises()
    }

    fun onMultipleChoiceSelected(option: String) {
        val currentState = _uiState.value
        val step = currentState.currentStep ?: return
        val exercise = step.exercise as? MultipleChoiceExercise ?: return
        val isCorrect = option.equals(exercise.correctAnswer, ignoreCase = true)
        val updatedStep = step.copy(
            multipleChoiceSelection = option,
            answerState = if (isCorrect) AnswerState.CORRECT else AnswerState.INCORRECT,
            attempts = step.attempts + 1
        )
        updateStep(updatedStep, advance = isCorrect)
    }

    fun onInputChanged(text: String) {
        val currentState = _uiState.value
        val step = currentState.currentStep ?: return
        if (step.exercise !is InputExercise) return
        val updatedStep = step.copy(
            inputValue = text,
            answerState = AnswerState.IDLE
        )
        replaceStep(updatedStep)
    }

    fun onInputSubmitted() {
        val currentState = _uiState.value
        val step = currentState.currentStep ?: return
        val exercise = step.exercise as? InputExercise ?: return
        val normalizedAnswer = step.inputValue.trim().lowercase(Locale.getDefault())
        val matches = exercise.acceptableAnswers.any { candidate ->
            normalizedAnswer == candidate.trim().lowercase(Locale.getDefault())
        }
        val updatedStep = step.copy(
            answerState = if (matches) AnswerState.CORRECT else AnswerState.INCORRECT,
            attempts = step.attempts + 1
        )
        updateStep(updatedStep, advance = matches)
    }

    fun onContinueToTargetApp() {
        _events.tryEmit(GateEvent.ContinueToApp(targetPackageName))
    }

    fun onContinueLearning() {
        _events.tryEmit(GateEvent.ContinueLearning)
    }

    private fun loadExercises() {
        viewModelScope.launch {
            val exercises = lessonRepository.getQuickGateExercises(language)
            val steps = exercises.map { ExerciseStep(exercise = it) }
            _uiState.value = GateUiState(
                appLabel = appLabel,
                targetPackageName = targetPackageName,
                steps = steps,
                currentStepIndex = 0,
                completed = false
            )
        }
    }

    private fun updateStep(step: ExerciseStep, advance: Boolean) {
        replaceStep(step)
        if (advance) {
            advanceToNextStep()
        }
    }

    private fun replaceStep(step: ExerciseStep) {
        val currentState = _uiState.value
        val index = currentState.currentStepIndex
        if (index !in currentState.steps.indices) return
        val newSteps = currentState.steps.toMutableList().apply {
            this[index] = step
        }
        _uiState.value = currentState.copy(steps = newSteps)
    }

    private fun advanceToNextStep() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentStepIndex + 1
        if (nextIndex >= currentState.steps.size) {
            _uiState.value = currentState.copy(
                completed = true,
                showSuccess = true
            )
            _events.tryEmit(GateEvent.Completed(targetPackageName))
        } else {
            _uiState.value = currentState.copy(
                currentStepIndex = nextIndex
            )
        }
    }

    companion object {
        fun factory(
            targetPackageName: String,
            appLabel: String,
            language: Language,
            lessonRepository: LessonRepository = LessonRepository()
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GateViewModel(
                        targetPackageName = targetPackageName,
                        appLabel = appLabel,
                        lessonRepository = lessonRepository,
                        language = language
                    ) as T
                }
            }
        }
    }
}

data class GateUiState(
    val appLabel: String,
    val targetPackageName: String,
    val steps: List<ExerciseStep>,
    val currentStepIndex: Int,
    val completed: Boolean,
    val showSuccess: Boolean = false
) {
    val currentStep: ExerciseStep?
        get() = steps.getOrNull(currentStepIndex)

    val progressLabel: String
        get() = "${currentStepIndex + 1}/${steps.size}"

    companion object {
        val Empty = GateUiState(
            appLabel = "",
            targetPackageName = "",
            steps = emptyList(),
            currentStepIndex = 0,
            completed = false
        )
    }
}

data class ExerciseStep(
    val exercise: Exercise,
    val multipleChoiceSelection: String? = null,
    val inputValue: String = "",
    val answerState: AnswerState = AnswerState.IDLE,
    val attempts: Int = 0
)

enum class AnswerState {
    IDLE,
    CORRECT,
    INCORRECT
}

sealed class GateEvent {
    data class Completed(val packageName: String) : GateEvent()
    data class ContinueToApp(val packageName: String) : GateEvent()
    data object ContinueLearning : GateEvent()
}
