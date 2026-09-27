package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BusinessProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profiles LIMIT 1")
    fun getBusinessProfile(): Flow<BusinessProfileEntity?>

    @Query("SELECT * FROM business_profiles LIMIT 1")
    suspend fun getBusinessProfileSync(): BusinessProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BusinessProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: BusinessProfileEntity)

    @Query("SELECT COUNT(*) FROM business_profiles")
    suspend fun getProfileCount(): Int
}
