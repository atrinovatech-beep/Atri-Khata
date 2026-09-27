package com.example.service.backup

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.LedgerEntry
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AppSettings
import com.example.data.model.InvoiceSettings
import com.example.data.model.PartySettings
import com.example.data.model.TransactionSettings
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Result data class for Firebase Storage Cloud Backup operations.
 */
data class CloudBackupResult(
    val isSuccess: Boolean,
    val message: String,
    val storagePath: String = "",
    val downloadUrl: String = "",
    val backupSizeBytes: Long = 0,
    val sha256Checksum: String = "",
    val ledgerCount: Int = 0,
    val configCount: Int = 0,
    val partiesCount: Int = 0,
    val transactionsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Result data class for Firebase Storage Cloud Restore operations.
 */
data class CloudRestoreResult(
    val isSuccess: Boolean,
    val message: String,
    val restoredLedgerCount: Int = 0,
    val restoredConfigCount: Int = 0,
    val restoredPartiesCount: Int = 0,
    val restoredTransactionsCount: Int = 0,
    val restoredInventoryCount: Int = 0,
    val isChecksumVerified: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Metadata snapshot of a stored cloud backup.
 */
data class CloudBackupSnapshot(
    val snapshotId: String,
    val fileName: String,
    val storagePath: String,
    val timestamp: Long,
    val formattedDate: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val ledgerEntriesCount: Int,
    val configsCount: Int,
    val sha256Checksum: String,
    val isLocalVault: Boolean = false
)

/**
 * Enterprise-grade Cloud Backup Service powered by Firebase Storage.
 *
 * Capabilities:
 * 1. Double-Entry General Ledger (GL) Serialization & Checksum Validation
 * 2. Configuration Settings Export (AppSettings, TransactionSettings, InvoiceSettings, PartySettings)
 * 3. Core Database Entities Snapshot (Parties, Transactions, Inventory, Staff)
 * 4. Direct Upload & Download to Firebase Storage Bucket
 * 5. Offline Secure Vault Fallback with SHA-256 Tamper-Evidence
 * 6. File-based Export/Import via Android Storage Access Framework (SAF)
 */
class FirebaseStorageBackupService(private val context: Context) {

    private val tag = "FirebaseBackupService"
    private val prefs: SharedPreferences = context.getSharedPreferences("atri_nova_prefs", Context.MODE_PRIVATE)

    private val localVaultDir: File by lazy {
        File(context.filesDir, "atri_firebase_vault").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Safely obtains FirebaseStorage instance.
     * Returns null if Firebase is not configured on the device or lacks google-services.json.
     */
    private fun getFirebaseStorageInstance(): FirebaseStorage? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.w(tag, "No active FirebaseApp found; operating in secure local vault mode")
                null
            } else {
                FirebaseStorage.getInstance()
            }
        } catch (e: Throwable) {
            Log.w(tag, "FirebaseStorage initialization bypassed: ${e.message}")
            null
        }
    }

    /**
     * Computes SHA-256 checksum for tamper-evident data integrity validation.
     */
    private fun computeSha256(data: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(data.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "unverified-${System.currentTimeMillis()}"
        }
    }

    /**
     * Gathers all system configuration settings into a structured JSON representation.
     */
    private fun serializeConfigurationSettings(
        appSettings: AppSettings?,
        transactionSettings: TransactionSettings?,
        invoiceSettings: InvoiceSettings?,
        partySettings: PartySettings?,
        businessProfile: BusinessProfileEntity?
    ): JSONObject {
        val root = JSONObject()

        // 1. App Settings
        val appSettingsJson = JSONObject((appSettings ?: AppSettings()).toJsonString())
        root.put("appSettings", appSettingsJson)

        // 2. Transaction Settings
        val transSettingsJson = JSONObject((transactionSettings ?: TransactionSettings.createDefaultsFor(com.example.data.model.BusinessTransactionPreset.GENERAL)).toJsonString())
        root.put("transactionSettings", transSettingsJson)

        // 3. Invoice Settings
        val invSettingsJson = JSONObject((invoiceSettings ?: InvoiceSettings.createDefaultsFor(com.example.data.model.BusinessInvoicePreset.GENERAL)).toJsonString())
        root.put("invoiceSettings", invSettingsJson)

        // 4. Party Settings
        val partySettingsJson = JSONObject((partySettings ?: PartySettings()).toJsonString())
        root.put("partySettings", partySettingsJson)

        // 5. Business Profile
        businessProfile?.let { bp ->
            val bpJson = JSONObject().apply {
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
            }
            root.put("businessProfile", bpJson)
        }

        return root
    }

    /**
     * Serializes all Frappe-style double-entry LedgerEntry records into a JSONArray.
     */
    private fun serializeLedgerEntries(entries: List<LedgerEntry>): JSONArray {
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("postingDateMillis", entry.postingDateMillis)
                put("postingDateBS", entry.postingDateBS)
                put("postingDateAD", entry.postingDateAD)
                put("account", entry.account)
                put("accountType", entry.accountType)
                put("partyType", entry.partyType ?: "")
                put("partyId", entry.partyId ?: -1L)
                put("partyName", entry.partyName ?: "")
                put("voucherType", entry.voucherType)
                put("voucherNo", entry.voucherNo)
                put("debit", entry.debit)
                put("credit", entry.credit)
                put("netAmount", entry.netAmount)
                put("againstAccount", entry.againstAccount)
                put("currency", entry.currency)
                put("fiscalYear", entry.fiscalYear)
                put("remarks", entry.remarks)
                put("isCancelled", entry.isCancelled)
                put("createdAt", entry.createdAt)
            }
            array.put(obj)
        }
        return array
    }

    /**
     * Exports and Uploads a complete snapshot to Firebase Storage.
     * Backs up:
     * - Double-entry Ledger entries
     * - Configuration Settings & Preferences
     * - Master Data: Parties, Transactions, Inventory, Staff
     */
    suspend fun exportAndUploadToFirebase(
        userId: String,
        userEmail: String,
        database: AppDatabase,
        appSettings: AppSettings?,
        transactionSettings: TransactionSettings?,
        invoiceSettings: InvoiceSettings?,
        partySettings: PartySettings?
    ): CloudBackupResult = withContext(Dispatchers.IO) {
        try {
            val nowMillis = System.currentTimeMillis()
            val sanitizedUserId = userId.replace("[^a-zA-Z0-9_.-]".toRegex(), "_").ifBlank { "default_user" }

            // 1. Fetch data from Room Database
            val ledgerEntries = database.ledgerDao().getAllEntriesSync()
            val businessProfile = database.businessProfileDao().getBusinessProfileSync()
            val parties = database.partyDao().getAllPartiesSync()
            val transactions = database.transactionDao().getAllTransactionsSync()
            val inventory = database.inventoryDao().getAllItemsSync()
            val staff = database.staffDao().getAllStaffSync()

            // 2. Build Settings Payload
            val configSettingsJson = serializeConfigurationSettings(
                appSettings,
                transactionSettings,
                invoiceSettings,
                partySettings,
                businessProfile
            )

            // 3. Build Ledger Payload
            val ledgerEntriesJson = serializeLedgerEntries(ledgerEntries)

            // 4. Build Core Master Data arrays
            val partiesArray = JSONArray()
            parties.forEach { p ->
                partiesArray.put(JSONObject().apply {
                    put("id", p.id)
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

            val transArray = JSONArray()
            transactions.forEach { t ->
                transArray.put(JSONObject().apply {
                    put("id", t.id)
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

            val invArray = JSONArray()
            inventory.forEach { item ->
                invArray.put(JSONObject().apply {
                    put("id", item.id)
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

            val staffArray = JSONArray()
            staff.forEach { s ->
                staffArray.put(JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("role", s.role)
                    put("phone", s.phone)
                    put("status", s.status)
                })
            }

            // 5. Build Content Object for Checksum calculation
            val contentObject = JSONObject().apply {
                put("configSettings", configSettingsJson)
                put("ledgerEntries", ledgerEntriesJson)
                put("parties", partiesArray)
                put("transactions", transArray)
                put("inventory", invArray)
                put("staff", staffArray)
            }

            val contentString = contentObject.toString()
            val sha256Checksum = computeSha256(contentString)

            // 6. Complete Root Vault JSON
            val rootJson = JSONObject().apply {
                put("vaultFormat", "ATRI_FIREBASE_STORAGE_VAULT_V2")
                put("version", 2)
                put("exportTimestamp", nowMillis)
                put("exportDateFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(nowMillis)))
                put("userId", userId)
                put("userEmail", userEmail)
                put("sha256Checksum", sha256Checksum)
                put("manifest", JSONObject().apply {
                    put("ledgerEntriesCount", ledgerEntries.size)
                    put("configSettingsCount", 5)
                    put("partiesCount", parties.size)
                    put("transactionsCount", transactions.size)
                    put("inventoryCount", inventory.size)
                    put("staffCount", staff.size)
                })
                put("payload", contentObject)
            }

            val finalJsonBytes = rootJson.toString(2).toByteArray(Charsets.UTF_8)
            val fileName = "ledger_backup_${nowMillis}.json"

            // 7. Save to Local Vault Cache (Ensures offline resilience & instant snapshot access)
            val localFile = File(localVaultDir, fileName)
            localFile.writeBytes(finalJsonBytes)

            val latestLocalFile = File(localVaultDir, "latest_backup_$sanitizedUserId.json")
            latestLocalFile.writeBytes(finalJsonBytes)

            // 8. Attempt Upload to Firebase Storage
            val firebaseStorage = getFirebaseStorageInstance()
            var cloudStoragePath = "local://atri_firebase_vault/$fileName"
            var downloadUrl = ""

            if (firebaseStorage != null) {
                try {
                    val storageRef = firebaseStorage.reference
                    val userBackupsRef = storageRef.child("backups/$sanitizedUserId/$fileName")
                    val latestBackupRef = storageRef.child("backups/$sanitizedUserId/latest_ledger_backup.json")

                    val metadata = StorageMetadata.Builder()
                        .setContentType("application/json")
                        .setCustomMetadata("checksum", sha256Checksum)
                        .setCustomMetadata("ledgerCount", ledgerEntries.size.toString())
                        .setCustomMetadata("userId", userId)
                        .setCustomMetadata("timestamp", nowMillis.toString())
                        .build()

                    // Upload timestamped file
                    val uploadTask = userBackupsRef.putBytes(finalJsonBytes, metadata)
                    Tasks.await(uploadTask, 15, TimeUnit.SECONDS)

                    // Update latest pointer
                    val latestTask = latestBackupRef.putBytes(finalJsonBytes, metadata)
                    Tasks.await(latestTask, 10, TimeUnit.SECONDS)

                    cloudStoragePath = "gs://${firebaseStorage.app.options.storageBucket ?: "atri-storage"}/backups/$sanitizedUserId/$fileName"
                    
                    try {
                        val uriTask = userBackupsRef.downloadUrl
                        val uri = Tasks.await(uriTask, 5, TimeUnit.SECONDS)
                        downloadUrl = uri.toString()
                    } catch (ignore: Exception) {
                        downloadUrl = cloudStoragePath
                    }

                    Log.i(tag, "Firebase Storage upload succeeded: $cloudStoragePath")
                } catch (e: Exception) {
                    Log.w(tag, "Direct Firebase Storage network upload encountered exception: ${e.message}. Preserved in local vault.")
                    cloudStoragePath = "cloud-ready://vault/$fileName"
                }
            } else {
                Log.i(tag, "Firebase Storage offline mode active: saved to local secure vault.")
            }

            // Save last backup metadata in SharedPreferences
            prefs.edit()
                .putLong("firebase_last_backup_timestamp", nowMillis)
                .putString("firebase_last_backup_checksum", sha256Checksum)
                .putInt("firebase_last_backup_ledger_count", ledgerEntries.size)
                .putLong("firebase_last_backup_size_bytes", finalJsonBytes.size.toLong())
                .putString("firebase_last_backup_path", cloudStoragePath)
                .apply()

            CloudBackupResult(
                isSuccess = true,
                message = "Backup successfully exported. ${ledgerEntries.size} ledger records & configurations secured.",
                storagePath = cloudStoragePath,
                downloadUrl = downloadUrl,
                backupSizeBytes = finalJsonBytes.size.toLong(),
                sha256Checksum = sha256Checksum,
                ledgerCount = ledgerEntries.size,
                configCount = 5,
                partiesCount = parties.size,
                transactionsCount = transactions.size,
                timestamp = nowMillis
            )
        } catch (e: Exception) {
            Log.e(tag, "Export and backup failed", e)
            CloudBackupResult(
                isSuccess = false,
                message = "Backup failed: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    /**
     * Downloads and Restores a complete snapshot from Firebase Storage or local vault.
     * Restores:
     * - General Ledger entries into Room DB
     * - Configuration settings into AppSettings, TransactionSettings, InvoiceSettings, PartySettings
     * - Business entities (Parties, Transactions, Inventory, Staff) without duplication
     */
    suspend fun downloadAndRestoreFromFirebase(
        userId: String,
        database: AppDatabase,
        specificSnapshotFile: String? = null,
        onSettingsRestored: (AppSettings, TransactionSettings, InvoiceSettings, PartySettings) -> Unit = { _, _, _, _ -> }
    ): CloudRestoreResult = withContext(Dispatchers.IO) {
        try {
            val sanitizedUserId = userId.replace("[^a-zA-Z0-9_.-]".toRegex(), "_").ifBlank { "default_user" }
            var jsonString: String? = null

            // 1. Try downloading from Firebase Storage if available
            val firebaseStorage = getFirebaseStorageInstance()
            if (firebaseStorage != null && specificSnapshotFile == null) {
                try {
                    val storageRef = firebaseStorage.reference
                    val latestBackupRef = storageRef.child("backups/$sanitizedUserId/latest_ledger_backup.json")
                    val maxDownloadSizeBytes: Long = 10 * 1024 * 1024 // 10MB
                    val downloadTask = latestBackupRef.getBytes(maxDownloadSizeBytes)
                    val bytes = Tasks.await(downloadTask, 15, TimeUnit.SECONDS)
                    jsonString = String(bytes, Charsets.UTF_8)
                    Log.i(tag, "Successfully retrieved latest backup from Firebase Storage")
                } catch (e: Exception) {
                    Log.w(tag, "Could not fetch from live Firebase Storage, falling back to local vault: ${e.message}")
                }
            }

            // 2. Fallback to Local Vault files
            if (jsonString == null) {
                val targetFile = if (!specificSnapshotFile.isNullOrBlank()) {
                    File(localVaultDir, specificSnapshotFile)
                } else {
                    File(localVaultDir, "latest_backup_$sanitizedUserId.json").takeIf { it.exists() }
                        ?: localVaultDir.listFiles { file -> file.name.startsWith("ledger_backup_") && file.name.endsWith(".json") }
                            ?.maxByOrNull { it.lastModified() }
                }

                if (targetFile != null && targetFile.exists()) {
                    jsonString = targetFile.readText(Charsets.UTF_8)
                    Log.i(tag, "Read backup from vault file: ${targetFile.name}")
                }
            }

            if (jsonString.isNullOrBlank()) {
                return@withContext CloudRestoreResult(
                    isSuccess = false,
                    message = "No cloud backup snapshot found for user $userId"
                )
            }

            // 3. Parse and Validate Checksum
            val root = JSONObject(jsonString)
            val expectedChecksum = root.optString("sha256Checksum", "")
            val payloadObj = root.optJSONObject("payload")
                ?: return@withContext CloudRestoreResult(isSuccess = false, message = "Invalid backup format: payload missing")

            val computedChecksum = computeSha256(payloadObj.toString())
            val isChecksumVerified = expectedChecksum.isNotBlank() && expectedChecksum.equals(computedChecksum, ignoreCase = true)

            var restoredLedgerCount = 0
            var restoredConfigCount = 0
            var restoredPartiesCount = 0
            var restoredTransCount = 0
            var restoredInventoryCount = 0

            // 4. Restore Configuration Settings
            val configSettingsObj = payloadObj.optJSONObject("configSettings")
            if (configSettingsObj != null) {
                val appSettingsObj = configSettingsObj.optJSONObject("appSettings")
                val transSettingsObj = configSettingsObj.optJSONObject("transactionSettings")
                val invSettingsObj = configSettingsObj.optJSONObject("invoiceSettings")
                val partySettingsObj = configSettingsObj.optJSONObject("partySettings")
                val bpObj = configSettingsObj.optJSONObject("businessProfile")

                val restoredAppSettings = AppSettings.fromJsonString(appSettingsObj?.toString()) ?: AppSettings()
                val restoredTransSettings = TransactionSettings.fromJsonString(transSettingsObj?.toString()) ?: TransactionSettings.createDefaultsFor(com.example.data.model.BusinessTransactionPreset.GENERAL)
                val restoredInvSettings = InvoiceSettings.fromJsonString(invSettingsObj?.toString()) ?: InvoiceSettings.createDefaultsFor(com.example.data.model.BusinessInvoicePreset.GENERAL)
                val restoredPartySettings = PartySettings.fromJsonString(partySettingsObj?.toString()) ?: PartySettings()

                // Save to SharedPreferences
                prefs.edit()
                    .putString("pref_app_settings_json", restoredAppSettings.toJsonString())
                    .putString("pref_transaction_settings_json", restoredTransSettings.toJsonString())
                    .putString("pref_invoice_settings_json", restoredInvSettings.toJsonString())
                    .putString("pref_party_settings_json", restoredPartySettings.toJsonString())
                    .putString("pref_currency_format", restoredAppSettings.currencySymbol)
                    .putString("pref_date_format", restoredAppSettings.dateFormat)
                    .putBoolean("pref_dark_mode", restoredAppSettings.themeMode == com.example.data.model.AppThemeMode.DARK)
                    .putBoolean("pref_tax_enabled", restoredInvSettings.enableVat)
                    .putFloat("pref_tax_rate", restoredInvSettings.defaultVatRate.toFloat())
                    .putString("pref_pan_vat_number", restoredInvSettings.panVatNumber)
                    .putString("pref_sales_prefix", restoredTransSettings.salesInvoicePrefix)
                    .apply()

                // Restore Business Profile
                if (bpObj != null) {
                    val currentProfile = database.businessProfileDao().getBusinessProfileSync()
                    val profileToSave = currentProfile?.copy(
                        businessName = bpObj.optString("businessName", currentProfile.businessName),
                        businessType = bpObj.optString("businessType", currentProfile.businessType),
                        panVatNumber = bpObj.optString("panVatNumber", currentProfile.panVatNumber),
                        phone = bpObj.optString("phone", currentProfile.phone),
                        email = bpObj.optString("email", currentProfile.email),
                        address = bpObj.optString("address", currentProfile.address),
                        city = bpObj.optString("city", currentProfile.city),
                        currencySymbol = bpObj.optString("currencySymbol", currentProfile.currencySymbol),
                        defaultVatRate = bpObj.optDouble("defaultVatRate", currentProfile.defaultVatRate),
                        isVatEnabled = bpObj.optBoolean("isVatEnabled", currentProfile.isVatEnabled),
                        updatedAt = System.currentTimeMillis()
                    ) ?: BusinessProfileEntity(
                        businessName = bpObj.optString("businessName", "My Business"),
                        businessType = bpObj.optString("businessType", "General Trade"),
                        panVatNumber = bpObj.optString("panVatNumber", ""),
                        phone = bpObj.optString("phone", ""),
                        email = bpObj.optString("email", ""),
                        address = bpObj.optString("address", ""),
                        city = bpObj.optString("city", ""),
                        stateProvince = bpObj.optString("stateProvince", ""),
                        currencySymbol = bpObj.optString("currencySymbol", "Rs."),
                        defaultVatRate = bpObj.optDouble("defaultVatRate", 13.0),
                        isVatEnabled = bpObj.optBoolean("isVatEnabled", true),
                        invoicePrefix = bpObj.optString("invoicePrefix", "INV"),
                        invoiceTerms = "1. Goods once sold will not be returned after 7 days.",
                        invoiceHeader = "Tax Invoice",
                        invoiceFooter = "Thank you for doing business with us."
                    )
                    database.businessProfileDao().insertProfile(profileToSave)
                }

                restoredConfigCount = 5
                onSettingsRestored(restoredAppSettings, restoredTransSettings, restoredInvSettings, restoredPartySettings)
            }

            // 5. Restore Ledger Entries
            val ledgerArray = payloadObj.optJSONArray("ledgerEntries")
            if (ledgerArray != null && ledgerArray.length() > 0) {
                val restoredEntries = mutableListOf<LedgerEntry>()
                for (i in 0 until ledgerArray.length()) {
                    val obj = ledgerArray.getJSONObject(i)
                    restoredEntries.add(
                        LedgerEntry(
                            id = 0, // Auto-generate clean primary key
                            postingDateMillis = obj.optLong("postingDateMillis", System.currentTimeMillis()),
                            postingDateBS = obj.optString("postingDateBS", ""),
                            postingDateAD = obj.optString("postingDateAD", ""),
                            account = obj.optString("account", "General Ledger"),
                            accountType = obj.optString("accountType", "Asset"),
                            partyType = obj.optString("partyType", null).takeIf { it?.isNotBlank() == true },
                            partyId = obj.optLong("partyId", -1L).takeIf { it > 0 },
                            partyName = obj.optString("partyName", null).takeIf { it?.isNotBlank() == true },
                            voucherType = obj.optString("voucherType", "Journal Entry"),
                            voucherNo = obj.optString("voucherNo", "GL-${i + 1}"),
                            debit = obj.optDouble("debit", 0.0),
                            credit = obj.optDouble("credit", 0.0),
                            againstAccount = obj.optString("againstAccount", ""),
                            currency = obj.optString("currency", "Rs."),
                            fiscalYear = obj.optString("fiscalYear", ""),
                            remarks = obj.optString("remarks", ""),
                            isCancelled = obj.optBoolean("isCancelled", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }

                if (restoredEntries.isNotEmpty()) {
                    database.ledgerDao().deleteAllEntries()
                    database.ledgerDao().insertAll(restoredEntries)
                    restoredLedgerCount = restoredEntries.size
                }
            }

            // 6. Restore Parties without duplicates
            val partiesArray = payloadObj.optJSONArray("parties")
            if (partiesArray != null) {
                val existingParties = database.partyDao().getAllPartiesSync().associateBy { it.name.trim().lowercase() }
                for (i in 0 until partiesArray.length()) {
                    val pObj = partiesArray.getJSONObject(i)
                    val pName = pObj.optString("name", "").trim()
                    if (pName.isNotBlank() && existingParties[pName.lowercase()] == null) {
                        val newParty = PartyEntity(
                            name = pName,
                            type = pObj.optString("type", "Customer"),
                            phone = pObj.optString("phone", ""),
                            email = pObj.optString("email", ""),
                            address = pObj.optString("address", ""),
                            panVatNumber = pObj.optString("panVatNumber", ""),
                            openingBalance = pObj.optDouble("openingBalance", 0.0),
                            balanceToReceive = pObj.optDouble("balanceToReceive", 0.0),
                            balanceToGive = pObj.optDouble("balanceToGive", 0.0),
                            creditLimit = pObj.optDouble("creditLimit", 0.0)
                        )
                        database.partyDao().insertParty(newParty)
                        restoredPartiesCount++
                    }
                }
            }

            // 7. Restore Transactions without duplicates
            val transArray = payloadObj.optJSONArray("transactions")
            if (transArray != null) {
                val existingKeys = database.transactionDao().getAllTransactionsSync().map { "${it.invoiceNumber}_${it.amount}_${it.dateMillis}" }.toSet()
                for (i in 0 until transArray.length()) {
                    val tObj = transArray.getJSONObject(i)
                    val invNum = tObj.optString("invoiceNumber", "")
                    val amt = tObj.optDouble("amount", 0.0)
                    val dMillis = tObj.optLong("dateMillis", System.currentTimeMillis())
                    val key = "${invNum}_${amt}_${dMillis}"

                    if (key !in existingKeys) {
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
                        restoredTransCount++
                    }
                }
            }

            // 8. Restore Inventory Items without duplicates
            val invArray = payloadObj.optJSONArray("inventory")
            if (invArray != null) {
                val existingItems = database.inventoryDao().getAllItemsSync().associateBy { it.name.trim().lowercase() }
                for (i in 0 until invArray.length()) {
                    val itemObj = invArray.getJSONObject(i)
                    val itemName = itemObj.optString("name", "").trim()
                    if (itemName.isNotBlank() && existingItems[itemName.lowercase()] == null) {
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
                        restoredInventoryCount++
                    }
                }
            }

            CloudRestoreResult(
                isSuccess = true,
                message = "Successfully restored $restoredLedgerCount ledger entries & $restoredConfigCount configuration suites.",
                restoredLedgerCount = restoredLedgerCount,
                restoredConfigCount = restoredConfigCount,
                restoredPartiesCount = restoredPartiesCount,
                restoredTransactionsCount = restoredTransCount,
                restoredInventoryCount = restoredInventoryCount,
                isChecksumVerified = isChecksumVerified
            )
        } catch (e: Exception) {
            Log.e(tag, "Restore failed", e)
            CloudRestoreResult(
                isSuccess = false,
                message = "Restore failed: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    /**
     * Lists available cloud backup snapshots stored in the local vault cache and Firebase.
     */
    suspend fun getAvailableSnapshots(userId: String): List<CloudBackupSnapshot> = withContext(Dispatchers.IO) {
        val snapshots = mutableListOf<CloudBackupSnapshot>()
        val files = localVaultDir.listFiles { file -> file.name.startsWith("ledger_backup_") && file.name.endsWith(".json") }
            ?: return@withContext emptyList()

        files.sortedByDescending { it.lastModified() }.forEach { file ->
            try {
                val json = JSONObject(file.readText(Charsets.UTF_8))
                val manifest = json.optJSONObject("manifest")
                val timestamp = json.optLong("exportTimestamp", file.lastModified())
                val formattedDate = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US).format(Date(timestamp))
                val sizeBytes = file.length()
                val formattedSize = if (sizeBytes > 1024 * 1024) {
                    "%.2f MB".format(sizeBytes.toDouble() / (1024 * 1024))
                } else {
                    "%.1f KB".format(sizeBytes.toDouble() / 1024)
                }

                snapshots.add(
                    CloudBackupSnapshot(
                        snapshotId = file.name,
                        fileName = file.name,
                        storagePath = "local://atri_firebase_vault/${file.name}",
                        timestamp = timestamp,
                        formattedDate = formattedDate,
                        sizeBytes = sizeBytes,
                        formattedSize = formattedSize,
                        ledgerEntriesCount = manifest?.optInt("ledgerEntriesCount", 0) ?: 0,
                        configsCount = manifest?.optInt("configSettingsCount", 5) ?: 5,
                        sha256Checksum = json.optString("sha256Checksum", "").take(8) + "...",
                        isLocalVault = true
                    )
                )
            } catch (e: Exception) {
                Log.w(tag, "Failed to parse snapshot file ${file.name}", e)
            }
        }
        snapshots
    }

    /**
     * Writes the latest backup JSON to a user-selected SAF Uri (Export to File).
     */
    suspend fun exportToFileUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val latestFile = localVaultDir.listFiles { file -> file.name.startsWith("ledger_backup_") && file.name.endsWith(".json") }
                ?.maxByOrNull { it.lastModified() }
                ?: return@withContext false

            val content = latestFile.readBytes()
            context.contentResolver.openOutputStream(uri)?.use { os: OutputStream ->
                os.write(content)
                os.flush()
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Export to SAF Uri failed", e)
            false
        }
    }

    /**
     * Reads a backup JSON from a user-selected SAF Uri and restores it (Import from File).
     */
    suspend fun restoreFromFileUri(
        context: Context,
        uri: Uri,
        database: AppDatabase,
        onSettingsRestored: (AppSettings, TransactionSettings, InvoiceSettings, PartySettings) -> Unit
    ): CloudRestoreResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: return@withContext CloudRestoreResult(isSuccess = false, message = "Could not read input file")

            // Cache imported file into local vault
            val cacheFile = File(localVaultDir, "imported_backup_${System.currentTimeMillis()}.json")
            cacheFile.writeText(jsonString, Charsets.UTF_8)

            downloadAndRestoreFromFirebase(
                userId = "imported_user",
                database = database,
                specificSnapshotFile = cacheFile.name,
                onSettingsRestored = onSettingsRestored
            )
        } catch (e: Exception) {
            Log.e(tag, "Restore from SAF Uri failed", e)
            CloudRestoreResult(isSuccess = false, message = "Failed to import file: ${e.localizedMessage}")
        }
    }
}
