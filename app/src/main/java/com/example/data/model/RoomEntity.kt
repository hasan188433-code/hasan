package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val roomId: String,
    val title: String,
    val description: String,
    val category: String, // "گفتگو و گپ", "موسیقی", "فناوری", "پادکست", "گیمینگ"
    val hostName: String,
    val hostAvatarColorHex: Long,
    val isPrivate: Boolean = false,
    val passcode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val participantCount: Int = 1,
    val maxParticipants: Int = 50,
    val bannerColorHex: Long = 0xFF673AB7,
    val tags: String = "",
    val isLive: Boolean = true
)
