package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.VoiceHubTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VoiceHubTheme {
                val viewModel: MainViewModel = viewModel()

                val rooms by viewModel.rooms.collectAsStateWithLifecycle()
                val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUserProfile.collectAsStateWithLifecycle()

                val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
                val activeMembers by viewModel.activeMembers.collectAsStateWithLifecycle()
                val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()

                val isMicMuted by viewModel.isMicMuted.collectAsStateWithLifecycle()
                val hasRaisedHand by viewModel.hasRaisedHand.collectAsStateWithLifecycle()
                val micAmplitude by viewModel.voiceAudioManager.micAmplitude.collectAsStateWithLifecycle()
                val floatingReactions by viewModel.floatingReactions.collectAsStateWithLifecycle()

                var showCreateRoomDialog by remember { mutableStateOf(false) }
                var showProfileDialog by remember { mutableStateOf(false) }

                // Request RECORD_AUDIO Permission Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(this, "دسترسی میکروفون فعال شد 🎙️", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "برای گفتگو صوتی، دسترسی میکروفون لازم است", Toast.LENGTH_LONG).show()
                    }
                }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0C20)
                ) {
                    val roomState = activeRoom

                    if (roomState != null) {
                        // Back press handles leaving the room
                        BackHandler {
                            viewModel.leaveCurrentRoom()
                        }

                        RoomDetailScreen(
                            room = roomState,
                            members = activeMembers,
                            messages = activeMessages,
                            currentUser = currentUser,
                            isMicMuted = isMicMuted,
                            hasRaisedHand = hasRaisedHand,
                            micAmplitude = micAmplitude,
                            floatingReactions = floatingReactions,
                            voiceAudioManager = viewModel.voiceAudioManager,
                            onToggleMicMute = {
                                if (ContextCompat.checkSelfPermission(
                                        this@MainActivity,
                                        Manifest.permission.RECORD_AUDIO
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    viewModel.toggleMicMute()
                                }
                            },
                            onToggleRaiseHand = { viewModel.toggleRaiseHand() },
                            onSendTextMessage = { text -> viewModel.sendTextMessage(text) },
                            onSendReaction = { emoji -> viewModel.sendReaction(emoji) },
                            onStartRecordVoiceNote = {
                                if (ContextCompat.checkSelfPermission(
                                        this@MainActivity,
                                        Manifest.permission.RECORD_AUDIO
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    viewModel.startRecordingVoiceNote()
                                }
                            },
                            onStopRecordVoiceNote = { viewModel.stopAndSendVoiceNote() },
                            onLeaveRoom = { viewModel.leaveCurrentRoom() }
                        )
                    } else {
                        LobbyScreen(
                            rooms = rooms,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            currentUser = currentUser,
                            onCategorySelected = { cat -> viewModel.selectCategory(cat) },
                            onSearchQueryChanged = { query -> viewModel.updateSearchQuery(query) },
                            onJoinRoom = { room ->
                                viewModel.joinRoom(room.roomId)
                            },
                            onCreateRoomClick = { showCreateRoomDialog = true },
                            onProfileClick = { showProfileDialog = true }
                        )
                    }

                    if (showCreateRoomDialog) {
                        CreateRoomDialog(
                            onDismiss = { showCreateRoomDialog = false },
                            onCreateRoom = { title, desc, category, isPrivate, passcode, maxCapacity ->
                                viewModel.createAndJoinRoom(
                                    title = title,
                                    description = desc,
                                    category = category,
                                    isPrivate = isPrivate,
                                    passcode = passcode,
                                    maxParticipants = maxCapacity,
                                    onCreated = {
                                        showCreateRoomDialog = false
                                        Toast.makeText(this@MainActivity, "اتاق صوتی ایجاد شد! 🚀", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    }

                    if (showProfileDialog) {
                        ProfileDialog(
                            currentProfile = currentUser,
                            onDismiss = { showProfileDialog = false },
                            onSaveProfile = { name, bio, avatarColorHex ->
                                viewModel.updateUserProfile(name, bio, avatarColorHex)
                                Toast.makeText(this@MainActivity, "پروفایل به روز شد ✅", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
