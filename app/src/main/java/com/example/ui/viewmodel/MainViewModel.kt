package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceAudioManager
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.RoomRepository
import com.example.ui.components.FloatingReaction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = RoomRepository(
        roomDao = db.roomDao(),
        messageDao = db.messageDao(),
        roomMemberDao = db.roomMemberDao(),
        userProfileDao = db.userProfileDao()
    )

    val voiceAudioManager = VoiceAudioManager(application)

    // Category & Search state
    private val _selectedCategory = MutableStateFlow("همه")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered rooms flow
    val rooms: StateFlow<List<RoomEntity>> = combine(
        _selectedCategory,
        _searchQuery,
        repository.allRooms
    ) { category, query, allRooms ->
        allRooms.filter { room ->
            val categoryMatches = (category == "همه" || category == "All" || room.category == category)
            val queryMatches = query.isBlank() ||
                    room.title.contains(query, ignoreCase = true) ||
                    room.description.contains(query, ignoreCase = true) ||
                    room.tags.contains(query, ignoreCase = true)
            categoryMatches && queryMatches
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current User Profile
    val currentUserProfile: StateFlow<UserProfileEntity> = repository.currentUserProfile
        .map { it ?: UserProfileEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserProfileEntity()
        )

    // Joined Room State
    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRoom: StateFlow<RoomEntity?> = _activeRoomId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getRoomFlow(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMembers: StateFlow<List<RoomMemberEntity>> = _activeRoomId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getMembersForRoom(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<MessageEntity>> = _activeRoomId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getMessagesForRoom(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Mic & Controls state
    private val _isMicMuted = MutableStateFlow(true)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _hasRaisedHand = MutableStateFlow(false)
    val hasRaisedHand: StateFlow<Boolean> = _hasRaisedHand.asStateFlow()

    // Floating Reactions
    private val _floatingReactions = MutableStateFlow<List<FloatingReaction>>(emptyList())
    val floatingReactions: StateFlow<List<FloatingReaction>> = _floatingReactions.asStateFlow()

    // Active Room Voice Engine Loop
    private var roomVoiceSimulationJob: Job? = null

    init {
        // Collect mic amplitude from manager when unmuted
        viewModelScope.launch {
            voiceAudioManager.micAmplitude.collect { amplitude ->
                if (!_isMicMuted.value && _activeRoomId.value != null) {
                    val userMemberId = "user_${currentUserProfile.value.userId}_${_activeRoomId.value}"
                    val isSpeakingNow = amplitude > 0.08f
                    repository.updateMemberSpeaking(userMemberId, isSpeakingNow)
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun joinRoom(roomId: String, passcodeAttempt: String? = null, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val user = currentUserProfile.value
            val success = repository.joinRoom(roomId, user)
            if (success) {
                _activeRoomId.value = roomId
                _isMicMuted.value = true
                _hasRaisedHand.value = false
                startRoomSimulation(roomId)
                onSuccess()
            } else {
                onError("خطا در ورود به اتاق")
            }
        }
    }

    fun createAndJoinRoom(
        title: String,
        description: String,
        category: String,
        isPrivate: Boolean,
        passcode: String?,
        maxParticipants: Int,
        onCreated: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUserProfile.value
            val roomId = "room_custom_${System.currentTimeMillis()}"

            val newRoom = RoomEntity(
                roomId = roomId,
                title = title,
                description = description,
                category = category,
                hostName = user.displayName,
                hostAvatarColorHex = user.avatarColorHex,
                isPrivate = isPrivate,
                passcode = passcode,
                participantCount = 1,
                maxParticipants = maxParticipants,
                bannerColorHex = 0xFF7C4DFF,
                tags = "#$category #جدید"
            )

            repository.createRoom(newRoom, user)
            _activeRoomId.value = roomId
            _isMicMuted.value = false // Host starts unmuted
            voiceAudioManager.startMicMonitoring()

            startRoomSimulation(roomId)
            onCreated()
        }
    }

    fun toggleMicMute() {
        val newMute = !_isMicMuted.value
        _isMicMuted.value = newMute

        val roomId = _activeRoomId.value ?: return
        val userMemberId = "user_${currentUserProfile.value.userId}_$roomId"

        viewModelScope.launch {
            repository.updateMemberMute(userMemberId, newMute)
            if (newMute) {
                repository.updateMemberSpeaking(userMemberId, false)
                voiceAudioManager.stopMicMonitoring()
            } else {
                voiceAudioManager.startMicMonitoring()
            }
        }
    }

    fun toggleRaiseHand() {
        val newRaised = !_hasRaisedHand.value
        _hasRaisedHand.value = newRaised

        val roomId = _activeRoomId.value ?: return
        val user = currentUserProfile.value
        val userMemberId = "user_${user.userId}_$roomId"

        viewModelScope.launch {
            repository.updateMemberRaisedHand(userMemberId, newRaised)

            val text = if (newRaised) "«${user.displayName}» دست خود را بالا برد ✋" else "«${user.displayName}» دست خود را پایین آورد 🖐️"
            val sysMsg = MessageEntity(
                id = "sys_hand_${System.currentTimeMillis()}",
                roomId = roomId,
                senderId = "system",
                senderName = "سیستم",
                senderAvatarColorHex = 0xFFFF9800,
                text = text,
                isSystemEvent = true
            )
            repository.sendMessage(sysMsg)
        }
    }

    fun sendTextMessage(text: String) {
        val roomId = _activeRoomId.value ?: return
        if (text.isBlank()) return

        val user = currentUserProfile.value
        viewModelScope.launch {
            val msg = MessageEntity(
                id = "msg_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                roomId = roomId,
                senderId = user.userId,
                senderName = user.displayName,
                senderAvatarColorHex = user.avatarColorHex,
                text = text.trim()
            )
            repository.sendMessage(msg)
        }
    }

    fun sendReaction(emoji: String) {
        val newReaction = FloatingReaction(emoji = emoji)
        _floatingReactions.value = _floatingReactions.value + newReaction

        // Clean old floating reactions
        viewModelScope.launch {
            delay(2500)
            _floatingReactions.value = _floatingReactions.value.filter { it.id != newReaction.id }
        }
    }

    fun startRecordingVoiceNote(): File? {
        return voiceAudioManager.startVoiceNoteRecording()
    }

    fun stopAndSendVoiceNote() {
        val result = voiceAudioManager.stopVoiceNoteRecording() ?: return
        val (file, durationMs) = result
        val roomId = _activeRoomId.value ?: return

        val user = currentUserProfile.value
        viewModelScope.launch {
            val msg = MessageEntity(
                id = "msg_voice_${System.currentTimeMillis()}",
                roomId = roomId,
                senderId = user.userId,
                senderName = user.displayName,
                senderAvatarColorHex = user.avatarColorHex,
                text = "[پیام صوتی]",
                audioFilePath = file.absolutePath,
                audioDurationMs = durationMs
            )
            repository.sendMessage(msg)
        }
    }

    fun leaveCurrentRoom() {
        val roomId = _activeRoomId.value ?: return
        val user = currentUserProfile.value
        val userMemberId = "user_${user.userId}_$roomId"

        viewModelScope.launch {
            repository.leaveRoom(roomId, userMemberId, user.displayName)
            _activeRoomId.value = null
            _isMicMuted.value = true
            _hasRaisedHand.value = false
            voiceAudioManager.stopMicMonitoring()
            voiceAudioManager.stopVoiceNotePlayback()
            stopRoomSimulation()
        }
    }

    fun updateUserProfile(name: String, bio: String, avatarColorHex: Long) {
        viewModelScope.launch {
            val updated = UserProfileEntity(
                userId = "current_user",
                displayName = name,
                bio = bio,
                avatarColorHex = avatarColorHex
            )
            repository.updateProfile(updated)
        }
    }

    /**
     * Start active audio lounge simulation so members randomly speak, send chat messages, and react
     */
    private fun startRoomSimulation(roomId: String) {
        stopRoomSimulation()

        val samplePersianPhrases = listOf(
            "چه صدای شفافی داری! بسیار عالی 🎙️",
            "موافق هستم با نظرتون، صحبت کاملا به‌جایی بود 👍",
            "دوستان اگه سوالی دارین حتما دستتونو بالا ببرین ✨",
            "به به چه آهنگ باحالی داری پخش میکنی 🎶",
            "خوشحالم تو این روم حضور دارم 😊",
            "این موضوع واقعا جذابه، ممنون از میزبان بابت ساخت این روم ❤️"
        )

        val sampleReactions = listOf("❤️", "👏", "🔥", "😂", "🎤", "⚡")

        roomVoiceSimulationJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive && _activeRoomId.value == roomId) {
                delay(3500)
                val members = repository.getMembersForRoom(roomId).firstOrNull() ?: emptyList()
                val otherMembers = members.filter { !it.name.contains("کاربر") && !it.isMuted }

                if (otherMembers.isNotEmpty()) {
                    val speaker = otherMembers.random()

                    // Make speaker speak
                    repository.updateMemberSpeaking(speaker.memberId, true)
                    delay(2500)
                    repository.updateMemberSpeaking(speaker.memberId, false)

                    // Occasionally post a message or reaction
                    if (Random.nextFloat() < 0.4f) {
                        val text = samplePersianPhrases.random()
                        val msg = MessageEntity(
                            id = "sim_msg_${System.currentTimeMillis()}",
                            roomId = roomId,
                            senderId = speaker.memberId,
                            senderName = speaker.name,
                            senderAvatarColorHex = speaker.avatarColorHex,
                            text = text
                        )
                        repository.sendMessage(msg)
                    }

                    if (Random.nextFloat() < 0.5f) {
                        sendReaction(sampleReactions.random())
                    }
                }
            }
        }
    }

    private fun stopRoomSimulation() {
        roomVoiceSimulationJob?.cancel()
        roomVoiceSimulationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceAudioManager.release()
        stopRoomSimulation()
    }
}
