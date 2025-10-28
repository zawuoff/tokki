# Tokki Learn - Interactive Tutorial System

## Overview
Transformed the exercise screen into an engaging, step-by-step interactive tutorial that guides users through their first language learning experience. The system is now **reusable app-wide** and not limited to onboarding.

---

## Tutorial Flow

### Step 1: LISTEN 🔊
**Instruction**: "Tap the sound button to hear the word"

**Visual Cues**:
- Pulsing green spotlight around sound button
- Animated hand/pointer gesture above button
- Gradient border (green → blue) around button
- Other buttons are dimmed (30% opacity)

**User Action**: Tap sound button → Hear "Perdón" in Spanish

**Completion**: Auto-advances after TTS finishes speaking (500ms delay)

---

### Step 2: PRONOUNCE 🎤
**Instruction**: "Now try saying it! Tap the microphone"

**Visual Cues**:
- Spotlight and pointer move to microphone button
- Green checkmark appears on sound button (completed)
- "Listening..." indicator with animated dots appears when recording
- Pulsing blue circle during speech recognition

**User Action**:
1. Tap microphone → Grant permission if needed
2. Speak "Perdón" clearly
3. Receive instant feedback with accuracy score

**Completion Requirements**:
- Must achieve ≥60% pronunciation accuracy to advance
- If accuracy is too low, user can:
  - Try again (tap "Try Again" button)
  - Skip (tap "Skip" button in feedback dialog)
- Auto-advances after 2 seconds when pronunciation is correct

---

### Step 3: FAVORITE ❤️
**Instruction**: "Great job! Tap the heart to save this word"

**Visual Cues**:
- Spotlight and pointer move to heart button
- Green checkmarks on both sound and mic buttons
- Heart button is now enabled and active

**User Action**: Tap heart button → Button turns red (favorited)

**Completion**: Auto-advances after 500ms

---

### Step 4: COMPLETED ✅
**Message**: "Excellent! Moving on..."

**Behavior**:
- Brief 1-second display of completion message
- **Automatic navigation** to next screen
- All three buttons show green checkmarks

---

## Visual Design Elements

### 1. Instruction Banner
- Light blue background (`#E3F2FD`)
- Blue text (`#1976D2`)
- Rounded corners (12dp)
- Animated fade-in/fade-out when changing steps
- Positioned at top of screen

### 2. Spotlight Effect
```
- Radial gradient (green with transparency)
- Pulsing scale animation (1.0x → 1.5x → 1.0x)
- 1500ms duration
- Infinite repeat
```

### 3. Animated Hand Pointer
```
- Canvas-drawn arrow pointing downward
- Bouncing animation (0 → 10dp → 0)
- 800ms duration
- Green color (#4CAF50)
- Positioned 80dp above active button
```

### 4. Button States
| State | Visual Treatment |
|-------|------------------|
| **Active** | Spotlight + Pointer + Gradient border + Full opacity |
| **Completed** | Green checkmark + Full opacity + No interaction |
| **Locked** | 30% opacity + No interaction |
| **Enabled** | Full opacity + Interactive |

### 5. Checkmark Badge
- Material Icon: `CheckCircle`
- Green color (`#4CAF50`)
- 24dp size
- Positioned at top-right corner of button
- Offset: (+8dp, -8dp)

---

## Reusable Architecture

### SpeechManager (Singleton)
**Location**: `app/src/main/java/com/fouwaz/tokki_learn/speech/SpeechManager.kt`

**Purpose**: App-wide singleton for TTS and Speech Recognition management

**Features**:
- Prevents multiple instances of TTS/Speech engines
- Proper lifecycle management
- Thread-safe initialization
- Context-independent (uses applicationContext)

**Usage Anywhere in App**:
```kotlin
// Initialize once in Application class or first usage
SpeechManager.initializeTTS(context) { success ->
    // TTS ready
}

// Use anywhere
val ttsManager = SpeechManager.getTTSManager(context)
ttsManager.speak("Hello", "en")

val speechManager = SpeechManager.getSpeechRecognitionManager(context)
speechManager.startListening("en-US")
```

---

## State Machine

### TutorialStep Enum
```kotlin
enum class TutorialStep {
    LISTEN,          // Must click sound button
    PRONOUNCE,       // Must speak correctly
    FAVORITE,        // Must click heart button
    COMPLETED        // All steps done → auto-navigate
}
```

### State Transitions
```
LISTEN → (TTS finishes) → PRONOUNCE
PRONOUNCE → (Pronunciation ≥60%) → FAVORITE
FAVORITE → (Heart clicked) → COMPLETED
COMPLETED → (1 second) → Navigate to next screen
```

### Button Enabling Logic
```kotlin
Sound Button:   enabled = (step == LISTEN && ttsReady)
Mic Button:     enabled = (step == PRONOUNCE && !isListening)
Heart Button:   enabled = (step == FAVORITE)
```

---

## Auto-Progression Logic

### 1. Sound Button
```kotlin
// In ExerciseViewModel.init
ttsManager.isSpeaking.collect { isSpeaking ->
    if (!isSpeaking && currentStep == LISTEN && hasPlayedSound) {
        delay(500ms)
        advanceToNextStep() // → PRONOUNCE
    }
}
```

### 2. Microphone Button
```kotlin
// In handleRecognitionSuccess
if (score.isPass) {
    delay(2000ms) // Show feedback
    dismissFeedback()
    advanceToNextStep() // → FAVORITE
}
```

### 3. Heart Button
```kotlin
// In toggleFavorite
isFavorited = true
delay(500ms)
advanceToNextStep() // → COMPLETED
```

### 4. Completion
```kotlin
// In OnboardingExerciseScreen
LaunchedEffect(currentStep) {
    if (currentStep == COMPLETED) {
        delay(1000ms)
        onNext() // Navigate to next screen
    }
}
```

---

## Feedback System

### Pronunciation Feedback Dialog

**Success (≥60% accuracy)**:
- Title: "Perfect! Excellent pronunciation! 🎉" or "Great job!" etc.
- Shows accuracy percentage
- Shows what user said
- Message: "Moving to next step..."
- Auto-closes after 2 seconds
- Auto-advances to FAVORITE step

**Needs Improvement (<60% accuracy)**:
- Title: "Keep trying! You'll get it!"
- Shows accuracy percentage
- Shows what user said
- Buttons:
  - "Try Again" (blue) → Relaunch speech recognition
  - "Skip" → Move to next step anyway

---

## Usage Beyond Onboarding

### Example: Exercise Library Screen
```kotlin
@Composable
fun ExerciseScreen(word: Word) {
    val viewModel: ExerciseViewModel = viewModel()
    val speechManager = SpeechManager.getTTSManager(context)

    Button(onClick = {
        speechManager.speak(word.text, word.languageCode)
    }) {
        Text("Hear Word")
    }
}
```

### Example: Flashcard Screen
```kotlin
@Composable
fun FlashcardScreen() {
    val tts = SpeechManager.getTTSManager(LocalContext.current)
    val speech = SpeechManager.getSpeechRecognitionManager(LocalContext.current)

    // Use for pronunciation practice
    // Use for listening exercises
    // Use for vocabulary review
}
```

---

## Benefits of New Design

### 1. **Guided Learning**
- Users can't skip ahead
- Learn features in logical order
- Guaranteed completion of tutorial

### 2. **Visual Clarity**
- Always know what to do next
- Clear visual hierarchy
- Immediate feedback

### 3. **Engagement**
- Interactive animations
- Satisfying progression (checkmarks)
- Gamified feel

### 4. **Reusability**
- SpeechManager can be used anywhere
- TutorialButton component is reusable
- State machine pattern is extensible

### 5. **Zero Friction**
- Auto-progression reduces taps
- No "Next" button needed
- Smooth transitions

---

## Technical Improvements

### Before → After

| Aspect | Before | After |
|--------|--------|-------|
| **TTS Management** | Per-screen instances | App-wide singleton |
| **Speech Recognition** | Per-screen instances | App-wide singleton |
| **Resource Cleanup** | Manual in onCleared | Automatic in SpeechManager |
| **User Flow** | Free-form exploration | Guided step-by-step |
| **Visual Guidance** | None | Spotlight, pointer, checkmarks |
| **Navigation** | Manual "Next" button | Auto-advance |
| **Button States** | Always enabled | Context-aware enabling |

---

## Files Modified/Created

### New Files:
1. `SpeechManager.kt` - App-wide singleton for speech features
2. `TUTORIAL_SYSTEM.md` - This documentation

### Modified Files:
1. `ExerciseViewModel.kt` - Added tutorial state machine
2. `OnboardingExerciseScreen.kt` - Added visual guidance and tutorial flow

---

## Future Enhancements

### Phase 1: Haptic Feedback
- Vibrate on successful pronunciation
- Vibrate on step completion
- Tactile confirmation

### Phase 2: Progress Persistence
- Save tutorial progress to DataStore
- Resume from last completed step
- Track user's first-time experience

### Phase 3: Tutorial Analytics
- Track time spent on each step
- Track pronunciation attempts before success
- Identify common pain points

### Phase 4: Adaptive Tutorial
- Skip tutorial if user demonstrates proficiency
- Adjust difficulty based on performance
- Personalized instruction text

### Phase 5: Multi-Exercise Tutorial
- Extend tutorial to multiple words
- Progressive difficulty
- Build confidence gradually

---

## Testing Checklist

- [ ] Sound button plays word correctly
- [ ] Spotlight animates on active button
- [ ] Hand pointer bounces above active button
- [ ] Inactive buttons are disabled and dimmed
- [ ] Checkmarks appear on completed steps
- [ ] TTS auto-advances to PRONOUNCE step
- [ ] Microphone permission request works
- [ ] Speech recognition captures audio
- [ ] Pronunciation feedback dialog shows correct score
- [ ] Auto-advance after correct pronunciation
- [ ] "Try Again" button works for low scores
- [ ] "Skip" button bypasses pronunciation requirement
- [ ] Heart button favorites the word (turns red)
- [ ] Auto-advance to COMPLETED step
- [ ] Screen auto-navigates after COMPLETED
- [ ] All animations are smooth
- [ ] No memory leaks (check with Android Profiler)
- [ ] Works on different screen sizes
- [ ] Works with different Android versions (API 26+)

---

## Summary

The tutorial system transforms the learning experience from passive observation to active participation. Users are guided through each interaction with clear visual cues and automatic progression, ensuring they understand all features before continuing. The underlying speech components are now reusable throughout the entire app, making it easy to add pronunciation practice and listening exercises anywhere in the application.

**Key Achievement**: Tutorial completion rate should be near 100% due to guided nature and auto-progression.
