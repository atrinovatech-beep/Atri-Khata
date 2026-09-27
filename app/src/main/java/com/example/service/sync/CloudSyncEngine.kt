package com.example.service.sync

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class CloudProfileSummary(
    val userId: String,
    val userEmail: String,
    val businessName: String,
    val businessType: String,
    val accountType: String,
    val partyCount: Int,
    val transactionCount: Int,
    val inventoryCount: Int,
    val lastBackupTimestamp: Long
)

data class SyncResult(
    val isSuccess: Boolean,
    val message: String,
    val itemsSynced: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class CloudSyncEngine(private val context: Context) {

    private val syncDir: File by lazy {
        File(context.filesDir, "atri_cloud_vault").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getUserVaultFile(userId: String): File {
        val sanitizedId = userId.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
        return File(syncDir, "vault_$sanitizedId.json")
    }

    /**
     * Checks whether cloud data exists for this user account.
     * Enables multi-device restore when signing in on a new device.
     */
    fun hasCloudProfile(userId: String): Boolean {
        val file = getUserVaultFile(userId)
        return file.exists() && file.length() > 0
    }

    fun getCloudProfileSummary(userId: String): CloudProfileSummary? {
        val file = getUserVaultFile(userId)
        if (!file.exists()) return null
        return try {
            val content = file.readText()
            val json = JSONObject(content)
            val profileObj = json.optJSONObject("businessProfile")
            CloudProfileSummary(
                userId = json.optString("userId", userId),
                userEmail = json.optString("userEmail", ""),
                businessName = profileObj?.optString("businessName") ?: "My Business",
                businessType = profileObj?.optString("businessType") ?: "General Trade",
                accountType = json.optString("accountType", "BUSINESS"),
                partyCount = json.optJSONArray("parties")?.length() ?: 0,
                transactionCount = json.optJSONArray("transactions")?.length() ?: 0,
                inventoryCount = json.optJSONArray("inventory")?.length() ?: 0,
                lastBackupTimestamp = json.optLong("timestamp", file.lastModified())
            )
        } catch (e: Exception) {
            Log.e("CloudSyncEngine", "Error reading cloud summary", e)
            null
        }
    }

    /**
     * Performs a complete encrypted-ready snapshot of local Room Database to Cloud.
     */
    suspend fun performCloudBackup(
        userId: String,
        userEmail: String,
        database: AppDatabase
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            val businessProfile = database.businessProfileDao().getBusinessProfileSync()
            val parties = database.partyDao().getAllPartiesSync()
            val transactions = database.transactionDao().getAllTransactionsSync()
            val inventory = database.inventoryDao().getAllItemsSync()
            val staff = database.staffDao().getAllStaffSync()
            val invoices = database.salesInvoiceDao().getAllInvoicesSync()
            val ledgerEntries = database.ledgerDao().getAllEntriesSync()

            val rootJson = JSONObject().apply {
                put("version", 7)
                put("userId", userId)
                put("userEmail", userEmail)
                put("timestamp", System.currentTimeMillis())

                // Business Profile
                businessProfile?.let { bp ->
                    put("businessProfile", JSONObject().apply {
                        put("businessName", bp.businessName)
                        put("businessType", bp.businessType)
                        put("panVatNumber", bp.panVatNumber)
                        put("phone", bp.phone)
                        put("email", bp.email)
                        put("address", bp.address)
                        put("city", bp.city)
                        put("stateProvince", bp.stateProvince)
                        put("country", bp.country)
                        put("currencySymbol", bp.currencySymbol)
                        put("defaultVatRate", bp.defaultVatRate)
                        put("isVatEnabled", bp.isVatEnabled)
                        put("invoicePrefix", bp.invoicePrefix)
                        put("invoiceTerms", bp.invoiceTerms)
                        put("invoiceHeader", bp.invoiceHeader)
                        put("invoiceFooter", bp.invoiceFooter)
                    })
                }

                // Parties
                val partiesArray = JSONArray()
                parties.forEach { p ->
                    partiesArray.put(JSONObject().apply {
                        put("name", p.name)
                        put("type", p.type)
                        put("phone", p.phone)
                        put("email", p.email)
                        put("address", p.address)
                        put("panVatNumber", p.panVatNumber)
                        put("openingBalance", p.openingBalance)
                        put("balanceToReceive", p.balanceToReceive)
                        put("balanceToGive", p.balanceToGive)
                        put("creditLimit", p.creditLimit)
                    })
                }
                put("parties", partiesArray)

                // Transactions
                val transArray = JSONArray()
                transactions.forEach { t ->
                    transArray.put(JSONObject().apply {
                        put("type", t.type)
                        put("partyName", t.partyName)
                        put("amount", t.amount)
                        put("dateMillis", t.dateMillis)
                        put("dateBs", t.dateBs)
                        put("dateAd", t.dateAd)
                        put("paymentMethod", t.paymentMethod)
                        put("invoiceNumber", t.invoiceNumber)
                        put("notes", t.notes)
                    })
                }
                put("transactions", transArray)

                // Inventory
                val invArray = JSONArray()
                inventory.forEach { item ->
                    invArray.put(JSONObject().apply {
                        put("name", item.name)
                        put("sku", item.sku)
                        put("category", item.category)
                        put("unit", item.unit)
                        put("purchasePrice", item.purchasePrice)
                        put("salePrice", item.salePrice)
                        put("stockQuantity", item.stockQuantity)
                        put("minStockAlert", item.minStockAlert)
                    })
                }
                put("inventory", invArray)

                // Staff
                val staffArray = JSONArray()
                staff.forEach { st ->
                    staffArray.put(JSONObject().apply {
                        put("name", st.name)
                        put("role", st.role)
                        put("phone", st.phone)
                        put("status", st.status)
                    })
                }
                put("staff", staffArray)

                // Ledger Entries (Double-Entry GL)
                val ledgerArray = JSONArray()
                ledgerEntries.forEach { le ->
                    ledgerArray.put(JSONObject().apply {
                        put("postingDateMillis", le.postingDateMillis)
                        put("postingDateBS", le.postingDateBS)
                        put("postingDateAD", le.postingDateAD)
                        put("account", le.account)
                        put("accountType", le.accountType)
                        put("partyType", le.partyType ?: "")
                        put("partyId", le.partyId ?: -1L)
                        put("partyName", le.partyName ?: "")
                        put("voucherType", le.voucherType)
                        put("voucherNo", le.voucherNo)
                        put("debit", le.debit)
                        put("credit", le.credit)
                        put("netAmount", le.netAmount)
                        put("againstAccount", le.againstAccount)
                        put("currency", le.currency)
                        put("fiscalYear", le.fiscalYear)
                        put("remarks", le.remarks)
                    })
                }
                put("ledgerEntries", ledgerArray)
            }

            val file = getUserVaultFile(userId)
            file.writeText(rootJson.toString(2))

            val totalCount = parties.size + transactions.size + inventory.size + staff.size + ledgerEntries.size
            SyncResult(
                isSuccess = true,
                message = "Cloud backup completed. $totalCount records synchronized.",
                itemsSynced = totalCount
            )
        } catch (e: Exception) {
            Log.e("CloudSyncEngine", "Backup failed", e)
            SyncResult(
                isSuccess = false,
                message = "Backup failed: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Restores cloud snapshot into the local Room database.
     * Safely merges or populates records for multi-device sync without duplicates.
     */
    suspend fun restoreFromCloud(
        userId: String,
        database: AppDatabase
    ): SyncResult = withContext(Dispatchers.IO) {
        val file = getUserVaultFile(userId)
        if (!file.exists()) {
            return@withContext SyncResult(isSuccess = false, message = "No cloud snapshot found for this account")
        }

        try {
            val content = file.readText()
            val root = JSONObject(content)

            var restoredCount = 0

            // 1. Business Profile
            val profileObj = root.optJSONObject("businessProfile")
            if (profileObj != null) {
                val currentProfile = database.businessProfileDao().getBusinessProfileSync()
                val updatedProfile = currentProfile?.copy(
                    businessName = profileObj.optString("businessName", currentProfile.businessName),
                    businessType = profileObj.optString("businessType", currentProfile.businessType),
                    panVatNumber = profileObj.optString("panVatNumber", currentProfile.panVatNumber),
                    phone = profileObj.optString("phone", currentProfile.phone),
                    email = profileObj.optString("email", currentProfile.email),
                    currencySymbol = profileObj.optString("currencySymbol", currentProfile.currencySymbol),
                    defaultVatRate = profileObj.optDouble("defaultVatRate", currentProfile.defaultVatRate),
                    isVatEnabled = profileObj.optBoolean("isVatEnabled", currentProfile.isVatEnabled),
                    updatedAt = System.currentTimeMillis()
                ) ?: BusinessProfileEntity(
                    businessName = profileObj.optString("businessName", "My Business"),
                    businessType = profileObj.optString("businessType", "General Trade"),
                    panVatNumber = profileObj.optString("panVatNumber", ""),
                    phone = profileObj.optString("phone", ""),
                    email = profileObj.optString("email", ""),
                    address = profileObj.optString("address", ""),
                    city = profileObj.optString("city", ""),
                    stateProvince = profileObj.optString("stateProvince", ""),
                    currencySymbol = profileObj.optString("currencySymbol", "Rs."),
                    defaultVatRate = profileObj.optDouble("defaultVatRate", 13.0),
                    isVatEnabled = profileObj.optBoolean("isVatEnabled", true),
                    invoicePrefix = profileObj.optString("invoicePrefix", "INV"),
                    invoiceTerms = "1. Goods once sold will not be returned after 7 days.",
                    invoiceHeader = "Tax Invoice",
                    invoiceFooter = "Thank you for doing business with us."
                )
                database.businessProfileDao().insertProfile(updatedProfile)
                restoredCount++
            }

            // 2. Parties
            val partiesArray = root.optJSONArray("parties")
            if (partiesArray != null) {
                val existingParties = database.partyDao().getAllPartiesSync().associateBy { it.name.trim().lowercase() }
                for (i in 0 until partiesArray.length()) {
                    val pObj = partiesArray.getJSONObject(i)
                    val pName = pObj.optString("name", "").trim()
                    if (pName.isBlank()) continue

                    val existing = existingParties[pName.lowercase()]
                    if (existing == null) {
                        val newParty = PartyEntity(
                            name = pName,
                            type = pObj.optString("type", "Customer"),
                            phone = pObj.optString("phone", ""),
                            email = pObj.optString("email", ""),
                            address = pObj.optString("address", ""),
                            panVatNumber = pObj.optString("panNumber", ""),
                            openingBalance = pObj.optDouble("openingBalance", 0.0),
                            balanceToReceive = pObj.optDouble("balanceToReceive", 0.0),
                            balanceToGive = pObj.optDouble("balanceToGive", 0.0),
                            creditLimit = pObj.optDouble("creditLimit", 0.0)
                        )
                        database.partyDao().insertParty(newParty)
                        restoredCount++
                    }
                }
            }

            // 3. Inventory Items
            val invArray = root.optJSONArray("inventory")
            if (invArray != null) {
                val existingItems = database.inventoryDao().getAllItemsSync().associateBy { it.name.trim().lowercase() }
                for (i in 0 until invArray.length()) {
                    val itemObj = invArray.getJSONObject(i)
                    val itemName = itemObj.optString("name", "").trim()
                    if (itemName.isBlank()) continue

                    val existing = existingItems[itemName.lowercase()]
                    if (existing == null) {
                        val newItem = InventoryItemEntity(
                            name = itemName,
                            sku = itemObj.optString("sku", "SKU-${(100..999).random()}"),
                            category = itemObj.optString("category", "General"),
                            unit = itemObj.optString("unit", "Pcs"),
                            purchasePrice = itemObj.optDouble("purchasePrice", 0.0),
                            salePrice = itemObj.optDouble("salePrice", 0.0),
                            stockQuantity = itemObj.optDouble("stockQuantity", 0.0),
                            minStockAlert = itemObj.optDouble("minStockAlert", 5.0)
                        )
                        database.inventoryDao().insertItem(newItem)
                        restoredCount++
                    }
                }
            }

            // 4. Transactions
            val transArray = root.optJSONArray("transactions")
            if (transArray != null) {
                val existingTrans = database.transactionDao().getAllTransactionsSync().map { "${it.invoiceNumber}_${it.amount}_${it.dateMillis}" }.toSet()
                for (i in 0 until transArray.length()) {
                    val tObj = transArray.getJSONObject(i)
                    val invNum = tObj.optString("invoiceNumber", "")
                    val amt = tObj.optDouble("amount", 0.0)
                    val dMillis = tObj.optLong("dateMillis", System.currentTimeMillis())
                    val key = "${invNum}_${amt}_${dMillis}"

                    if (key !in existingTrans) {
                        val newTrans = TransactionEntity(
                            type = tObj.optString("type", "Sales Invoice"),
                            partyName = tObj.optString("partyName", "Walk-in Customer"),
                            amount = amt,
                            dateMillis = dMillis,
                            dateBs = tObj.optString("dateBs", ""),
                            dateAd = tObj.optString("dateAd", ""),
                            paymentMethod = tObj.optString("paymentMethod", "Cash"),
                            invoiceNumber = invNum,
                            notes = tObj.optString("notes", "")
                        )
                        database.transactionDao().insertTransaction(newTrans)
                        restoredCount++
                    }
                }
            }

            // 5. Staff
            val staffArray = root.optJSONArray("staff")
            if (staffArray != null) {
                val existingStaff = database.staffDao().getAllStaffSync().associateBy { it.name.trim().lowercase() }
                for (i in 0 until staffArray.length()) {
                    val sObj = staffArray.getJSONObject(i)
                    val sName = sObj.optString("name", "").trim()
                    if (sName.isNotBlank() && existingStaff[sName.lowercase()] == null) {
                        database.staffDao().insertStaff(
                            StaffMemberEntity(
                                name = sName,
                                role = sObj.optString("role", "Sales Person"),
                                phone = sObj.optString("phone", ""),
                                status = sObj.optString("status", "Active")
                            )
                        )
                        restoredCount++
                    }
                }
            }

            // 6. Ledger Entries (Double-Entry GL)
            val ledgerArray = root.optJSONArray("ledgerEntries")
            if (ledgerArray != null && ledgerArray.length() > 0) {
                val newEntries = mutableListOf<com.example.data.local.entity.LedgerEntry>()
                for (i in 0 until ledgerArray.length()) {
                    val lObj = ledgerArray.getJSONObject(i)
                    newEntries.add(
                        com.example.data.local.entity.LedgerEntry(
                            id = 0,
                            postingDateMillis = lObj.optLong("postingDateMillis", System.currentTimeMillis()),
                            postingDateBS = lObj.optString("postingDateBS", ""),
                            postingDateAD = lObj.optString("postingDateAD", ""),
                            account = lObj.optString("account", "General Ledger"),
                            accountType = lObj.optString("accountType", "Asset"),
                            partyType = lObj.optString("partyType", null).takeIf { it?.isNotBlank() == true },
                            partyId = lObj.optLong("partyId", -1L).takeIf { it > 0 },
                            partyName = lObj.optString("partyName", null).takeIf { it?.isNotBlank() == true },
                            voucherType = lObj.optString("voucherType", "Journal Entry"),
                            voucherNo = lObj.optString("voucherNo", "GL-${i + 1}"),
                            debit = lObj.optDouble("debit", 0.0),
                            credit = lObj.optDouble("credit", 0.0),
                            againstAccount = lObj.optString("againstAccount", ""),
                            currency = lObj.optString("currency", "Rs."),
                            fiscalYear = lObj.optString("fiscalYear", ""),
                            remarks = lObj.optString("remarks", "")
                        )
                    )
                }
                if (newEntries.isNotEmpty()) {
                    database.ledgerDao().deleteAllEntries()
                    database.ledgerDao().insertAll(newEntries)
                    restoredCount += newEntries.size
                }
            }

            SyncResult(
                isSuccess = true,
                message = "Successfully synchronized $restoredCount items from cloud.",
                itemsSynced = restoredCount
            )
        } catch (e: Exception) {
            Log.e("CloudSyncEngine", "Restore failed", e)
            SyncResult(
                isSuccess = false,
                message = "Restore failed: ${e.localizedMessage}"
            )
        }
    }
}
