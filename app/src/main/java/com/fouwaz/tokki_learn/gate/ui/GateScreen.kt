package com.fouwaz.tokki_learn.gate.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fouwaz.tokki_learn.gate.AnswerState
import com.fouwaz.tokki_learn.gate.ExerciseStep
import com.fouwaz.tokki_learn.gate.GateUiState
import com.fouwaz.tokki_learn.lesson.Language
import com.fouwaz.tokki_learn.lesson.LessonRepository
import com.fouwaz.tokki_learn.lesson.model.Exercise
import com.fouwaz.tokki_learn.lesson.model.InputExercise
import com.fouwaz.tokki_learn.lesson.model.MultipleChoiceExercise
import com.fouwaz.tokki_learn.ui.theme.Tokki_learnTheme

@Composable
fun GateScreen(
    state: GateUiState,
    onMultipleChoiceSelected: (String) -> Unit,
    onInputChanged: (String) -> Unit,
    onInputSubmitted: () -> Unit,
    onContinueToApp: () -> Unit,
    onContinueLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Toki check",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Before you continue to ${state.appLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (!state.showSuccess) {
                    Text(
                        text = "Exercise ${state.progressLabel}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                val currentStep = state.currentStep
                when {
                    state.showSuccess -> {
                        GateSuccessPane(
                            appLabel = state.appLabel,
                            onContinueToApp = onContinueToApp,
                            onContinueLearning = onContinueLearning
                        )
                    }
                    currentStep == null -> {
                        Text("No exercises available right now.")
                    }
                    else -> ExerciseStepPane(
                        step = currentStep,
                        onMultipleChoiceSelected = onMultipleChoiceSelected,
                        onInputChanged = onInputChanged,
                        onInputSubmitted = onInputSubmitted
                    )
                }
            }

            if (!state.showSuccess) {
                Text(
                    text = "Finish these quick tasks to unlock ${state.appLabel}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ExerciseStepPane(
    step: ExerciseStep,
    onMultipleChoiceSelected: (String) -> Unit,
    onInputChanged: (String) -> Unit,
    onInputSubmitted: () -> Unit
) {
    when (val exercise = step.exercise) {
        is MultipleChoiceExercise -> MultipleChoiceCard(
            step = step,
            exercise = exercise,
            onOptionSelected = onMultipleChoiceSelected
        )
        is InputExercise -> InputCard(
            step = step,
            exercise = exercise,
            onInputChanged = onInputChanged,
            onSubmit = onInputSubmitted
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MultipleChoiceCard(
    step: ExerciseStep,
    exercise: MultipleChoiceExercise,
    onOptionSelected: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = exercise.prompt,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        items(exercise.options) { option ->
            val isSelected = step.multipleChoiceSelection == option
            val state = if (isSelected) step.answerState else AnswerState.IDLE
            val borderColor = when (state) {
                AnswerState.CORRECT -> MaterialTheme.colorScheme.primary
                AnswerState.INCORRECT -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline
            }
            val borderStroke = BorderStroke(1.dp, SolidColor(borderColor))
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = borderStroke,
                onClick = { onOptionSelected(option) }
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
        if (step.answerState == AnswerState.INCORRECT) {
            item {
                Text(
                    text = "Not quite—try again!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else if (
            step.answerState == AnswerState.CORRECT &&
            !exercise.explanation.isNullOrBlank()
        ) {
            item {
                Text(
                    text = exercise.explanation!!,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InputCard(
    step: ExerciseStep,
    exercise: InputExercise,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        if (!exercise.hint.isNullOrBlank()) {
            Text(
                text = exercise.hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = step.inputValue,
            onValueChange = onInputChanged,
            label = { Text("Your answer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = step.answerState == AnswerState.INCORRECT
        )
        if (step.answerState == AnswerState.INCORRECT) {
            Text(
                text = "Double-check the spelling.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Button(
            onClick = onSubmit,
            enabled = step.inputValue.isNotBlank()
        ) {
            Text("Check answer")
        }
    }
}

@Composable
private fun GateSuccessPane(
    appLabel: String,
    onContinueToApp: () -> Unit,
    onContinueLearning: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Nice work!",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "You're good to continue to $appLabel or keep practicing with Toki.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onContinueToApp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue to $appLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Button(
            onClick = onContinueLearning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Keep learning in Toki")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GateScreenPreview() {
    val repository = LessonRepository()
    val exercises = repository.getQuickGateExercises(Language.SPANISH)
    val steps = exercises.map { ExerciseStep(it) }
    Tokki_learnTheme {
        GateScreen(
            state = GateUiState(
                appLabel = "Instagram",
                targetPackageName = "com.instagram.android",
                steps = steps,
                currentStepIndex = 0,
                completed = false
            ),
            onMultipleChoiceSelected = {},
            onInputChanged = {},
            onInputSubmitted = {},
            onContinueToApp = {},
            onContinueLearning = {}
        )
    }
}
