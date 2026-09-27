package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MemberRole {
    HOST,
    SPEAKER,
    LISTENER
}

@Entity(tableName = "room_members")
data class RoomMemberEntity(
    @PrimaryKey val memberId: String,
    val roomId: String,
    val name: String,
    val avatarColorHex: Long,
    val role: MemberRole,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val hasRaisedHand: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)
