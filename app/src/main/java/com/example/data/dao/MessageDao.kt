package com.example.data.dao

import androidx.room.*
import com.example.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE roomId = :roomId")
    suspend fun clearRoomMessages(roomId: String)

    @Query("UPDATE messages SET reactionEmoji = :emoji WHERE id = :messageId")
    suspend fun updateMessageReaction(messageId: String, emoji: String)
}
