package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LedgerEntry
import kotlinx.coroutines.flow.Flow

data class AccountBalanceRow(
    val account: String,
    val accountType: String,
    val totalDebit: Double,
    val totalCredit: Double,
    val balance: Double
)

@Dao
interface LedgerDao {

    @Query("SELECT * FROM ledger_entries ORDER BY postingDateMillis DESC, id DESC")
    fun getAllEntries(): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE isCancelled = 0 ORDER BY postingDateMillis DESC, id DESC")
    fun getActiveEntries(): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE isCancelled = 0 ORDER BY postingDateMillis DESC, id DESC LIMIT :limit OFFSET :offset")
    fun getLedgerHistoryPaged(limit: Int, offset: Int): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE isCancelled = 0 ORDER BY postingDateMillis DESC, id DESC LIMIT :limit")
    fun getRecentLedgerHistory(limit: Int): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE voucherType = :voucherType AND voucherNo = :voucherNo ORDER BY id ASC")
    fun getEntriesByVoucher(voucherType: String, voucherNo: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE voucherNo = :voucherNo ORDER BY id ASC")
    fun getVoucherHistory(voucherNo: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE voucherType = :voucherType AND isCancelled = 0 ORDER BY postingDateMillis DESC, id DESC")
    fun getLedgerHistoryByVoucherType(voucherType: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE voucherType = :voucherType AND voucherNo = :voucherNo ORDER BY id ASC")
    suspend fun getEntriesByVoucherSync(voucherType: String, voucherNo: String): List<LedgerEntry>

    @Query("SELECT * FROM ledger_entries WHERE account = :account AND isCancelled = 0 ORDER BY postingDateMillis ASC, id ASC")
    fun getEntriesByAccount(account: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE account = :account AND isCancelled = 0 ORDER BY postingDateMillis DESC, id DESC LIMIT :limit")
    fun getAccountLedgerHistory(account: String, limit: Int = 100): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE partyId = :partyId AND isCancelled = 0 ORDER BY postingDateMillis ASC, id ASC")
    fun getEntriesByParty(partyId: Long): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE partyId = :partyId AND isCancelled = 0 ORDER BY postingDateMillis DESC, id DESC LIMIT :limit")
    fun getPartyLedgerHistory(partyId: Long, limit: Int = 100): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE postingDateMillis BETWEEN :startDateMillis AND :endDateMillis AND isCancelled = 0 ORDER BY postingDateMillis ASC, id ASC")
    fun getEntriesByDateRange(startDateMillis: Long, endDateMillis: Long): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE postingDateMillis BETWEEN :startDateMillis AND :endDateMillis AND isCancelled = 0 ORDER BY postingDateMillis DESC, id DESC")
    fun getLedgerHistoryBetweenDates(startDateMillis: Long, endDateMillis: Long): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE account = :account AND postingDateMillis BETWEEN :startDateMillis AND :endDateMillis AND isCancelled = 0 ORDER BY postingDateMillis ASC, id ASC")
    fun getEntriesByAccountAndDateRange(account: String, startDateMillis: Long, endDateMillis: Long): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE fiscalYear = :fiscalYear AND isCancelled = 0 ORDER BY postingDateMillis ASC, id ASC")
    fun getEntriesForFiscalYear(fiscalYear: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE isCancelled = 1 ORDER BY postingDateMillis DESC, id DESC")
    fun getCancelledLedgerHistory(): Flow<List<LedgerEntry>>

    @Query("SELECT (SUM(debit) - SUM(credit)) FROM ledger_entries WHERE account = :account AND isCancelled = 0")
    fun getAccountBalance(account: String): Flow<Double?>

    @Query("SELECT (SUM(debit) - SUM(credit)) FROM ledger_entries WHERE partyId = :partyId AND isCancelled = 0")
    fun getPartyBalance(partyId: Long): Flow<Double?>

    @Query("SELECT SUM(debit) FROM ledger_entries WHERE isCancelled = 0")
    fun getTotalDebits(): Flow<Double?>

    @Query("SELECT SUM(credit) FROM ledger_entries WHERE isCancelled = 0")
    fun getTotalCredits(): Flow<Double?>

    @Query("""
        SELECT 
            account,
            accountType,
            SUM(debit) AS totalDebit,
            SUM(credit) AS totalCredit,
            (SUM(debit) - SUM(credit)) AS balance
        FROM ledger_entries
        WHERE isCancelled = 0
        GROUP BY account, accountType
        ORDER BY accountType ASC, account ASC
    """)
    fun getTrialBalance(): Flow<List<AccountBalanceRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LedgerEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LedgerEntry>): List<Long>

    @Update
    suspend fun updateEntry(entry: LedgerEntry)

    @Query("UPDATE ledger_entries SET isCancelled = 1 WHERE voucherType = :voucherType AND voucherNo = :voucherNo")
    suspend fun cancelVoucherEntries(voucherType: String, voucherNo: String)

    @Query("DELETE FROM ledger_entries WHERE voucherType = :voucherType AND voucherNo = :voucherNo")
    suspend fun deleteEntriesByVoucher(voucherType: String, voucherNo: String)

    @Query("SELECT * FROM ledger_entries ORDER BY postingDateMillis ASC, id ASC")
    suspend fun getAllEntriesSync(): List<LedgerEntry>

    @Query("DELETE FROM ledger_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT COUNT(*) FROM ledger_entries")
    suspend fun getEntriesCount(): Int
}
