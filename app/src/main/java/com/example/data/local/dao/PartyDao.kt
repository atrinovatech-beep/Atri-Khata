package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties ORDER BY name ASC")
    suspend fun getAllPartiesSync(): List<PartyEntity>

    @Query("SELECT * FROM parties WHERE id = :id")
    suspend fun getPartyById(id: Long): PartyEntity?

    @Query("SELECT * FROM parties WHERE type = :type ORDER BY name ASC")
    fun getPartiesByType(type: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchParties(query: String): Flow<List<PartyEntity>>

    @Query("SELECT SUM(balanceToReceive) FROM parties")
    fun getTotalToReceive(): Flow<Double?>

    @Query("SELECT SUM(balanceToGive) FROM parties")
    fun getTotalToGive(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parties: List<PartyEntity>)

    @Update
    suspend fun updateParty(party: PartyEntity)

    @Delete
    suspend fun deleteParty(party: PartyEntity)

    @Query("SELECT COUNT(*) FROM parties")
    suspend fun getPartiesCount(): Int
}
