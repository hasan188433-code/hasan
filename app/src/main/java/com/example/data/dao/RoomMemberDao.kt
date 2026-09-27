package com.example.data.dao

import androidx.room.*
import com.example.data.model.MemberRole
import com.example.data.model.RoomMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomMemberDao {
    @Query("SELECT * FROM room_members WHERE roomId = :roomId ORDER BY role ASC, joinedAt ASC")
    fun getMembersForRoom(roomId: String): Flow<List<RoomMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: RoomMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<RoomMemberEntity>)

    @Query("DELETE FROM room_members WHERE memberId = :memberId")
    suspend fun removeMember(memberId: String)

    @Query("DELETE FROM room_members WHERE roomId = :roomId AND memberId = :memberId")
    suspend fun removeMemberFromRoom(roomId: String, memberId: String)

    @Query("UPDATE room_members SET isMuted = :isMuted WHERE memberId = :memberId")
    suspend fun updateMuteState(memberId: String, isMuted: Boolean)

    @Query("UPDATE room_members SET isSpeaking = :isSpeaking WHERE memberId = :memberId")
    suspend fun updateSpeakingState(memberId: String, isSpeaking: Boolean)

    @Query("UPDATE room_members SET hasRaisedHand = :hasRaisedHand WHERE memberId = :memberId")
    suspend fun updateRaisedHand(memberId: String, hasRaisedHand: Boolean)

    @Query("UPDATE room_members SET role = :role WHERE memberId = :memberId")
    suspend fun updateRole(memberId: String, role: MemberRole)

    @Query("UPDATE room_members SET isSpeaking = 0 WHERE roomId = :roomId")
    suspend fun resetAllSpeakingInRoom(roomId: String)
}
