package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberRole
import com.example.data.model.RoomMemberEntity

@Composable
fun MemberAvatar(
    member: RoomMemberEntity,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 68.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val avatarColor = Color(member.avatarColorHex)
    val initialLetter = member.name.trim().take(1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(6.dp)
            .clickable { onClick() }
            .testTag("avatar_${member.memberId}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(avatarSize + 12.dp)
        ) {
            // Animated Speaking Ring
            if (member.isSpeaking) {
                Box(
                    modifier = Modifier
                        .size(avatarSize + 10.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676).copy(alpha = 0.35f))
                        .border(2.5.dp, Color(0xFF00E676), CircleShape)
                )
            }

            // Main Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(avatarColor)
                    .border(
                        width = if (member.role == MemberRole.HOST) 2.dp else 1.dp,
                        color = if (member.role == MemberRole.HOST) Color(0xFFFFD600) else Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = initialLetter,
                    color = Color.White,
                    fontSize = (avatarSize.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Host Badge
            if (member.role == MemberRole.HOST) {
                Surface(
                    color = Color(0xFFFFD600),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "میزبان",
                        tint = Color.Black,
                        modifier = Modifier
                            .padding(2.dp)
                            .fillMaxSize()
                    )
                }
            }

            // Mute Icon
            if (member.isMuted) {
                Surface(
                    color = Color(0xFF212121),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .border(1.dp, Color.Gray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "میکروفون خاموش",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxSize()
                    )
                }
            }

            // Raised Hand Indicator
            if (member.hasRaisedHand) {
                Surface(
                    color = Color(0xFFFF9800),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = "دست بالا",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Name
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = if (member.isSpeaking) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(avatarSize + 16.dp)
        )

        // Role Label
        val roleText = when (member.role) {
            MemberRole.HOST -> "میزبان 👑"
            MemberRole.SPEAKER -> "گوینده 🎙️"
            MemberRole.LISTENER -> "شنونده 🎧"
        }

        val roleBg = when (member.role) {
            MemberRole.HOST -> Color(0xFFFFD600).copy(alpha = 0.2f)
            MemberRole.SPEAKER -> Color(0xFF7C4DFF).copy(alpha = 0.2f)
            MemberRole.LISTENER -> Color.White.copy(alpha = 0.1f)
        }

        Surface(
            color = roleBg,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = roleText,
                style = MaterialTheme.typography.labelSmall,
                color = if (member.role == MemberRole.HOST) Color(0xFFFFD600) else Color(0xFFD1C4E9),
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
