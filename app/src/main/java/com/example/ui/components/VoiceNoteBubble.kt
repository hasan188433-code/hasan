package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VoiceNoteBubble(
    audioFilePath: String,
    durationMs: Long,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false
) {
    val durationSec = (durationMs / 1000).coerceAtLeast(1)
    val minutes = durationSec / 60
    val seconds = durationSec % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    Surface(
        color = if (isCurrentUser) Color(0xFF673AB7) else Color(0xFF2B254E),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .widthIn(min = 200.dp, max = 280.dp)
            .padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            // Play / Pause Button
            Surface(
                color = if (isCurrentUser) Color(0xFF00E676) else Color(0xFF7C4DFF),
                shape = CircleShape,
                modifier = Modifier
                    .size(38.dp)
                    .clickable { onPlayPauseToggle() }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "پخش پیام صوتی",
                    tint = Color.Black,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Waveform or Progress
                AudioWaveform(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    barCount = 14,
                    isSpeaking = isPlaying,
                    amplitude = if (isPlaying) 0.8f else 0.2f,
                    barColor = if (isCurrentUser) Color(0xFF00E676) else Color(0xFF00E5FF)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "پیام صوتی 🎙️",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
