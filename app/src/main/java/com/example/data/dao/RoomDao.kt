package com.example.data.dao

import androidx.room.*
import com.example.data.model.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY createdAt DESC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE roomId = :roomId")
    fun getRoomByIdFlow(roomId: String): Flow<RoomEntity?>

    @Query("SELECT * FROM rooms WHERE roomId = :roomId")
    suspend fun getRoomById(roomId: String): RoomEntity?

    @Query("SELECT * FROM rooms WHERE category = :category ORDER BY createdAt DESC")
    fun getRoomsByCategory(category: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRooms(query: String): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE roomId = :roomId")
    suspend fun deleteRoom(roomId: String)

    @Query("UPDATE rooms SET participantCount = :count WHERE roomId = :roomId")
    suspend fun updateParticipantCount(roomId: String, count: Int)
}
