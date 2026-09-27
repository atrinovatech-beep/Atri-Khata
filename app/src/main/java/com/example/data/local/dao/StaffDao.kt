package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StaffMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members ORDER BY id ASC")
    fun getAllStaff(): Flow<List<StaffMemberEntity>>

    @Query("SELECT * FROM staff_members ORDER BY id ASC")
    suspend fun getAllStaffSync(): List<StaffMemberEntity>

    @Query("SELECT * FROM staff_members WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: Long): StaffMemberEntity?

    @Query("SELECT COUNT(*) FROM staff_members")
    suspend fun getStaffCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffMemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(staffList: List<StaffMemberEntity>)

    @Update
    suspend fun updateStaff(staff: StaffMemberEntity)

    @Delete
    suspend fun deleteStaff(staff: StaffMemberEntity)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: Long)
}
