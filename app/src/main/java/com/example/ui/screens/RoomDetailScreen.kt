package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceAudioManager
import com.example.data.model.*
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    room: RoomEntity,
    members: List<RoomMemberEntity>,
    messages: List<MessageEntity>,
    currentUser: UserProfileEntity,
    isMicMuted: Boolean,
    hasRaisedHand: Boolean,
    micAmplitude: Float,
    floatingReactions: List<FloatingReaction>,
    voiceAudioManager: VoiceAudioManager,
    onToggleMicMute: () -> Unit,
    onToggleRaiseHand: () -> Unit,
    onSendTextMessage: (String) -> Unit,
    onSendReaction: (String) -> Unit,
    onStartRecordVoiceNote: () -> Unit,
    onStopRecordVoiceNote: () -> Unit,
    onLeaveRoom: () -> Unit
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var showChatSection by remember { mutableStateOf(true) }
    var isRecordingHold by remember { mutableStateOf(false) }

    val speakers = members.filter { it.role == MemberRole.HOST || it.role == MemberRole.SPEAKER }
    val listeners = members.filter { it.role == MemberRole.LISTENER }

    val playingFile by voiceAudioManager.currentlyPlayingFile.collectAsState()
    val playProgress by voiceAudioManager.playbackProgress.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0C20))) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = room.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF00E676).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "زنده 🔴",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${room.category} • ${members.size} آنلاین",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA5A0CA),
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onLeaveRoom,
                            modifier = Modifier.testTag("leave_room_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "خروج از اتاق",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Toggle Chat View
                        IconButton(
                            onClick = { showChatSection = !showChatSection },
                            modifier = Modifier.testTag("toggle_chat_button")
                        ) {
                            Icon(
                                imageVector = if (showChatSection) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                                contentDescription = "چت متنی",
                                tint = if (showChatSection) Color(0xFF00E676) else Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1B1736)
                    )
                )
            },
            bottomBar = {
                // Audio Room Main Control Dock
                Surface(
                    color = Color(0xFF1B1736),
                    tonalElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Reaction Quick Bar
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            listOf("❤️", "👏", "🔥", "😂", "🎤", "⚡").forEach { emoji ->
                                Surface(
                                    color = Color(0xFF262049),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable { onSendReaction(emoji) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        // Main Controls Row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Leave Button
                            OutlinedButton(
                                onClick = onLeaveRoom,
                                shape = CircleShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF5252)))
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = "خروج", tint = Color(0xFFFF5252))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("خروج", fontWeight = FontWeight.Bold)
                            }

                            // Mic Mute Toggle Button
                            Button(
                                onClick = onToggleMicMute,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMicMuted) Color(0xFFFF5252) else Color(0xFF00E676)
                                ),
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("mic_mute_button")
                            ) {
                                Icon(
                                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "میکروفون",
                                    tint = if (isMicMuted) Color.White else Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Raise Hand Button
                            IconButton(
                                onClick = onToggleRaiseHand,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(if (hasRaisedHand) Color(0xFFFF9800) else Color(0xFF262049))
                                    .testTag("raise_hand_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PanTool,
                                    contentDescription = "دست بالا",
                                    tint = Color.White
                                )
                            }
                        }

                        // Mic Amplitude Wave Bar when unmuted
                        if (!isMicMuted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "صدا در حال ضبط و پخش... ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF00E676),
                                    fontSize = 11.sp
                                )
                                AudioWaveform(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(18.dp),
                                    barCount = 12,
                                    isSpeaking = true,
                                    amplitude = micAmplitude,
                                    barColor = Color(0xFF00E676)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF0F0C20)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Audio Stage Section (Top half)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (showChatSection) 1.2f else 2f)
                        .padding(16.dp)
                ) {
                    // Speakers Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "گویندگان استیج (${speakers.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Speakers Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(speakers, key = { it.memberId }) { member ->
                            MemberAvatar(
                                member = member,
                                avatarSize = 64.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Listeners Section Header
                    if (listeners.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color(0xFFB388FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "شنوندگان (${listeners.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFA5A0CA)
                            )
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 60.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                        ) {
                            items(listeners, key = { it.memberId }) { member ->
                                MemberAvatar(
                                    member = member,
                                    avatarSize = 48.dp
                                )
                            }
                        }
                    }
                }

                // Text Chat Section (Bottom half)
                AnimatedVisibility(
                    visible = showChatSection,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Surface(
                        color = Color(0xFF16122D),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Chat Title Bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "گفتگوی متنی و صوتی 💬",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "${messages.size} پیام",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            Divider(color = Color(0xFF262049))

                            // Messages List
                            LazyColumn(
                                reverseLayout = true,
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(messages.reversed(), key = { it.id }) { msg ->
                                    if (msg.isSystemEvent) {
                                        // System Event Banner
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Surface(
                                                color = Color(0xFF262049).copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = msg.text,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFD1C4E9),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        val isMe = msg.senderId == currentUser.userId
                                        Column(
                                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = msg.senderName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(msg.senderAvatarColorHex),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
                                            )

                                            if (msg.audioFilePath != null) {
                                                // Voice Note Item
                                                val isPlayingThis = playingFile == msg.audioFilePath
                                                VoiceNoteBubble(
                                                    audioFilePath = msg.audioFilePath,
                                                    durationMs = msg.audioDurationMs,
                                                    isPlaying = isPlayingThis,
                                                    progress = if (isPlayingThis) playProgress else 0f,
                                                    onPlayPauseToggle = {
                                                        voiceAudioManager.playVoiceNote(msg.audioFilePath)
                                                    },
                                                    isCurrentUser = isMe
                                                )
                                            } else {
                                                // Text Bubble
                                                Surface(
                                                    color = if (isMe) Color(0xFF673AB7) else Color(0xFF262049),
                                                    shape = RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                                    )
                                                ) {
                                                    Text(
                                                        text = msg.text,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Chat Input Row (Text + Voice Note Recorder)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    placeholder = { Text("پیام خود را بنویسید...", color = Color.Gray) },
                                    maxLines = 3,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF262049),
                                        unfocusedContainerColor = Color(0xFF262049),
                                        focusedBorderColor = Color(0xFF7C4DFF),
                                        unfocusedBorderColor = Color(0xFF382F66),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chat_text_input")
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                if (textInput.isNotBlank()) {
                                    // Send Text Button
                                    IconButton(
                                        onClick = {
                                            onSendTextMessage(textInput)
                                            textInput = ""
                                        },
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF7C4DFF))
                                            .testTag("send_chat_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "ارسال",
                                            tint = Color.White
                                        )
                                    }
                                } else {
                                    // Voice Note Record Button (Hold or Tap to Record)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(if (isRecordingHold) Color(0xFFFF5252) else Color(0xFF00E676))
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onPress = {
                                                        isRecordingHold = true
                                                        onStartRecordVoiceNote()
                                                        Toast.makeText(context, "در حال ضبط پیام صوتی...", Toast.LENGTH_SHORT).show()
                                                        tryAwaitRelease()
                                                        isRecordingHold = false
                                                        onStopRecordVoiceNote()
                                                    }
                                                )
                                            }
                                            .testTag("voice_note_record_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "ضبط ویس",
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Reactions Animation Layer
        ReactionFloatingOverlay(reactions = floatingReactions)
    }
}
