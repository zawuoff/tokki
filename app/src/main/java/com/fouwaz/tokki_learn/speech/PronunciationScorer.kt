package com.fouwaz.tokki_learn.speech

import kotlin.math.max

/**
 * Utility class for scoring pronunciation accuracy
 * Uses string similarity algorithms to compare user input with target word
 */
object PronunciationScorer {

    /**
     * Calculate pronunciation accuracy score
     * @param target The correct word/phrase
     * @param spoken The user's spoken word/phrase (from speech recognition)
     * @return PronunciationScore object with accuracy percentage and grade
     */
    fun score(target: String, spoken: String): PronunciationScore {
        val normalizedTarget = target.lowercase().trim()
        val normalizedSpoken = spoken.lowercase().trim()

        // Calculate Levenshtein distance
        val distance = levenshteinDistance(normalizedTarget, normalizedSpoken)
        val maxLength = max(normalizedTarget.length, normalizedSpoken.length)

        // Calculate similarity percentage
        val similarity = if (maxLength == 0) {
            100.0
        } else {
            ((maxLength - distance).toDouble() / maxLength) * 100.0
        }

        // Determine grade based on accuracy
        val grade = when {
            similarity >= 90 -> PronunciationGrade.EXCELLENT
            similarity >= 75 -> PronunciationGrade.GOOD
            similarity >= 60 -> PronunciationGrade.FAIR
            else -> PronunciationGrade.NEEDS_PRACTICE
        }

        return PronunciationScore(
            accuracy = similarity,
            grade = grade,
            target = target,
            spoken = spoken
        )
    }

    /**
     * Calculate Levenshtein distance between two strings
     * This represents the minimum number of single-character edits needed
     * to change one string into another
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length

        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) {
            dp[i][0] = i
        }

        for (j in 0..len2) {
            dp[0][j] = j
        }

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return dp[len1][len2]
    }

    /**
     * Check if the spoken word matches the target within acceptable threshold
     * @param target The correct word
     * @param spoken The spoken word
     * @param threshold Minimum accuracy percentage to consider as match (default 75%)
     * @return true if match is acceptable, false otherwise
     */
    fun isAcceptableMatch(target: String, spoken: String, threshold: Double = 75.0): Boolean {
        val score = score(target, spoken)
        return score.accuracy >= threshold
    }

    /**
     * Find the best match from multiple recognition results
     * @param target The correct word
     * @param candidates List of possible matches from speech recognition
     * @return The best matching candidate with its score
     */
    fun findBestMatch(target: String, candidates: List<String>): Pair<String, PronunciationScore>? {
        if (candidates.isEmpty()) return null

        var bestMatch: String = candidates[0]
        var bestScore = score(target, candidates[0])

        for (candidate in candidates.drop(1)) {
            val currentScore = score(target, candidate)
            if (currentScore.accuracy > bestScore.accuracy) {
                bestMatch = candidate
                bestScore = currentScore
            }
        }

        return Pair(bestMatch, bestScore)
    }
}

/**
 * Data class representing a pronunciation score
 */
data class PronunciationScore(
    val accuracy: Double,
    val grade: PronunciationGrade,
    val target: String,
    val spoken: String
) {
    val isPass: Boolean
        get() = accuracy >= 60.0

    val feedbackMessage: String
        get() = when (grade) {
            PronunciationGrade.EXCELLENT -> "Perfect! Excellent pronunciation! 🎉"
            PronunciationGrade.GOOD -> "Great job! Good pronunciation!"
            PronunciationGrade.FAIR -> "Not bad! Keep practicing!"
            PronunciationGrade.NEEDS_PRACTICE -> "Keep trying! You'll get it!"
        }
}

/**
 * Enum representing pronunciation quality grades
 */
enum class PronunciationGrade {
    EXCELLENT,      // 90-100% accuracy
    GOOD,           // 75-89% accuracy
    FAIR,           // 60-74% accuracy
    NEEDS_PRACTICE  // Below 60% accuracy
}
