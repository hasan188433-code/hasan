package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String = "current_user",
    val displayName: String = "کاربر عزیز",
    val bio: String = "علاقه‌مند به گپ صوتی و موسیقی 🎧",
    val avatarColorHex: Long = 0xFF9C27B0,
    val preferredLanguage: String = "fa"
)
