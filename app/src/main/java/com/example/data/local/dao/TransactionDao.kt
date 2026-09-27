package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateMillis DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE partyId = :partyId ORDER BY dateMillis DESC")
    fun getTransactionsForParty(partyId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE partyName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY dateMillis DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Sales Invoice'")
    fun getTotalSales(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Purchase'")
    fun getTotalPurchases(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense'")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Payment In'")
    fun getTotalPaymentIn(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Payment Out'")
    fun getTotalPaymentOut(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionsCount(): Int
}
