package com.fouwaz.tokki_learn.lesson

import com.fouwaz.tokki_learn.lesson.model.Exercise
import com.fouwaz.tokki_learn.lesson.model.InputExercise
import com.fouwaz.tokki_learn.lesson.model.MultipleChoiceExercise
import kotlin.random.Random

class LessonRepository {

    private val spanishExercises = listOf(
        MultipleChoiceExercise(
            id = "es_mc_1",
            prompt = "How do you say \"morning\" in Spanish?",
            options = listOf("tarde", "mañana", "noche"),
            correctAnswer = "mañana",
            explanation = "“Mañana” is morning; “tarde” is afternoon; “noche” is night."
        ),
        MultipleChoiceExercise(
            id = "es_mc_2",
            prompt = "Select the correct translation for “book”.",
            options = listOf("libro", "mesa", "gato"),
            correctAnswer = "libro",
            explanation = "“Libro” means book."
        ),
        InputExercise(
            id = "es_in_1",
            prompt = "Type the Spanish word for “thank you”.",
            correctAnswer = "gracias",
            acceptableAnswers = listOf("gracias")
        ),
        InputExercise(
            id = "es_in_2",
            prompt = "Fill in the missing word: “Buenos ____” (good day).",
            correctAnswer = "días",
            acceptableAnswers = listOf("dias", "días"),
            hint = "It literally means “good days”."
        )
    )

    private val frenchExercises = listOf(
        MultipleChoiceExercise(
            id = "fr_mc_1",
            prompt = "Choose the translation for “apple”.",
            options = listOf("pomme", "poire", "orange"),
            correctAnswer = "pomme",
            explanation = "“Pomme” means apple."
        ),
        MultipleChoiceExercise(
            id = "fr_mc_2",
            prompt = "Select the French word for “water”.",
            options = listOf("lait", "eau", "jus"),
            correctAnswer = "eau",
            explanation = "“Eau” is water."
        ),
        InputExercise(
            id = "fr_in_1",
            prompt = "Type the French word for “thank you”.",
            correctAnswer = "merci",
            acceptableAnswers = listOf("merci")
        ),
        InputExercise(
            id = "fr_in_2",
            prompt = "Fill in the blank: “Bonne ____” (good night).",
            correctAnswer = "nuit",
            acceptableAnswers = listOf("nuit")
        )
    )

    fun getQuickGateExercises(language: Language = Language.SPANISH): List<Exercise> {
        val pool = when (language) {
            Language.SPANISH -> spanishExercises
            Language.FRENCH -> frenchExercises
        }

        if (pool.isEmpty()) return emptyList()

        val shuffled = pool.shuffled(Random(System.currentTimeMillis()))
        return if (shuffled.size <= GATE_EXERCISE_COUNT) {
            shuffled
        } else {
            shuffled.take(GATE_EXERCISE_COUNT)
        }
    }

    companion object {
        private const val GATE_EXERCISE_COUNT = 2
    }
}
