package com.fouwaz.tokki_learn.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import com.fouwaz.tokki_learn.R

@Composable
fun OnboardingWelcomeScreen(
    onContinue: () -> Unit,
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
        ) {
            val top15 = maxHeight * 0.15f
            val afterImage10 = maxHeight * 0.10f
            val betweenTitleSubtitle8 = maxHeight * 0.08f
            val afterSubtitle15 = maxHeight * 0.15f
            val bottom8 = maxHeight * 0.08f

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(top15))

                Image(
                    painter = painterResource(id = R.drawable.tokki_icon),
                    contentDescription = "Tokki icon",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(Modifier.height(afterImage10))

                Text(
                    text = stringResource(id = R.string.onboarding_title),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(betweenTitleSubtitle8))

                Text(
                    text = stringResource(id = R.string.onboarding_subtitle),
                    color = Color(0xFFB3B1B0),
                    textAlign = TextAlign.Center,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(afterSubtitle15))

                // Push remaining content to bottom, keep 8% bottom spacing
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onContinue,
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
