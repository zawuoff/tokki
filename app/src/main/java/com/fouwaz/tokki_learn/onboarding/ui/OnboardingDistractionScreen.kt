package com.fouwaz.tokki_learn.onboarding.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import kotlinx.coroutines.delay
import com.fouwaz.tokki_learn.R

@Composable
fun OnboardingDistractionScreen(
    onContinue: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notoSans = FontFamily(
        Font(R.font.notosans_regular, weight = FontWeight.Normal),
        Font(R.font.notosans_medium, weight = FontWeight.SemiBold)
    )

    // Glyph animation states
    val glyph1Scale = remember { Animatable(0f) }
    val glyph2Scale = remember { Animatable(0f) }
    val glyph3Scale = remember { Animatable(0f) }
    val glyph4Scale = remember { Animatable(0f) }
    val glyph5Scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val glyphs = listOf(glyph1Scale, glyph2Scale, glyph3Scale, glyph4Scale, glyph5Scale)
        glyphs.forEachIndexed { index, animatable ->
            delay(70L * index)
            animatable.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFBF9F3)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    val dragThreshold = 120f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            totalDrag += dragAmount
                            change.consumePositionChange()
                        },
                        onDragEnd = {
                            when {
                                totalDrag > dragThreshold -> onPrevious()
                                totalDrag < -dragThreshold -> onContinue()
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = {
                            totalDrag = 0f
                        }
                    )
                }
        ) {
            val top10 = maxHeight * 0.10f
            val afterImage5 = maxHeight * 0.05f
            val betweenTitleSubtitle10 = maxHeight * 0.10f
            val afterSubtitle15 = maxHeight * 0.15f
            val bottom8 = maxHeight * 0.08f

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(top10))

                // Hero image with animated glyphs
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Main hero image
                    Image(
                        painter = painterResource(id = R.drawable.welcome_screen_1),
                        contentDescription = "Learning illustration",
                        modifier = Modifier.size(220.dp)
                    )

                    // Glyph positions around the hero (relative to Box center)
                    // Top-left
                    Image(
                        painter = painterResource(id = R.drawable.ch_1),
                        contentDescription = "Character 1",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = (-70).dp, y = (-70).dp)
                            .scale(glyph1Scale.value)
                    )

                    // Top-right
                    Image(
                        painter = painterResource(id = R.drawable.ch_2),
                        contentDescription = "Character 2",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 70.dp, y = (-70).dp)
                            .scale(glyph2Scale.value)
                    )

                    // Left
                    Image(
                        painter = painterResource(id = R.drawable.ch_3),
                        contentDescription = "Character 3",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = (-90).dp, y = 0.dp)
                            .scale(glyph3Scale.value)
                    )

                    // Right
                    Image(
                        painter = painterResource(id = R.drawable.ch_4),
                        contentDescription = "Character 4",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 90.dp, y = 0.dp)
                            .scale(glyph4Scale.value)
                    )

                    // Bottom-right
                    Image(
                        painter = painterResource(id = R.drawable.ch_5),
                        contentDescription = "Character 5",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 70.dp, y = 70.dp)
                            .scale(glyph5Scale.value)
                    )
                }

                Spacer(Modifier.height(afterImage5))

                Text(
                    text = stringResource(id = R.string.onboarding_distraction_title),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(betweenTitleSubtitle10))

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(id = R.string.onboarding_distraction_subtitle_first))
                        append(" ")
                        pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.Black))
                        append(stringResource(id = R.string.onboarding_distraction_subtitle_distractions))
                        pop()
                        append(" ")
                        append(stringResource(id = R.string.onboarding_distraction_subtitle_equals))
                        append(" ")
                        append(stringResource(id = R.string.onboarding_distraction_subtitle_second_num))
                        append(" ")
                        pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.Black))
                        append(stringResource(id = R.string.onboarding_distraction_subtitle_words))
                        pop()
                        append(" ")
                        pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.Black))
                        append(stringResource(id = R.string.onboarding_distraction_subtitle_learned))
                        pop()
                    },
                    color = Color(0xFFB3B1B0),
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(afterSubtitle15))

                // Push remaining content to bottom
                Spacer(modifier = Modifier.weight(1f))

                // Page indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    // Page 1 - Completed (gray)
                    Spacer(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(Color(0xFFB3B1B0))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Page 2 - Current (black)
                    Spacer(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(Color.Black)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Page 3 - Upcoming (gray)
                    Spacer(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(Color(0xFFB3B1B0))
                    )
                }

                Spacer(Modifier.height(bottom8))
            }
        }
    }
}
