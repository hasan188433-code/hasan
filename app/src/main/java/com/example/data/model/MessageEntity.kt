package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarColorHex: Long,
    val text: String,
    val audioFilePath: String? = null,
    val audioDurationMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemEvent: Boolean = false,
    val reactionEmoji: String? = null
)
