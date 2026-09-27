package com.example.service

import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PartyEntity

data class PartyImportParsedRow(
    val rowIndex: Int,
    val rawValues: Map<String, String>,
    val party: PartyEntity,
    val isDuplicate: Boolean,
    val existingPartyId: Long? = null,
    val isValid: Boolean,
    val validationError: String? = null
)

data class ItemImportParsedRow(
    val rowIndex: Int,
    val rawValues: Map<String, String>,
    val item: InventoryItemEntity,
    val isDuplicate: Boolean,
    val existingItemId: Long? = null,
    val isValid: Boolean,
    val validationError: String? = null
)

data class ImportResultSummary(
    val totalProcessed: Int,
    val importedCount: Int,
    val updatedCount: Int,
    val skippedDuplicatesCount: Int,
    val invalidCount: Int,
    val errorMessages: List<String> = emptyList()
)

object BulkImportService {

    /**
     * Parse CSV or TSV text into headers and row value lists.
     * Supports commas, semicolons, and tabs.
     * Handles quoted values with escaped quotes.
     */
    fun parseDelimitedText(rawContent: String): Pair<List<String>, List<List<String>>> {
        val cleanContent = rawContent.replace("\uFEFF", "").trim()
        if (cleanContent.isBlank()) return Pair(emptyList(), emptyList())

        val lines = cleanContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return Pair(emptyList(), emptyList())

        // Auto-detect delimiter from the first line
        val firstLine = lines.first()
        val delimiter = when {
            firstLine.count { it == '\t' } > firstLine.count { it == ',' } -> '\t'
            firstLine.count { it == ';' } > firstLine.count { it == ',' } -> ';'
            else -> ','
        }

        val allParsedLines = lines.map { parseLine(it, delimiter) }
        val headers = allParsedLines.firstOrNull()?.map { it.trim() } ?: emptyList()
        val rows = if (allParsedLines.size > 1) allParsedLines.drop(1) else emptyList()

        return Pair(headers, rows)
    }

    private fun parseLine(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    // Escaped double quote
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Map parsed rows to Party records and validate against existing parties.
     */
    fun processParties(
        headers: List<String>,
        rows: List<List<String>>,
        columnMapping: Map<String, String>, // System Field -> Header Name
        existingParties: List<PartyEntity>
    ): List<PartyImportParsedRow> {
        val headerIndices = headers.mapIndexed { index, name -> name to index }.toMap()

        return rows.mapIndexed { index, rowTokens ->
            fun getValue(systemField: String): String {
                val headerName = columnMapping[systemField] ?: return ""
                val colIdx = headerIndices[headerName] ?: return ""
                return if (colIdx in rowTokens.indices) rowTokens[colIdx].trim() else ""
            }

            val name = getValue("name")
            val phone = getValue("phone")
            val contactNumber = getValue("contactNumber")
            val rawType = getValue("type")
            val type = if (rawType.contains("supplier", ignoreCase = true)) "Supplier" else "Customer"
            val category = getValue("category").ifBlank { if (type == "Customer") "Retail" else "Wholesaler" }
            val balanceStr = getValue("openingBalance")
            val openingBalance = balanceStr.replace(",", "").toDoubleOrNull() ?: 0.0
            val rawBalType = getValue("balanceType")
            val balanceType = when {
                rawBalType.contains("give", ignoreCase = true) || rawBalType.contains("cr", ignoreCase = true) -> "To Give (Cr)"
                else -> "To Receive (Dr)"
            }
            val panVat = getValue("panVatNumber")
            val email = getValue("email")
            val contactPerson = getValue("contactPerson")
            val address = getValue("address")
            val city = getValue("city")
            val creditLimitStr = getValue("creditLimit")
            val creditLimit = creditLimitStr.replace(",", "").toDoubleOrNull() ?: 50000.0
            val notes = getValue("notes")

            val isValid = name.isNotBlank()
            val validationError = if (!isValid) "Party name is required" else null

            // Detect duplicate in existing database
            val duplicateMatch = existingParties.firstOrNull { existing ->
                existing.name.trim().equals(name, ignoreCase = true) ||
                        (phone.isNotBlank() && existing.phone.isNotBlank() && existing.phone.trim() == phone)
            }

            val isDr = balanceType.contains("Receive", ignoreCase = true) || balanceType.contains("Dr", ignoreCase = true)
            val isCr = balanceType.contains("Give", ignoreCase = true) || balanceType.contains("Cr", ignoreCase = true)

            val partyEntity = PartyEntity(
                id = duplicateMatch?.id ?: 0L,
                name = name,
                phone = phone,
                contactNumber = contactNumber,
                type = type,
                category = category,
                balanceToReceive = if (isDr) openingBalance else 0.0,
                balanceToGive = if (isCr) openingBalance else 0.0,
                openingBalance = openingBalance,
                balanceType = balanceType,
                panVatNumber = panVat,
                email = email,
                contactPerson = contactPerson,
                address = address,
                city = city,
                creditLimit = creditLimit,
                notes = notes,
                registerDate = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )

            val rawMap = mutableMapOf<String, String>()
            headers.forEachIndexed { hIdx, hName ->
                if (hIdx in rowTokens.indices) {
                    rawMap[hName] = rowTokens[hIdx]
                }
            }

            PartyImportParsedRow(
                rowIndex = index + 1,
                rawValues = rawMap,
                party = partyEntity,
                isDuplicate = duplicateMatch != null,
                existingPartyId = duplicateMatch?.id,
                isValid = isValid,
                validationError = validationError
            )
        }
    }

    /**
     * Map parsed rows to Inventory Item records and validate against existing items.
     */
    fun processItems(
        headers: List<String>,
        rows: List<List<String>>,
        columnMapping: Map<String, String>, // System Field -> Header Name
        existingItems: List<InventoryItemEntity>
    ): List<ItemImportParsedRow> {
        val headerIndices = headers.mapIndexed { index, name -> name to index }.toMap()

        return rows.mapIndexed { index, rowTokens ->
            fun getValue(systemField: String): String {
                val headerName = columnMapping[systemField] ?: return ""
                val colIdx = headerIndices[headerName] ?: return ""
                return if (colIdx in rowTokens.indices) rowTokens[colIdx].trim() else ""
            }

            val name = getValue("name")
            val sku = getValue("sku").ifBlank { "SKU-${(1000 + index)}" }
            val category = getValue("category").ifBlank { "General" }
            val stockQty = getValue("stockQuantity").replace(",", "").toDoubleOrNull() ?: 0.0
            val unit = getValue("unit").ifBlank { "pcs" }
            val purchasePrice = getValue("purchasePrice").replace(",", "").toDoubleOrNull() ?: 0.0
            val salePrice = getValue("salePrice").replace(",", "").toDoubleOrNull() ?: 0.0
            val minStockAlert = getValue("minStockAlert").replace(",", "").toDoubleOrNull() ?: 5.0

            val isValid = name.isNotBlank() && salePrice >= 0.0
            val validationError = when {
                name.isBlank() -> "Item name is required"
                salePrice < 0.0 -> "Sale price cannot be negative"
                else -> null
            }

            // Detect duplicate in existing database
            val duplicateMatch = existingItems.firstOrNull { existing ->
                existing.name.trim().equals(name, ignoreCase = true) ||
                        (sku.isNotBlank() && existing.sku.isNotBlank() && existing.sku.trim().equals(sku, ignoreCase = true))
            }

            val itemEntity = InventoryItemEntity(
                id = duplicateMatch?.id ?: 0L,
                name = name,
                sku = sku,
                category = category,
                stockQuantity = stockQty,
                unit = unit,
                purchasePrice = purchasePrice,
                salePrice = salePrice,
                minStockAlert = minStockAlert,
                createdAt = System.currentTimeMillis()
            )

            val rawMap = mutableMapOf<String, String>()
            headers.forEachIndexed { hIdx, hName ->
                if (hIdx in rowTokens.indices) {
                    rawMap[hName] = rowTokens[hIdx]
                }
            }

            ItemImportParsedRow(
                rowIndex = index + 1,
                rawValues = rawMap,
                item = itemEntity,
                isDuplicate = duplicateMatch != null,
                existingItemId = duplicateMatch?.id,
                isValid = isValid,
                validationError = validationError
            )
        }
    }

    /**
     * Auto-suggest column mapping from detected CSV headers for Parties.
     */
    fun suggestPartyMapping(headers: List<String>): Map<String, String> {
        val mapping = mutableMapOf<String, String>()
        for (header in headers) {
            val h = header.lowercase().trim()
            when {
                h.contains("alt") || h.contains("secondary") -> mapping.putIfAbsent("contactNumber", header)
                h.contains("name") || h.contains("party") || h.contains("customer") || h.contains("supplier") -> mapping.putIfAbsent("name", header)
                h.contains("phone") || h.contains("mobile") || h.contains("cell") || h.contains("contact") -> mapping.putIfAbsent("phone", header)
                h.contains("type") -> mapping.putIfAbsent("type", header)
                h.contains("category") || h.contains("group") -> mapping.putIfAbsent("category", header)
                h.contains("balance type") || h.contains("dr_cr") -> mapping.putIfAbsent("balanceType", header)
                h.contains("balance") || h.contains("opening") -> mapping.putIfAbsent("openingBalance", header)
                h.contains("pan") || h.contains("vat") || h.contains("tax") -> mapping.putIfAbsent("panVatNumber", header)
                h.contains("email") || h.contains("mail") -> mapping.putIfAbsent("email", header)
                h.contains("person") || h.contains("rep") -> mapping.putIfAbsent("contactPerson", header)
                h.contains("address") || h.contains("location") -> mapping.putIfAbsent("address", header)
                h.contains("city") || h.contains("district") -> mapping.putIfAbsent("city", header)
                h.contains("credit") || h.contains("limit") -> mapping.putIfAbsent("creditLimit", header)
                h.contains("note") || h.contains("remark") -> mapping.putIfAbsent("notes", header)
            }
        }
        return mapping
    }

    /**
     * Auto-suggest column mapping from detected CSV headers for Inventory Items.
     */
    fun suggestItemMapping(headers: List<String>): Map<String, String> {
        val mapping = mutableMapOf<String, String>()
        for (header in headers) {
            val h = header.lowercase().trim()
            when {
                h.contains("item") || h.contains("product") || h.contains("title") || (h.contains("name") && !h.contains("cat")) -> mapping.putIfAbsent("name", header)
                h.contains("sku") || h.contains("code") || h.contains("barcode") -> mapping.putIfAbsent("sku", header)
                h.contains("category") || h.contains("group") -> mapping.putIfAbsent("category", header)
                h.contains("stock") || h.contains("quantity") || h.contains("qty") -> mapping.putIfAbsent("stockQuantity", header)
                h.contains("unit") || h.contains("uom") -> mapping.putIfAbsent("unit", header)
                h.contains("purchase") || h.contains("cost") || h.contains("buy") -> mapping.putIfAbsent("purchasePrice", header)
                h.contains("sale") || h.contains("selling") || h.contains("mrp") || h.contains("price") -> mapping.putIfAbsent("salePrice", header)
                h.contains("min") || h.contains("alert") || h.contains("reorder") -> mapping.putIfAbsent("minStockAlert", header)
            }
        }
        return mapping
    }

    /**
     * Sample Parties CSV with ready-to-test records.
     */
    fun getSamplePartiesCsv(): String {
        return """
Party Name,Phone,Type,Category,Opening Balance,Balance Type,PAN/VAT,City,Address
Shree Krishna Traders,9841234567,Customer,Wholesale,12500,To Receive (Dr),601234567,Kathmandu,New Road
Himalayan Suppliers,9851098765,Supplier,Manufacturer,45000,To Give (Cr),302345678,Pokhara,Chipledhunga
Laxmi General Store,9803456789,Customer,Retail,8200,To Receive (Dr),,Lalitpur,Patan Dhoka
Everest Tech Hub,9812987654,Customer,Distributor,25000,To Receive (Dr),609876543,Bhaktapur,Suryabinayak
Sagarmatha Packaging,9865123456,Supplier,Wholesaler,18000,To Give (Cr),301122334,Biratnagar,Main Road
        """.trimIndent()
    }

    /**
     * Sample Inventory Items CSV with ready-to-test records.
     */
    fun getSampleItemsCsv(): String {
        return """
Item Name,SKU,Category,Stock Qty,Unit,Purchase Price,Sale Price,Min Stock
HP LaserJet 1020 Toner,TONER-HP-1020,Printers,25,pcs,1200,1850,5
A4 Copier Paper (75 GSM),PAPER-A4-75,Stationery,120,ream,420,550,20
Thermal Receipt Roll 80mm,ROLL-80MM,POS Supplies,300,roll,45,75,50
Logitech Wireless Mouse M185,MOUSE-M185,Hardware,40,pcs,850,1250,8
Epson EcoTank Black Ink 003,INK-003-BK,Printers,60,bottle,550,750,15
        """.trimIndent()
    }
}
