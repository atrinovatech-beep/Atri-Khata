package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    suspend fun getAllItemsSync(): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    fun getItemByIdFlow(id: Long): Flow<InventoryItemEntity?>

    @Query("SELECT DISTINCT category FROM inventory_items WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM inventory_items WHERE stockQuantity <= minStockAlert")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun getItemsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)
}
