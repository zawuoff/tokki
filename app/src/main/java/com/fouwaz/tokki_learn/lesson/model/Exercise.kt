package com.fouwaz.tokki_learn.lesson.model

sealed interface Exercise {
    val id: String
    val prompt: String
    val correctAnswer: String
}

data class MultipleChoiceExercise(
    override val id: String,
    override val prompt: String,
    val options: List<String>,
    override val correctAnswer: String,
    val explanation: String? = null
) : Exercise

data class InputExercise(
    override val id: String,
    override val prompt: String,
    override val correctAnswer: String,
    val acceptableAnswers: List<String> = listOf(correctAnswer),
    val hint: String? = null
) : Exercise
