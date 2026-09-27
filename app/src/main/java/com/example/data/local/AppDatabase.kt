package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BusinessProfileDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.PartyDao
import com.example.data.local.dao.SalesInvoiceDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InvoiceActivityEntity
import com.example.data.local.entity.InvoiceAttachmentEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.local.entity.TransactionEntity
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PartyEntity::class,
        TransactionEntity::class,
        InventoryItemEntity::class,
        StaffMemberEntity::class,
        BusinessProfileEntity::class,
        SalesInvoiceEntity::class,
        SalesInvoiceItemEntity::class,
        PaymentAllocationEntity::class,
        InvoiceActivityEntity::class,
        InvoiceAttachmentEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partyDao(): PartyDao
    abstract fun transactionDao(): TransactionDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun staffDao(): StaffDao
    abstract fun salesInvoiceDao(): SalesInvoiceDao
    abstract fun businessProfileDao(): BusinessProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. business_profiles table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `business_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `businessName` TEXT NOT NULL,
                        `businessType` TEXT NOT NULL,
                        `panVatNumber` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `stateProvince` TEXT NOT NULL,
                        `country` TEXT NOT NULL,
                        `currencySymbol` TEXT NOT NULL,
                        `defaultVatRate` REAL NOT NULL,
                        `isVatEnabled` INTEGER NOT NULL,
                        `invoicePrefix` TEXT NOT NULL,
                        `invoiceTerms` TEXT NOT NULL,
                        `invoiceHeader` TEXT NOT NULL,
                        `invoiceFooter` TEXT NOT NULL,
                        `logoUri` TEXT,
                        `signatureUri` TEXT,
                        `bankName` TEXT,
                        `bankAccountNumber` TEXT,
                        `bankBranch` TEXT,
                        `qrCodePayload` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. sales_invoices table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sales_invoices` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceNumber` TEXT NOT NULL,
                        `partyId` INTEGER,
                        `partyNameSnapshot` TEXT NOT NULL,
                        `partyPhoneSnapshot` TEXT,
                        `partyAddressSnapshot` TEXT,
                        `partyPanSnapshot` TEXT,
                        `invoiceDateBS` TEXT NOT NULL,
                        `invoiceDateAD` TEXT NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `invoiceStatus` TEXT NOT NULL,
                        `subtotal` REAL NOT NULL,
                        `discountAmount` REAL NOT NULL,
                        `discountPercent` REAL NOT NULL,
                        `taxableAmount` REAL NOT NULL,
                        `vatRate` REAL NOT NULL,
                        `vatAmount` REAL NOT NULL,
                        `roundOff` REAL NOT NULL,
                        `grandTotal` REAL NOT NULL,
                        `paidAmount` REAL NOT NULL,
                        `dueAmount` REAL NOT NULL,
                        `paymentStatus` TEXT NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `paymentAccountId` TEXT,
                        `dueDateBS` TEXT,
                        `dueDateAD` TEXT,
                        `dueDateMillis` INTEGER,
                        `salespersonId` INTEGER,
                        `salespersonName` TEXT,
                        `referenceNumber` TEXT,
                        `remarks` TEXT,
                        `isTaxInvoice` INTEGER NOT NULL,
                        `businessProfileId` INTEGER,
                        `createdBy` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`partyId`) REFERENCES `parties`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(`salespersonId`) REFERENCES `staff_members`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(`businessProfileId`) REFERENCES `business_profiles`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_invoices_partyId` ON `sales_invoices` (`partyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_invoices_salespersonId` ON `sales_invoices` (`salespersonId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_invoices_businessProfileId` ON `sales_invoices` (`businessProfileId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sales_invoices_invoiceNumber` ON `sales_invoices` (`invoiceNumber`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_invoices_dateMillis` ON `sales_invoices` (`dateMillis`)")

                // 3. sales_invoice_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sales_invoice_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceId` INTEGER NOT NULL,
                        `productId` INTEGER,
                        `productNameSnapshot` TEXT NOT NULL,
                        `productCodeSnapshot` TEXT,
                        `quantity` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `rate` REAL NOT NULL,
                        `discount` REAL NOT NULL,
                        `discountPercent` REAL NOT NULL,
                        `vatRate` REAL NOT NULL,
                        `vatAmount` REAL NOT NULL,
                        `lineTotal` REAL NOT NULL,
                        `batchNumber` TEXT,
                        `expiryDate` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`invoiceId`) REFERENCES `sales_invoices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`productId`) REFERENCES `inventory_items`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_invoice_items_invoiceId` ON `sales_invoice_items` (`invoiceId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_invoice_items_productId` ON `sales_invoice_items` (`productId`)")

                // 4. payment_allocations table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payment_allocations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceId` INTEGER NOT NULL,
                        `partyId` INTEGER,
                        `paymentAmount` REAL NOT NULL,
                        `paymentDateMillis` INTEGER NOT NULL,
                        `paymentDateBS` TEXT NOT NULL,
                        `paymentDateAD` TEXT NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `paymentAccountId` TEXT,
                        `transactionReference` TEXT,
                        `remarks` TEXT,
                        `recordedBy` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`invoiceId`) REFERENCES `sales_invoices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`partyId`) REFERENCES `parties`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocations_invoiceId` ON `payment_allocations` (`invoiceId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocations_partyId` ON `payment_allocations` (`partyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocations_paymentDateMillis` ON `payment_allocations` (`paymentDateMillis`)")

                // 5. invoice_activities table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoice_activities` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceId` INTEGER NOT NULL,
                        `action` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `performedBy` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL,
                        FOREIGN KEY(`invoiceId`) REFERENCES `sales_invoices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoice_activities_invoiceId` ON `invoice_activities` (`invoiceId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoice_activities_timestampMillis` ON `invoice_activities` (`timestampMillis`)")

                // 6. transactions table alteration
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `salesInvoiceId` INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add optional adjustment columns to sales_invoices table
                db.execSQL("ALTER TABLE `sales_invoices` ADD COLUMN `isDiscountApplied` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `sales_invoices` ADD COLUMN `isVatApplied` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `sales_invoices` ADD COLUMN `isExtraChargeApplied` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `sales_invoices` ADD COLUMN `extraChargeAmount` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `sales_invoices` ADD COLUMN `extraChargeDescription` TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE `sales_invoices` ADD COLUMN `preRoundTotal` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `sales_invoices` ADD COLUMN `isRoundOffApplied` INTEGER NOT NULL DEFAULT 0")

                // Preserve historical calculations for existing invoices
                db.execSQL("UPDATE `sales_invoices` SET `isDiscountApplied` = 1 WHERE `discountAmount` > 0")
                db.execSQL("UPDATE `sales_invoices` SET `isVatApplied` = 1 WHERE `vatAmount` > 0")
                db.execSQL("UPDATE `sales_invoices` SET `isRoundOffApplied` = 1 WHERE `roundOff` != 0.0")
                db.execSQL("UPDATE `sales_invoices` SET `preRoundTotal` = `grandTotal` - `roundOff`")

                // 2. Create invoice_attachments table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoice_attachments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceId` INTEGER NOT NULL,
                        `fileName` TEXT NOT NULL,
                        `fileType` TEXT NOT NULL,
                        `mimeType` TEXT NOT NULL,
                        `uriString` TEXT NOT NULL,
                        `fileSize` INTEGER NOT NULL,
                        `description` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`invoiceId`) REFERENCES `sales_invoices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoice_attachments_invoiceId` ON `invoice_attachments` (`invoiceId`)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atri_nova_business.db"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val businessProfileDao = database.businessProfileDao()
            if (businessProfileDao.getProfileCount() == 0) {
                val profile = BusinessProfileEntity(
                    businessName = "My Business",
                    businessType = "General Trade",
                    panVatNumber = "",
                    phone = "",
                    email = "",
                    address = "",
                    city = "",
                    stateProvince = "",
                    currencySymbol = "Rs.",
                    defaultVatRate = 13.0,
                    isVatEnabled = true,
                    invoicePrefix = "INV",
                    invoiceTerms = "1. Goods once sold will not be returned after 7 days.\n2. Payment is due within 15 days of invoice date.",
                    invoiceHeader = "Tax Invoice / Bill of Supply",
                    invoiceFooter = "Thank you for choosing Atri Khata! Visit again.",
                    bankName = null,
                    bankAccountNumber = null,
                    bankBranch = null
                )
                businessProfileDao.insertProfile(profile)
            }

            // Cleanup any previously seeded demo data so the app starts fresh and clean
            cleanupLegacyDemoData(database)
        }

        private fun cleanupLegacyDemoData(database: AppDatabase) {
            try {
                val db = database.openHelper.writableDatabase
                db.execSQL("DELETE FROM parties WHERE name IN ('Cash Transactions', 'NCG Medimart', 'Nepal Red Cross Society', 'Life Check Pharmacy', 'UNIQUE MEDICINE DISTRIBUTORS', 'Bhupendra Dev M...', 'Neomed Pvt. Ltd.', 'Rakesh Tuladhar', 'Annapurna Sales', 'Sunrise Stores', 'ABC Enterprises', 'Galaxy Traders', 'Electricity Office', 'Himalayan Suppliers', 'Walk-in Cash Customer')")
                db.execSQL("DELETE FROM transactions WHERE invoiceNumber IN ('Sale #21', 'Sale #20', 'Sale #19', 'INV-00124', 'Sale #17', 'Payment In #6', 'Payment Out #4', 'EXP-001', 'EXP-002', 'EXP-003', 'INV-00125', 'PUR-0048', 'BNK-0003', 'TRF-0007', 'UTL-0012', 'SUP-0034') OR partyName IN ('Cash Transactions', 'NCG Medimart', 'Nepal Red Cross Society', 'Life Check Pharmacy', 'UNIQUE MEDICINE DISTRIBUTORS', 'Bhupendra Dev M...', 'Neomed Pvt. Ltd.', 'Rakesh Tuladhar', 'Annapurna Sales', 'Sunrise Stores', 'ABC Enterprises', 'Galaxy Traders', 'Electricity Office', 'Himalayan Suppliers')")
                db.execSQL("DELETE FROM inventory_items WHERE sku IN ('POS-T4G-01', 'ACC-ROLL-80', 'SCN-2D-PRO', 'PPR-A4-75', 'SEC-KEY-FOB')")
                db.execSQL("DELETE FROM sales_invoices WHERE invoiceNumber IN ('Sale #21', 'Sale #20', 'Sale #19', 'INV-00124', 'Sale #17')")
                db.execSQL("DELETE FROM sales_invoice_items WHERE productNameSnapshot IN ('Thermal Receipt Roll (80mm x 50m)', 'Atri Smart POS Terminal 4G', 'Omnidirectional 2D Barcode Scanner', 'A4 Copier Paper 75GSM', 'Cloud Backup Key Fob Hardware', 'POS Thermal Head Cleaner Kit')")
            } catch (e: Exception) {
                // Ignore if tables or columns already clean
            }
        }
    }
}
