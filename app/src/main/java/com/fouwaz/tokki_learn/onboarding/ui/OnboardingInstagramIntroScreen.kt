package com.fouwaz.tokki_learn.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
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
import com.fouwaz.tokki_learn.R

@Composable
fun OnboardingInstagramIntroScreen(
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notoSans = FontFamily(
        Font(R.font.notosans_regular, weight = FontWeight.Normal),
        Font(R.font.notosans_medium, weight = FontWeight.SemiBold)
    )

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
                                totalDrag < -dragThreshold -> onNext()
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
            val betweenAppNameAndText10 = maxHeight * 0.10f
            val afterSubtitle15 = maxHeight * 0.15f
            val bottom8 = maxHeight * 0.08f

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(top10))

                Image(
                    painter = painterResource(id = R.drawable.insta_icon),
                    contentDescription = "Instagram icon",
                    modifier = Modifier.size(220.dp)
                )

                Spacer(Modifier.height(afterImage5))

                Text(
                    text = stringResource(id = R.string.instagram_intro_app_name),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(betweenAppNameAndText10))

                Text(
                    text = stringResource(id = R.string.instagram_intro_text_line1),
                    color = Color(0xFFB3B1B0),
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.Black))
                        append(stringResource(id = R.string.instagram_intro_text_line2_tokki))
                        pop()
                        append(" ")
                        append(stringResource(id = R.string.instagram_intro_text_line2_works))
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

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onNext,
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
                        text = stringResource(id = R.string.instagram_intro_button_next),
                        fontFamily = notoSans,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(bottom8))
            }
        }
    }
}
