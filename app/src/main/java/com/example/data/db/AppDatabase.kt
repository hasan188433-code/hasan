package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.MessageDao
import com.example.data.dao.RoomDao
import com.example.data.dao.RoomMemberDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RoomEntity::class,
        MessageEntity::class,
        RoomMemberEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun roomDao(): RoomDao
    abstract fun messageDao(): MessageDao
    abstract fun roomMemberDao(): RoomMemberDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voice_hub_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val userProfile = UserProfileEntity(
                userId = "current_user",
                displayName = "کاربر عزیز",
                bio = "علاقه‌مند به گفتگو، ویس چت و دورهمی صوتی 🎧",
                avatarColorHex = 0xFF8E24AA
            )
            db.userProfileDao().insertOrUpdateProfile(userProfile)

            val now = System.currentTimeMillis()

            val room1 = RoomEntity(
                roomId = "room_lounge_1",
                title = "☕ دورهمی صوتی دنج و گپ آزاد",
                description = "اتاقی برای صحبت درباره موضوعات روزمره، شعر، قهوه و صحبت‌های دوستانه.",
                category = "گفتگو و گپ",
                hostName = "مریم صبوری",
                hostAvatarColorHex = 0xFFE91E63,
                isPrivate = false,
                participantCount = 12,
                maxParticipants = 50,
                bannerColorHex = 0xFF673AB7,
                tags = "#دنج #گپ_آزاد #دوستانه"
            )

            val room2 = RoomEntity(
                roomId = "room_tech_2",
                title = "💻 برنامه نویسان و هوش مصنوعی",
                description = "بحث در مورد اندروید، کاتلین، فریمورک‌ها، فناوری‌های روز و AI.",
                category = "فناوری",
                hostName = "آرش مهندس",
                hostAvatarColorHex = 0xFF009688,
                isPrivate = false,
                participantCount = 18,
                maxParticipants = 100,
                bannerColorHex = 0xFF00B0FF,
                tags = "#کاتلین #برنامه_نویسی #AI"
            )

            val room3 = RoomEntity(
                roomId = "room_music_3",
                title = "🎵 موسیقی و آهنگ‌های درخواستی",
                description = "گوش دادن همزمان به موزیک‌های خاطره‌انگیز و تبادل نظر درباره ساخت قطعات موسیقی.",
                category = "موسیقی",
                hostName = "سارا دی‌جی",
                hostAvatarColorHex = 0xFFFF5722,
                isPrivate = false,
                participantCount = 24,
                maxParticipants = 80,
                bannerColorHex = 0xFFFF4081,
                tags = "#موسیقی #پاپ #سنتی #درخواستی"
            )

            val room4 = RoomEntity(
                roomId = "room_podcast_4",
                title = "🎙️ پادکست زنده: داستان‌های کتاب و تاریخ",
                description = "بررسی کتاب‌های برجسته ادبیات جهان و داستان‌های تاریخی شنیدنی.",
                category = "پادکست",
                hostName = "رضا راوی",
                hostAvatarColorHex = 0xFF3F51B5,
                isPrivate = false,
                participantCount = 35,
                maxParticipants = 200,
                bannerColorHex = 0xFF3F51B5,
                tags = "#کتاب #ادبیات #پادکست"
            )

            db.roomDao().insertRoom(room1)
            db.roomDao().insertRoom(room2)
            db.roomDao().insertRoom(room3)
            db.roomDao().insertRoom(room4)

            // Seed members for room 1
            val room1Members = listOf(
                RoomMemberEntity("m1", "room_lounge_1", "مریم صبوری (میزبان)", 0xFFE91E63, MemberRole.HOST, isMuted = false, isSpeaking = true),
                RoomMemberEntity("m2", "room_lounge_1", "امیرحسین", 0xFF2196F3, MemberRole.SPEAKER, isMuted = false, isSpeaking = false),
                RoomMemberEntity("m3", "room_lounge_1", "زهرا کاظمی", 0xFF4CAF50, MemberRole.SPEAKER, isMuted = true, isSpeaking = false),
                RoomMemberEntity("m4", "room_lounge_1", "پرهام", 0xFFFF9800, MemberRole.LISTENER, hasRaisedHand = true),
                RoomMemberEntity("m5", "room_lounge_1", "نیلوفر", 0xFF9C27B0, MemberRole.LISTENER)
            )
            db.roomMemberDao().insertMembers(room1Members)

            // Seed initial messages for room 1
            val messages1 = listOf(
                MessageEntity("msg_1", "room_lounge_1", "m1", "مریم صبوری", 0xFFE91E63, "سلام به همه دوستان خوش آمدید به اتاق صوتی ما! 🎉", timestamp = now - 600000),
                MessageEntity("msg_2", "room_lounge_1", "m2", "امیرحسین", 0xFF2196F3, "سلام مریم جان، کیفیت ویس خیلی خوبه! عالیه.", timestamp = now - 500000),
                MessageEntity("msg_3", "room_lounge_1", "sys_1", "سیستم", 0xFF000000, "پرهام دست خود را برای صحبت کردن بالا برد ✋", isSystemEvent = true, timestamp = now - 300000),
                MessageEntity("msg_4", "room_lounge_1", "m3", "زهرا کاظمی", 0xFF4CAF50, "موضوع گفتگو امشب عالیه! منم موزیک‌های لوفای رو ترجیح میدم.", timestamp = now - 100000)
            )

            messages1.forEach { db.messageDao().insertMessage(it) }

            // Seed members for room 2
            val room2Members = listOf(
                RoomMemberEntity("m6", "room_tech_2", "آرش مهندس (میزبان)", 0xFF009688, MemberRole.HOST, isMuted = false, isSpeaking = true),
                RoomMemberEntity("m7", "room_tech_2", "کامران کاتلین", 0xFF3F51B5, MemberRole.SPEAKER, isMuted = false, isSpeaking = false),
                RoomMemberEntity("m8", "room_tech_2", "مهسا AI", 0xFFE91E63, MemberRole.LISTENER)
            )
            db.roomMemberDao().insertMembers(room2Members)
            db.messageDao().insertMessage(MessageEntity("msg_tech_1", "room_tech_2", "m6", "آرش مهندس", 0xFF009688, "در حال بررسی قابلیت جدید Jetpack Compose در این نسخه هستیم! 🚀", timestamp = now - 200000))
        }
    }
}
