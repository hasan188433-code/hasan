package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    isSpeaking: Boolean = true,
    amplitude: Float = 0.5f,
    barColor: Color = Color(0xFF00E676),
    inactiveColor: Color = Color(0x3300E676),
    barWidth: Dp = 3.dp,
    barGap: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    // Generate random phases for organic wave effect
    val phaseOffsets = remember { List(barCount) { Random.nextFloat() * 2f * Math.PI.toFloat() } }

    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    Canvas(modifier = modifier) {
        val totalWidth = (barWidth.toPx() + barGap.toPx()) * barCount
        val startX = (size.width - totalWidth) / 2f
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val x = startX + i * (barWidth.toPx() + barGap.toPx())

            val wave = if (isSpeaking) {
                val sinVal = Math.sin((animProgress + phaseOffsets[i]).toDouble()).toFloat()
                val base = 0.2f + 0.8f * ((sinVal + 1f) / 2f)
                (base * amplitude.coerceAtLeast(0.3f)).coerceIn(0.15f, 1.0f)
            } else {
                0.15f
            }

            val currentHeight = (maxHeight * wave).coerceAtLeast(barWidth.toPx())
            val y = (maxHeight - currentHeight) / 2f

            drawRoundRect(
                color = if (isSpeaking) barColor else inactiveColor,
                topLeft = Offset(x, y),
                size = Size(barWidth.toPx(), currentHeight),
                cornerRadius = CornerRadius(barWidth.toPx() / 2, barWidth.toPx() / 2)
            )
        }
    }
}
