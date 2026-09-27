package com.example.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.InvoiceActivityEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.local.entity.StaffMemberEntity

data class SalesInvoiceWithDetails(
    @Embedded
    val invoice: SalesInvoiceEntity,

    @Relation(
        parentColumn = "partyId",
        entityColumn = "id"
    )
    val party: PartyEntity?,

    @Relation(
        parentColumn = "salespersonId",
        entityColumn = "id"
    )
    val salesperson: StaffMemberEntity?,

    @Relation(
        parentColumn = "businessProfileId",
        entityColumn = "id"
    )
    val businessProfile: BusinessProfileEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<SalesInvoiceItemEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val payments: List<PaymentAllocationEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val activities: List<InvoiceActivityEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val attachments: List<com.example.data.local.entity.InvoiceAttachmentEntity> = emptyList()
)
