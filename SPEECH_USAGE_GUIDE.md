# Speech Components Usage Guide

Quick reference for using TTS and Speech Recognition anywhere in your app.

---

## Quick Start

### 1. Import the Manager
```kotlin
import com.fouwaz.tokki_learn.speech.SpeechManager
```

### 2. Get Instance
```kotlin
val ttsManager = SpeechManager.getTTSManager(context)
val speechManager = SpeechManager.getSpeechRecognitionManager(context)
```

### 3. Use It!
```kotlin
// Speak text
ttsManager.speak("Hola", languageCode = "es")

// Listen for speech
speechManager.startListening(languageCode = "es-ES")
```

---

## Common Use Cases

### Use Case 1: Vocabulary Practice Screen
```kotlin
@Composable
fun VocabularyPracticeScreen(word: VocabularyWord) {
    val context = LocalContext.current
    val tts = remember { SpeechManager.getTTSManager(context) }

    LaunchedEffect(Unit) {
        tts.initialize()
    }

    Column {
        Text(word.text, fontSize = 32.sp)
        Text(word.translation, fontSize = 18.sp, color = Color.Gray)

        Button(onClick = {
            tts.speak(word.text, word.languageCode)
        }) {
            Text("Hear Word")
        }
    }
}
```

### Use Case 2: Pronunciation Test
```kotlin
@Composable
fun PronunciationTestScreen(targetWord: String) {
    val context = LocalContext.current
    val speechRecognition = remember {
        SpeechManager.getSpeechRecognitionManager(context)
    }

    val result by speechRecognition.recognitionResult.collectAsState()

    LaunchedEffect(Unit) {
        speechRecognition.initialize()
    }

    Column {
        Text("Say: $targetWord")

        Button(onClick = {
            speechRecognition.startListening("es-ES")
        }) {
            Text("Start Recording")
        }

        when (val r = result) {
            is RecognitionResult.Success -> {
                val score = PronunciationScorer.score(targetWord, r.matches[0])
                Text("Score: ${score.accuracy.toInt()}%")
            }
            is RecognitionResult.Error -> {
                Text("Error: ${r.message}")
            }
            null -> Text("Ready to record")
        }
    }
}
```

### Use Case 3: Flashcard with Audio
```kotlin
@Composable
fun FlashcardScreen(flashcard: Flashcard) {
    val tts = SpeechManager.getTTSManager(LocalContext.current)
    var showAnswer by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showAnswer = !showAnswer
                if (showAnswer) {
                    tts.speak(flashcard.answer, flashcard.languageCode)
                }
            }
    ) {
        Text(
            text = if (showAnswer) flashcard.answer else flashcard.question,
            fontSize = 24.sp
        )
    }
}
```

### Use Case 4: Listening Quiz
```kotlin
@Composable
fun ListeningQuizScreen(quiz: Quiz) {
    val tts = SpeechManager.getTTSManager(LocalContext.current)
    val speech = SpeechManager.getSpeechRecognitionManager(LocalContext.current)
    var userAnswer by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Auto-play word when screen loads
        tts.initialize { success ->
            if (success) {
                tts.speak(quiz.word, quiz.languageCode)
            }
        }
    }

    Column {
        Text("What word did you hear?")

        Button(onClick = {
            tts.speak(quiz.word, quiz.languageCode)
        }) {
            Text("🔊 Play Again")
        }

        Button(onClick = {
            speech.startListening(quiz.languageCode)
        }) {
            Text("🎤 Answer")
        }

        // Check answer
        if (userAnswer.isNotEmpty()) {
            val isCorrect = PronunciationScorer.isAcceptableMatch(
                quiz.word,
                userAnswer,
                threshold = 70.0
            )
            Text(
                text = if (isCorrect) "✅ Correct!" else "❌ Try again",
                color = if (isCorrect) Color.Green else Color.Red
            )
        }
    }
}
```

### Use Case 5: Conversation Practice
```kotlin
@Composable
fun ConversationPracticeScreen(conversation: Conversation) {
    val tts = SpeechManager.getTTSManager(LocalContext.current)
    val speech = SpeechManager.getSpeechRecognitionManager(LocalContext.current)

    LaunchedEffect(conversation.currentLine) {
        // App speaks the prompt
        tts.speak(conversation.currentLine.prompt, "es")
    }

    Column {
        // Show conversation context
        Text(conversation.currentLine.prompt, fontSize = 18.sp)
        Text("Your turn to respond:", color = Color.Gray)

        Button(onClick = {
            speech.startListening("es-ES")
        }) {
            Text("🎤 Speak Your Response")
        }

        // Show user's response and score
        val result = speech.recognitionResult.collectAsState().value
        if (result is RecognitionResult.Success) {
            val userResponse = result.matches[0]
            val expectedResponse = conversation.currentLine.expectedResponse

            val score = PronunciationScorer.score(expectedResponse, userResponse)

            Text("You said: $userResponse")
            Text("Accuracy: ${score.accuracy.toInt()}%")

            if (score.accuracy >= 60) {
                Button(onClick = { /* Move to next line */ }) {
                    Text("Continue →")
                }
            }
        }
    }
}
```

---

## Supported Languages

### TTS Language Codes
```kotlin
val languages = mapOf(
    "Spanish" to "es",
    "French" to "fr",
    "German" to "de",
    "Italian" to "it",
    "Portuguese" to "pt",
    "Japanese" to "ja",
    "Korean" to "ko",
    "Chinese" to "zh",
    "English" to "en"
)
```

### Speech Recognition Language Codes
```kotlin
val recognitionLanguages = mapOf(
    "Spanish (Spain)" to "es-ES",
    "Spanish (Mexico)" to "es-MX",
    "French" to "fr-FR",
    "German" to "de-DE",
    "Italian" to "it-IT",
    "Portuguese" to "pt-PT",
    "Japanese" to "ja-JP",
    "Korean" to "ko-KR",
    "Chinese" to "zh-CN",
    "English (US)" to "en-US"
)
```

---

## Advanced Usage

### Adjusting Speech Rate
```kotlin
// In TTSManager.kt, modify the initialize() function:
tts?.setSpeechRate(0.7f) // 0.5f = very slow, 1.0f = normal, 2.0f = fast
```

### Custom Pronunciation Scoring Threshold
```kotlin
val isAcceptable = PronunciationScorer.isAcceptableMatch(
    target = "Perdón",
    spoken = "Perdon",
    threshold = 80.0 // Require 80% accuracy instead of default 75%
)
```

### Find Best Match from Multiple Results
```kotlin
val recognitionResults = listOf("Perdon", "Pardon", "Perdón")
val (bestMatch, score) = PronunciationScorer.findBestMatch(
    target = "Perdón",
    candidates = recognitionResults
)
// bestMatch = "Perdón", score.accuracy = 100.0
```

### Custom Feedback Messages
```kotlin
val score = PronunciationScorer.score("Hola", "Ola")

val customMessage = when {
    score.accuracy >= 95 -> "¡Perfecto! Native-like pronunciation!"
    score.accuracy >= 80 -> "Excellent! Very clear pronunciation"
    score.accuracy >= 65 -> "Good effort! Almost there"
    else -> "Keep practicing! You can do it!"
}
```

---

## State Observation

### Monitor TTS State
```kotlin
val isSpeaking by ttsManager.isSpeaking.collectAsState()

if (isSpeaking) {
    // Show speaking animation
    CircularProgressIndicator()
}
```

### Monitor Speech Recognition State
```kotlin
val isListening by speechRecognition.isListening.collectAsState()

if (isListening) {
    // Show listening animation
    Text("Listening...")
}
```

### Monitor Recognition Results
```kotlin
val result by speechRecognition.recognitionResult.collectAsState()

when (result) {
    is RecognitionResult.Success -> {
        Text("Heard: ${result.matches.joinToString()}")
    }
    is RecognitionResult.Error -> {
        Text("Error: ${result.message}")
    }
    null -> {
        Text("Ready to listen")
    }
}
```

---

## Best Practices

### 1. Always Initialize
```kotlin
LaunchedEffect(Unit) {
    tts.initialize { success ->
        if (success) {
            // Ready to use
        }
    }
}
```

### 2. Check Availability
```kotlin
if (speechRecognition.isAvailable()) {
    // Speech recognition is supported
} else {
    // Show alternative input method
}
```

### 3. Handle Permissions
```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) {
        speechRecognition.startListening()
    } else {
        // Show permission rationale
    }
}

Button(onClick = {
    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
}) {
    Text("Start Recording")
}
```

### 4. Clean Up Resources
```kotlin
// SpeechManager handles cleanup automatically
// But you can manually stop operations:

DisposableEffect(Unit) {
    onDispose {
        tts.stop() // Stop any ongoing speech
        speechRecognition.stopListening() // Stop listening
    }
}
```

### 5. Error Handling
```kotlin
speechRecognition.recognitionResult.collect { result ->
    when (result) {
        is RecognitionResult.Error -> {
            when (result.message) {
                "No match found" -> showToast("Couldn't understand. Try again!")
                "Network error" -> showToast("Check internet connection")
                "Speech timeout" -> showToast("No speech detected")
                else -> showToast("Error: ${result.message}")
            }
        }
        is RecognitionResult.Success -> {
            // Process results
        }
        null -> { /* Initial state */ }
    }
}
```

---

## Performance Tips

### 1. Reuse Manager Instances
```kotlin
// ✅ Good - Reuse singleton
val tts = SpeechManager.getTTSManager(context)

// ❌ Bad - Create new instance
val tts = TTSManager(context)
```

### 2. Initialize Early
```kotlin
// In Application class or MainActivity
class TokiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SpeechManager.initializeTTS(this)
        SpeechManager.initializeSpeechRecognition(this)
    }
}
```

### 3. Cache Common Words
```kotlin
// Pre-generate audio for frequently used words
val commonWords = listOf("Hola", "Gracias", "Adiós")
LaunchedEffect(Unit) {
    commonWords.forEach { word ->
        tts.speak(word, "es")
        delay(2000) // Wait for each to complete
    }
}
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| TTS not speaking | Check if `initialize()` was called and succeeded |
| No speech detected | Ensure `RECORD_AUDIO` permission is granted |
| Low accuracy scores | Use country-specific language codes (e.g., "es-ES" not "es") |
| App crashes on TTS | Check if device has TTS engine installed |
| Recognition times out | Speak within 2-3 seconds of starting |

---

## Summary

The speech components are now centralized, efficient, and easy to use throughout your app. Simply import `SpeechManager`, get the instance you need, and start building engaging language learning experiences!
