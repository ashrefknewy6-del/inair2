package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStatus
import com.example.ui.theme.InAirCardBorder
import com.example.ui.theme.InAirGlow
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirRedDark
import com.example.ui.theme.InAirSecondary
import com.example.ui.theme.InAirSignatureGradient
import com.example.ui.theme.InAirSurface
import com.example.ui.theme.InAirSurfaceVariant

@Composable
fun InAirCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, InAirCardBorder, RoundedCornerShape(cornerRadius))
            .shadow(12.dp, RoundedCornerShape(cornerRadius), ambientColor = InAirGlow, spotColor = InAirPrimary),
        color = InAirSurface,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun InAirGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "inair_button"
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .testTag(testTag)
            .height(54.dp)
            .clip(CircleShape)
            .background(if (enabled) InAirSignatureGradient else Brush.linearGradient(listOf(Color(0xFF331518), Color(0xFF220D10))))
            .border(1.dp, if (enabled) Color(0x66FF2D3F) else Color.Transparent, CircleShape)
            .shadow(if (enabled) 12.dp else 0.dp, CircleShape, spotColor = InAirPrimary)
            .clickable(enabled = enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFF886065),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun PulsingPowerButton(
    status: BroadcastStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val isBroadcasting = status == BroadcastStatus.BROADCASTING

    // Radar infinite transitions
    val infiniteTransition = rememberInfiniteTransition(label = "power_pulse")
    val radar1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar1"
    )
    val radar2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 730, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar2"
    )
    val radar3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 1460, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar3"
    )

    // Breathing glow when idle
    val breathingGlow by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("power_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Radar waves drawing
        if (isBroadcasting) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = 68.dp.toPx()
                val maxExtra = 48.dp.toPx()

                listOf(radar1, radar2, radar3).forEach { progress ->
                    val r = baseRadius + progress * maxExtra
                    val alpha = (1f - progress).coerceIn(0f, 0.7f)
                    drawCircle(
                        color = Color(0xFFFF2D3F).copy(alpha = alpha * 0.5f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }

        // Central Power Button
        val scale = if (isBroadcasting) 1f else breathingGlow
        Box(
            modifier = Modifier
                .scale(scale)
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    if (isBroadcasting) {
                        InAirSignatureGradient
                    } else {
                        Brush.radialGradient(
                            listOf(Color(0xFF4A1016), InAirSurfaceVariant)
                        )
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isBroadcasting) Color(0xFFFF6B6B) else Color(0x4DFF2D3F),
                    shape = CircleShape
                )
                .shadow(
                    elevation = if (isBroadcasting) 24.dp else 8.dp,
                    shape = CircleShape,
                    spotColor = InAirPrimary,
                    ambientColor = InAirGlow
                )
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Broadcast Power Toggle",
                    tint = if (isBroadcasting) Color.White else InAirSecondary,
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (status) {
                        BroadcastStatus.OFFLINE -> "START"
                        BroadcastStatus.STARTING -> "STARTING"
                        BroadcastStatus.BROADCASTING -> "STOP"
                        BroadcastStatus.STOPPING -> "STOPPING"
                        BroadcastStatus.ERROR -> "RETRY"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    status: BroadcastStatus,
    modifier: Modifier = Modifier
) {
    val isLive = status == BroadcastStatus.BROADCASTING
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isLive) Color(0x33FF2D3F) else Color(0x22FFFFFF))
            .border(1.dp, if (isLive) Color(0x66FF2D3F) else Color(0x33FFFFFF), CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val transition = rememberInfiniteTransition(label = "chip_dot")
        val alpha by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_alpha"
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isLive) InAirPrimary.copy(alpha = alpha) else Color.Gray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (status) {
                BroadcastStatus.OFFLINE -> "Offline"
                BroadcastStatus.STARTING -> "Starting…"
                BroadcastStatus.BROADCASTING -> "Broadcasting Live"
                BroadcastStatus.STOPPING -> "Stopping…"
                BroadcastStatus.ERROR -> "Connection Error"
            },
            color = if (isLive) InAirSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun LiveBandwidthGraph(
    history: List<Float>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14080A))
            .border(1.dp, Color(0x22FF2D3F), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        if (history.isEmpty()) return@Box

        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxVal = (history.maxOrNull() ?: 100f).coerceAtLeast(10f)
            val w = size.width
            val h = size.height
            val stepX = w / (history.size - 1).coerceAtLeast(1)

            val path = Path()
            val fillPath = Path()

            history.forEachIndexed { i, v ->
                val x = i * stepX
                val y = h - (v / maxVal) * (h - 8f) - 4f
                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(w, h)
            fillPath.close()

            // Draw filled gradient area
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x40FF2D3F), Color(0x05FF2D3F)),
                    startY = 0f,
                    endY = h
                )
            )

            // Draw smooth top line
            drawPath(
                path = path,
                color = Color(0xFFFF2D3F),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun AnimatedGradientProgressBar(
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(progressFraction) {
        animProgress.animateTo(progressFraction, tween(300, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(Color(0xFF261014))
            .border(1.dp, Color(0x33FF2D3F), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animProgress.value)
                .height(10.dp)
                .clip(CircleShape)
                .background(InAirSignatureGradient)
        )
    }
}
