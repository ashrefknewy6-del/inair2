package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.InAirBackground
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val scaleAnim = remember { Animatable(0f) }
    val arc1Anim = remember { Animatable(0f) }
    val arc2Anim = remember { Animatable(0f) }
    val arc3Anim = remember { Animatable(0f) }
    val fadeAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Phone scales in
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        // Three arcs draw sequentially
        arc1Anim.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
        arc2Anim.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
        arc3Anim.animateTo(1f, tween(350, easing = FastOutSlowInEasing))

        fadeAnim.animateTo(1f, tween(400))
        delay(600)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InAirBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scaleAnim.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val phoneTop = h * 0.48f

                    // Draw Arc 1 (inner)
                    if (arc1Anim.value > 0f) {
                        val path1 = Path().apply {
                            val r1 = 18.dp.toPx()
                            val sweep1 = 100f * arc1Anim.value
                            arcTo(
                                rect = Rect(cx - r1, phoneTop - r1 - 8.dp.toPx(), cx + r1, phoneTop + r1 - 8.dp.toPx()),
                                startAngleDegrees = 220f - sweep1 / 2f,
                                sweepAngleDegrees = sweep1,
                                forceMoveTo = false
                            )
                        }
                        drawPath(path1, InAirPrimary, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
                    }

                    // Draw Arc 2 (middle)
                    if (arc2Anim.value > 0f) {
                        val path2 = Path().apply {
                            val r2 = 32.dp.toPx()
                            val sweep2 = 110f * arc2Anim.value
                            arcTo(
                                rect = Rect(cx - r2, phoneTop - r2 - 8.dp.toPx(), cx + r2, phoneTop + r2 - 8.dp.toPx()),
                                startAngleDegrees = 220f - sweep2 / 2f,
                                sweepAngleDegrees = sweep2,
                                forceMoveTo = false
                            )
                        }
                        drawPath(path2, InAirPrimary, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
                    }

                    // Draw Arc 3 (outer)
                    if (arc3Anim.value > 0f) {
                        val path3 = Path().apply {
                            val r3 = 46.dp.toPx()
                            val sweep3 = 120f * arc3Anim.value
                            arcTo(
                                rect = Rect(cx - r3, phoneTop - r3 - 8.dp.toPx(), cx + r3, phoneTop + r3 - 8.dp.toPx()),
                                startAngleDegrees = 220f - sweep3 / 2f,
                                sweepAngleDegrees = sweep3,
                                forceMoveTo = false
                            )
                        }
                        drawPath(path3, InAirSecondary, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
                    }

                    // Draw Smartphone Outline
                    val phoneW = 54.dp.toPx()
                    val phoneH = 78.dp.toPx()
                    val phoneRect = Rect(cx - phoneW / 2f, phoneTop, cx + phoneW / 2f, phoneTop + phoneH)
                    drawRoundRect(
                        color = InAirPrimary,
                        topLeft = phoneRect.topLeft,
                        size = phoneRect.size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                        style = Stroke(width = 3.5.dp.toPx())
                    )

                    // Phone screen inner glow
                    val screenW = 42.dp.toPx()
                    val screenH = 58.dp.toPx()
                    drawRoundRect(
                        color = Color(0x33FF2D3F),
                        topLeft = Offset(cx - screenW / 2f, phoneTop + 8.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(screenW, screenH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.tagline),
                color = InAirSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
