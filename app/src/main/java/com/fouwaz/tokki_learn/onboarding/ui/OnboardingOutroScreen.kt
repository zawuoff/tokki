package com.fouwaz.tokki_learn.onboarding.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
fun OnboardingOutroScreen(
    onComplete: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notoSans = FontFamily(
        Font(R.font.notosans_regular, weight = FontWeight.Normal),
        Font(R.font.notosans_medium, weight = FontWeight.SemiBold)
    )

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
                                totalDrag < -dragThreshold -> onComplete()
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

                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.welcome_screen2),
                        contentDescription = "Tokki character waving",
                        modifier = Modifier.size(220.dp)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.wr_1),
                        contentDescription = "Word bubble 1",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = (-70).dp, y = (-70).dp)
                            .scale(glyph1Scale.value)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.wr_2),
                        contentDescription = "Word bubble 2",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 70.dp, y = (-70).dp)
                            .scale(glyph2Scale.value)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.wr_3),
                        contentDescription = "Word bubble 3",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = (-90).dp, y = 0.dp)
                            .scale(glyph3Scale.value)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.wr_4),
                        contentDescription = "Word bubble 4",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 90.dp, y = 0.dp)
                            .scale(glyph4Scale.value)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.wr_5),
                        contentDescription = "Word bubble 5",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 70.dp, y = 70.dp)
                            .scale(glyph5Scale.value)
                    )
                }

                Spacer(Modifier.height(afterImage5))

                Text(
                    text = stringResource(id = R.string.onboarding_outro_title),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(betweenTitleSubtitle10))

                Text(
                    text = stringResource(id = R.string.onboarding_outro_subtitle),
                    color = Color(0xFFB3B1B0),
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(afterSubtitle15))

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .width(226.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEFEEE7),
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.onboarding_cta),
                        fontFamily = notoSans,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(bottom8))
            }
        }
    }
}
