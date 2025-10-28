package com.fouwaz.tokki_learn.onboarding.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fouwaz.tokki_learn.R
import com.fouwaz.tokki_learn.onboarding.ExerciseViewModel
import com.fouwaz.tokki_learn.onboarding.TutorialStep
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingExerciseScreen(
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseViewModel = viewModel()
) {
    val notoSans = FontFamily(
        Font(R.font.notosans_regular, weight = FontWeight.Normal),
        Font(R.font.notosans_medium, weight = FontWeight.SemiBold),
        Font(R.font.notosans_bold, weight = FontWeight.Bold)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto-navigate when tutorial is completed
    LaunchedEffect(uiState.currentTutorialStep) {
        if (uiState.currentTutorialStep == TutorialStep.COMPLETED) {
            delay(1000)
            onNext()
        }
    }

    // Permission launcher for microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening(uiState.recognitionLanguageCode)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFBF9F3) // App's cream background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section with instruction and content
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    // "I'm sorry" image
                    Image(
                        painter = painterResource(id = R.drawable.im_sorry_image),
                        contentDescription = "I'm sorry image",
                        modifier = Modifier.size(180.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Spanish word "Perdón"
                    Text(
                        text = uiState.targetWord,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontFamily = notoSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        lineHeight = 44.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // English translation "Sorry"
                    Text(
                        text = uiState.translation,
                        color = Color(0xFFB3B1B0),
                        textAlign = TextAlign.Center,
                        fontFamily = notoSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        lineHeight = 28.sp
                    )

                    // Show listening indicator
                    if (uiState.isListening) {
                        Spacer(modifier = Modifier.height(24.dp))
                        ListeningIndicator()
                    }
                }

                // Bottom section with icons and tutorial guidance
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sound button
                        TutorialButton(
                            isActive = uiState.currentTutorialStep == TutorialStep.LISTEN,
                            isCompleted = uiState.currentTutorialStep.ordinal > TutorialStep.LISTEN.ordinal,
                            isEnabled = uiState.isTtsReady && uiState.currentTutorialStep == TutorialStep.LISTEN,
                            onClick = {
                                viewModel.playWord(uiState.targetWord, uiState.languageCode)
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sound_icon),
                                contentDescription = "Play sound",
                                modifier = Modifier.size(48.dp),
                                alpha = if (uiState.isTtsReady) 1f else 0.5f
                            )
                        }

                        // Microphone button
                        TutorialButton(
                            isActive = uiState.currentTutorialStep == TutorialStep.PRONOUNCE,
                            isCompleted = uiState.currentTutorialStep.ordinal > TutorialStep.PRONOUNCE.ordinal,
                            isEnabled = uiState.currentTutorialStep == TutorialStep.PRONOUNCE && !uiState.isListening,
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (uiState.isListening) {
                                    PulsingCircle()
                                }
                                Image(
                                    painter = painterResource(id = R.drawable.mic_icon),
                                    contentDescription = "Microphone",
                                    modifier = Modifier.size(48.dp),
                                    alpha = if (uiState.isListening) 0.5f else 1f
                                )
                            }
                        }

                        // Like/Favorite button
                        TutorialButton(
                            isActive = uiState.currentTutorialStep == TutorialStep.FAVORITE,
                            isCompleted = uiState.currentTutorialStep.ordinal > TutorialStep.FAVORITE.ordinal,
                            isEnabled = uiState.currentTutorialStep == TutorialStep.FAVORITE,
                            onClick = {
                                viewModel.toggleFavorite()
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.like_icon),
                                contentDescription = "Like/Favorite",
                                modifier = Modifier.size(48.dp),
                                colorFilter = if (uiState.isFavorited) {
                                    ColorFilter.tint(Color(0xFFFF6B6B))
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }

            // Bottom sheet for pronunciation feedback
            if (uiState.showFeedback && uiState.pronunciationScore != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.dismissFeedback() },
                    sheetState = sheetState,
                    containerColor = Color(0xFFFBF9F3),
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PronunciationFeedbackBottomSheet(
                        score = uiState.pronunciationScore!!,
                        onDismiss = { viewModel.dismissFeedback() },
                        onTryAgain = {
                            viewModel.dismissFeedback()
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        fontFamily = notoSans
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialButton(
    isActive: Boolean,
    isCompleted: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle pulsing background for active button
        if (isActive) {
            SubtleSpotlight()
            AnimatedHandPointer()
        }

        // Button container with subtle border
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    color = if (isActive) Color(0xFFEFEEE7) else Color.Transparent,
                    shape = CircleShape
                )
                .then(
                    if (isActive) {
                        Modifier.border(
                            width = 2.dp,
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
                .clickable(enabled = isEnabled, onClick = onClick)
                .alpha(if (isEnabled || isCompleted) 1f else 0.4f),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        // Checkmark for completed steps
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = Color.Black,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            )
        }
    }
}

@Composable
private fun SubtleSpotlight() {
    val scale by animateFloatAsState(
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spotlight"
    )

    Box(
        modifier = Modifier
            .size(90.dp)
            .scale(scale)
            .alpha(0.15f)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
private fun AnimatedHandPointer() {
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Canvas(
        modifier = Modifier
            .size(30.dp)
            .offset(y = (-70).dp + offsetY.value.dp)
    ) {
        val path = Path().apply {
            // Simple arrow pointing down
            moveTo(size.width / 2, size.height * 0.2f)
            lineTo(size.width / 2, size.height * 0.8f)
            // Arrow head
            moveTo(size.width / 2 - 8f, size.height * 0.65f)
            lineTo(size.width / 2, size.height * 0.8f)
            lineTo(size.width / 2 + 8f, size.height * 0.65f)
        }
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = 3f)
        )
    }
}

@Composable
private fun ListeningIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Listening...",
            color = Color(0xFFB3B1B0),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
        repeat(3) { index ->
            val animatedAlpha = remember { Animatable(0.3f) }
            LaunchedEffect(Unit) {
                animatedAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(animatedAlpha.value)
                    .background(Color(0xFFB3B1B0), CircleShape)
            )
        }
    }
}

@Composable
private fun PulsingCircle() {
    val scale by animateFloatAsState(
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing"
    )

    Box(
        modifier = Modifier
            .size(70.dp)
            .scale(scale)
            .background(Color(0xFFB3B1B0).copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
private fun PronunciationFeedbackBottomSheet(
    score: com.fouwaz.tokki_learn.speech.PronunciationScore,
    onDismiss: () -> Unit,
    onTryAgain: () -> Unit,
    fontFamily: FontFamily
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color(0xFFB3B1B0), RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Result emoji/icon
        Text(
            text = when {
                score.accuracy >= 90 -> "🎉"
                score.accuracy >= 75 -> "✨"
                score.accuracy >= 60 -> "👍"
                else -> "🤔"
            },
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback message
        Text(
            text = score.feedbackMessage,
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            color = Color.Black,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Accuracy score
        Text(
            text = "${score.accuracy.toInt()}% accurate",
            fontFamily = fontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFB3B1B0)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // What user said
        Text(
            text = "You said: \"${score.spoken}\"",
            fontFamily = fontFamily,
            fontSize = 14.sp,
            color = Color(0xFFB3B1B0),
            textAlign = TextAlign.Center
        )

        // Success message for passing score
        if (score.isPass) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Moving to next step...",
                fontFamily = fontFamily,
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.6f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        // Try again button for low scores
        if (!score.isPass) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onTryAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFEEE7),
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Try Again",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skip option
            Text(
                text = "Skip",
                fontFamily = fontFamily,
                fontSize = 14.sp,
                color = Color(0xFFB3B1B0),
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    }
}
