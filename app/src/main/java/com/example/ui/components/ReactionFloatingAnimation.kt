package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class FloatingReaction(
    val id: Long = System.currentTimeMillis() + Random.nextLong(1, 10000),
    val emoji: String,
    val startXOffsetDp: Float = Random.nextFloat() * 200f - 100f
)

@Composable
fun ReactionFloatingOverlay(
    reactions: List<FloatingReaction>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        reactions.forEach { reaction ->
            key(reaction.id) {
                SingleFloatingReactionItem(reaction = reaction)
            }
        }
    }
}

@Composable
private fun SingleFloatingReactionItem(reaction: FloatingReaction) {
    val transition = remember { Animatable(0f) }

    LaunchedEffect(reaction.id) {
        transition.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearOutSlowInEasing)
        )
    }

    val progress = transition.value
    val translateY = -400.dp * progress
    val alpha = (1f - progress).coerceIn(0f, 1f)
    val scale = 0.8f + (progress * 0.5f)

    Text(
        text = reaction.emoji,
        fontSize = 36.sp,
        modifier = Modifier
            .offset(x = reaction.startXOffsetDp.dp, y = translateY)
            .alpha(alpha)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}
