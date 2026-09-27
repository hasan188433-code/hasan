package com.example.data.repository

import com.example.data.dao.MessageDao
import com.example.data.dao.RoomDao
import com.example.data.dao.RoomMemberDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class RoomRepository(
    private val roomDao: RoomDao,
    private val messageDao: MessageDao,
    private val roomMemberDao: RoomMemberDao,
    private val userProfileDao: UserProfileDao
) {
    val allRooms: Flow<List<RoomEntity>> = roomDao.getAllRooms()
    val currentUserProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfileFlow()

    fun getRoomFlow(roomId: String): Flow<RoomEntity?> = roomDao.getRoomByIdFlow(roomId)

    fun getRoomsByCategory(category: String): Flow<List<RoomEntity>> =
        if (category == "همه" || category == "All") roomDao.getAllRooms()
        else roomDao.getRoomsByCategory(category)

    fun searchRooms(query: String): Flow<List<RoomEntity>> = roomDao.searchRooms(query)

    suspend fun createRoom(room: RoomEntity, hostUser: UserProfileEntity): String {
        roomDao.insertRoom(room)

        // Add creator as HOST
        val hostMember = RoomMemberEntity(
            memberId = "user_${hostUser.userId}_${room.roomId}",
            roomId = room.roomId,
            name = "${hostUser.displayName} (میزبان)",
            avatarColorHex = hostUser.avatarColorHex,
            role = MemberRole.HOST,
            isMuted = false
        )
        roomMemberDao.insertMember(hostMember)

        // Add initial system message
        val sysMsg = MessageEntity(
            id = "sys_created_${System.currentTimeMillis()}",
            roomId = room.roomId,
            senderId = "system",
            senderName = "سیستم",
            senderAvatarColorHex = 0xFF6200EE,
            text = "اتاق «${room.title}» ایجاد شد! خوش آمدید. 🎉",
            isSystemEvent = true
        )
        messageDao.insertMessage(sysMsg)

        return room.roomId
    }

    suspend fun joinRoom(roomId: String, user: UserProfileEntity): Boolean {
        val room = roomDao.getRoomById(roomId) ?: return false
        val memberId = "user_${user.userId}_$roomId"

        val newMember = RoomMemberEntity(
            memberId = memberId,
            roomId = roomId,
            name = user.displayName,
            avatarColorHex = user.avatarColorHex,
            role = MemberRole.LISTENER,
            isMuted = true
        )
        roomMemberDao.insertMember(newMember)

        // System message for join
        val sysMsg = MessageEntity(
            id = "sys_join_${System.currentTimeMillis()}",
            roomId = roomId,
            senderId = "system",
            senderName = "سیستم",
            senderAvatarColorHex = 0xFF009688,
            text = "«${user.displayName}» به اتاق پیوست 👋",
            isSystemEvent = true
        )
        messageDao.insertMessage(sysMsg)

        // Update count
        roomDao.updateParticipantCount(roomId, room.participantCount + 1)
        return true
    }

    fun getMembersForRoom(roomId: String): Flow<List<RoomMemberEntity>> =
        roomMemberDao.getMembersForRoom(roomId)

    fun getMessagesForRoom(roomId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForRoom(roomId)

    suspend fun sendMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun updateMemberMute(memberId: String, isMuted: Boolean) {
        roomMemberDao.updateMuteState(memberId, isMuted)
    }

    suspend fun updateMemberSpeaking(memberId: String, isSpeaking: Boolean) {
        roomMemberDao.updateSpeakingState(memberId, isSpeaking)
    }

    suspend fun updateMemberRaisedHand(memberId: String, hasRaisedHand: Boolean) {
        roomMemberDao.updateRaisedHand(memberId, hasRaisedHand)
    }

    suspend fun updateMemberRole(memberId: String, role: MemberRole) {
        roomMemberDao.updateRole(memberId, role)
    }

    suspend fun leaveRoom(roomId: String, memberId: String, userName: String) {
        roomMemberDao.removeMemberFromRoom(roomId, memberId)
        val sysMsg = MessageEntity(
            id = "sys_leave_${System.currentTimeMillis()}",
            roomId = roomId,
            senderId = "system",
            senderName = "سیستم",
            senderAvatarColorHex = 0xFFE91E63,
            text = "«$userName» از اتاق خارج شد 🚶‍♂️",
            isSystemEvent = true
        )
        messageDao.insertMessage(sysMsg)

        val room = roomDao.getRoomById(roomId)
        if (room != null && room.participantCount > 1) {
            roomDao.updateParticipantCount(roomId, room.participantCount - 1)
        }
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun updateMessageReaction(messageId: String, emoji: String) {
        messageDao.updateMessageReaction(messageId, emoji)
    }
}
