package com.example.util

import kotlin.math.round

enum class BillDiscountType {
    PERCENTAGE,
    AMOUNT
}

data class InvoiceLineItemInput(
    val quantity: Double,
    val unitPrice: Double,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0
) {
    val grossAmount: Double
        get() = (quantity * unitPrice).coerceAtLeast(0.0)

    val itemDiscountAmount: Double
        get() = if (discountPercent > 0.0) {
            (grossAmount * (discountPercent / 100.0)).coerceAtLeast(0.0).coerceAtMost(grossAmount)
        } else {
            discountAmount.coerceAtLeast(0.0).coerceAtMost(grossAmount)
        }

    val netAmount: Double
        get() = (grossAmount - itemDiscountAmount).coerceAtLeast(0.0)
}

data class InvoiceCalculationResult(
    val grossItemAmount: Double,
    val itemDiscountTotal: Double,
    val itemNetAmount: Double,
    val isDiscountApplied: Boolean,
    val billDiscountType: BillDiscountType,
    val billDiscountInput: Double,
    val billDiscountAmount: Double,
    val totalDiscountAmount: Double,
    val taxableAmount: Double,
    val isVatApplied: Boolean,
    val vatRate: Double,
    val vatAmount: Double,
    val isExtraChargeApplied: Boolean,
    val extraChargeDescription: String?,
    val extraChargeAmount: Double,
    val preRoundTotal: Double,
    val isRoundOffApplied: Boolean,
    val roundOffAmount: Double,
    val grandTotal: Double
) {
    val roundOff: Double
        get() = roundOffAmount
}

object InvoiceCalculationEngine {

    /**
     * Centralized calculation flow strictly adhering to:
     * Gross Item Amount
     * − Item Discount
     * = Item Net Amount
     * − Bill Discount
     * = Taxable/Base Amount
     * + VAT
     * + Extra Charge
     * = Pre-Round Total
     * ± Round Off
     * = Grand Total
     */
    fun calculate(
        items: List<InvoiceLineItemInput>,
        isDiscountApplied: Boolean = false,
        billDiscountType: BillDiscountType = BillDiscountType.AMOUNT,
        billDiscountValue: Double = 0.0,
        isVatApplied: Boolean = false,
        vatRate: Double = 13.0,
        isExtraChargeApplied: Boolean = false,
        extraChargeDescription: String? = null,
        extraChargeAmount: Double = 0.0,
        isRoundOffApplied: Boolean = false,
        roundingMethod: String = "Normal (Nearest 1.00)"
    ): InvoiceCalculationResult {
        val gross = items.sumOf { it.grossAmount }
        val itemDisc = items.sumOf { it.itemDiscountAmount }
        val netItem = (gross - itemDisc).coerceAtLeast(0.0)

        val billDisc = if (isDiscountApplied && billDiscountValue > 0.0) {
            when (billDiscountType) {
                BillDiscountType.PERCENTAGE -> {
                    val pct = billDiscountValue.coerceAtLeast(0.0).coerceAtMost(100.0)
                    round2(netItem * (pct / 100.0)).coerceAtMost(netItem)
                }
                BillDiscountType.AMOUNT -> {
                    round2(billDiscountValue.coerceAtLeast(0.0)).coerceAtMost(netItem)
                }
            }
        } else {
            0.0
        }

        val totalDiscount = round2(itemDisc + billDisc)
        val taxable = round2((netItem - billDisc).coerceAtLeast(0.0))

        val vat = if (isVatApplied && vatRate > 0.0) {
            round2(taxable * (vatRate / 100.0))
        } else {
            0.0
        }

        val extraCharge = if (isExtraChargeApplied && extraChargeAmount > 0.0) {
            round2(extraChargeAmount.coerceAtLeast(0.0))
        } else {
            0.0
        }

        val preRound = round2(taxable + vat + extraCharge)

        val (roundOff, finalGrandTotal) = if (isRoundOffApplied) {
            val rounded = when {
                roundingMethod.contains("Up", ignoreCase = true) -> kotlin.math.ceil(preRound)
                roundingMethod.contains("Down", ignoreCase = true) -> kotlin.math.floor(preRound)
                else -> round(preRound)
            }
            val diff = round2(rounded - preRound)
            Pair(diff, rounded)
        } else {
            Pair(0.0, preRound)
        }

        return InvoiceCalculationResult(
            grossItemAmount = round2(gross),
            itemDiscountTotal = round2(itemDisc),
            itemNetAmount = round2(netItem),
            isDiscountApplied = isDiscountApplied && billDisc > 0.0,
            billDiscountType = billDiscountType,
            billDiscountInput = billDiscountValue,
            billDiscountAmount = billDisc,
            totalDiscountAmount = totalDiscount,
            taxableAmount = taxable,
            isVatApplied = isVatApplied && vat > 0.0,
            vatRate = if (isVatApplied) vatRate else 0.0,
            vatAmount = vat,
            isExtraChargeApplied = isExtraChargeApplied && extraCharge > 0.0,
            extraChargeDescription = if (isExtraChargeApplied && extraCharge > 0.0) extraChargeDescription else null,
            extraChargeAmount = extraCharge,
            preRoundTotal = preRound,
            isRoundOffApplied = isRoundOffApplied,
            roundOffAmount = roundOff,
            grandTotal = round2(finalGrandTotal)
        )
    }

    private fun round2(value: Double): Double {
        return (round(value * 100.0) / 100.0)
    }
}
