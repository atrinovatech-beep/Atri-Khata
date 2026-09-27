package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.local.entity.InvoiceActivityEntity
import com.example.data.local.entity.InvoiceAttachmentEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.local.relation.SalesInvoiceWithDetails
import com.example.service.pdf.PdfReportService
import com.example.ui.ActiveDialog
import com.example.ui.MainViewModel
import com.example.ui.NavTab
import com.example.util.AttachmentUtils
import com.example.util.NepaliDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesInvoiceDetailScreen(
    invoiceId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    initialInvoiceWithDetails: com.example.data.local.relation.SalesInvoiceWithDetails? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkMode = isSystemInDarkTheme()

    val allInvoicesWithDetails by viewModel.allInvoicesWithDetails.collectAsStateWithLifecycle()
    val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val allParties by viewModel.allParties.collectAsStateWithLifecycle()
    val invoiceSettings by viewModel.invoiceSettings.collectAsStateWithLifecycle()

    val invoiceWithDetails = remember(allInvoicesWithDetails, invoiceId, initialInvoiceWithDetails) {
        initialInvoiceWithDetails ?: allInvoicesWithDetails.find { it.invoice.id == invoiceId }
    }

    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var showPrintExportModal by remember { mutableStateOf(false) }
    var isProcessingAction by remember { mutableStateOf(false) }

    fun doPrint(isReceipt: Boolean) {
        if (invoiceWithDetails == null) return
        scope.launch(Dispatchers.IO) {
            val file = if (isReceipt) {
                PdfReportService.generateSalesReceiptPdf(context, invoiceWithDetails)
            } else {
                PdfReportService.generateSalesInvoicePdf(context, invoiceWithDetails, invoiceSettings)
            }
            withContext(Dispatchers.Main) {
                val title = if (isReceipt) "Receipt_${invoiceWithDetails.invoice.invoiceNumber}" else "Invoice_${invoiceWithDetails.invoice.invoiceNumber}"
                PdfReportService.printPdf(context, file, title)
            }
        }
    }

    fun doShare(isReceipt: Boolean) {
        if (invoiceWithDetails == null) return
        scope.launch(Dispatchers.IO) {
            val file = if (isReceipt) {
                PdfReportService.generateSalesReceiptPdf(context, invoiceWithDetails)
            } else {
                PdfReportService.generateSalesInvoicePdf(context, invoiceWithDetails, invoiceSettings)
            }
            withContext(Dispatchers.Main) {
                val subject = if (isReceipt) "POS Receipt ${invoiceWithDetails.invoice.invoiceNumber}" else "Sales Invoice ${invoiceWithDetails.invoice.invoiceNumber}"
                PdfReportService.sharePdf(context, file, subject)
            }
        }
    }

    fun doDownload(isReceipt: Boolean) {
        if (invoiceWithDetails == null) return
        scope.launch(Dispatchers.IO) {
            val file = if (isReceipt) {
                PdfReportService.generateSalesReceiptPdf(context, invoiceWithDetails)
            } else {
                PdfReportService.generateSalesInvoicePdf(context, invoiceWithDetails, invoiceSettings)
            }
            val prefix = if (isReceipt) "Receipt" else "Invoice"
            val cleanNumber = invoiceWithDetails.invoice.invoiceNumber.replace("#", "").replace(" ", "_")
            val fileName = "${prefix}_${cleanNumber}_${System.currentTimeMillis()}.pdf"
            val uri = PdfReportService.downloadPdf(context, file, fileName)
            withContext(Dispatchers.Main) {
                if (uri != null) {
                    Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Unable to save to Downloads folder", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun doPreview(isReceipt: Boolean) {
        if (invoiceWithDetails == null) return
        scope.launch(Dispatchers.IO) {
            val file = if (isReceipt) {
                PdfReportService.generateSalesReceiptPdf(context, invoiceWithDetails)
            } else {
                PdfReportService.generateSalesInvoicePdf(context, invoiceWithDetails, invoiceSettings)
            }
            withContext(Dispatchers.Main) {
                PdfReportService.openPdf(context, file)
            }
        }
    }

    // Dynamic Theme-Aware Colors
    val screenBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val primaryColor = Color(0xFF2563EB)

    if (invoiceWithDetails == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Sales Invoice", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = screenBg,
                        titleContentColor = textPrimary,
                        navigationIconContentColor = textPrimary
                    )
                )
            },
            containerColor = screenBg
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = primaryColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading invoice details...", color = textSecondary, fontSize = 14.sp)
                }
            }
        }
        return
    }

    val invoice = invoiceWithDetails.invoice
    val items = invoiceWithDetails.items
    val payments = invoiceWithDetails.payments
    val activities = invoiceWithDetails.activities
    val attachments = invoiceWithDetails.attachments
    val party = invoiceWithDetails.party ?: allParties.find { it.id == invoice.partyId }
    val effectiveBusiness = invoiceWithDetails.businessProfile ?: businessProfile

    var previewAttachment by remember { mutableStateOf<InvoiceAttachmentEntity?>(null) }
    var attachmentToDelete by remember { mutableStateOf<InvoiceAttachmentEntity?>(null) }

    val detailImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val att = AttachmentUtils.saveUriToInternalStorage(context, it, forcedPdf = false)
            if (att != null) {
                viewModel.addInvoiceAttachments(listOf(att.copy(invoiceId = invoice.id)))
                Toast.makeText(context, "Image attached successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to attach image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val detailPdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val att = AttachmentUtils.saveUriToInternalStorage(context, it, forcedPdf = true)
            if (att != null) {
                viewModel.addInvoiceAttachments(listOf(att.copy(invoiceId = invoice.id)))
                Toast.makeText(context, "PDF attached successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to attach PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sales Invoice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textPrimary
                        )
                        Text(
                            text = invoice.invoiceNumber,
                            fontSize = 12.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("invoice_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    // Quick Print / Export Modal
                    IconButton(
                        onClick = { showPrintExportModal = true },
                        modifier = Modifier.testTag("quick_print_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print / Export", tint = textPrimary)
                    }

                    // Quick Share Action
                    IconButton(
                        onClick = { doShare(false) },
                        modifier = Modifier.testTag("quick_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = textPrimary)
                    }

                    // Overflow More Menu
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.testTag("invoice_more_menu_button")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = textPrimary)
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Print / Export Options") },
                                leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    showPrintExportModal = true
                                },
                                modifier = Modifier.testTag("menu_print_options")
                            )

                            DropdownMenuItem(
                                text = { Text("Download A4 PDF") },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    doDownload(false)
                                },
                                modifier = Modifier.testTag("menu_download_a4")
                            )

                            DropdownMenuItem(
                                text = { Text("Download POS Receipt") },
                                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    doDownload(true)
                                },
                                modifier = Modifier.testTag("menu_download_pos")
                            )

                            DropdownMenuItem(
                                text = { Text("Preview Invoice PDF") },
                                leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    doPreview(false)
                                },
                                modifier = Modifier.testTag("menu_preview_pdf")
                            )

                            HorizontalDivider()

                            if (invoiceSettings.allowEditSavedInvoice && invoiceSettings.permissions.canEditInvoice && (!invoiceSettings.lockPaidInvoices || invoice.dueAmount > 0)) {
                                DropdownMenuItem(
                                    text = { Text("Edit Invoice") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.openDialog(ActiveDialog.SALES_INVOICE)
                                        Toast.makeText(context, "Opening Sales Invoice Editor", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("menu_edit_invoice")
                                )
                            }

                            if (invoice.dueAmount > 0 && invoice.invoiceStatus != "Cancelled") {
                                DropdownMenuItem(
                                    text = { Text("Record Payment") },
                                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showRecordPaymentDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_record_payment")
                                )
                            }

                            if (invoiceSettings.allowDuplicateInvoice) {
                                DropdownMenuItem(
                                    text = { Text("Duplicate Invoice") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.duplicateSalesInvoice(invoice.id) {
                                            Toast.makeText(context, "Invoice duplicated as Draft", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("menu_duplicate_invoice")
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("View Party Account") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    if (invoice.partyId != null) {
                                        viewModel.selectPartyForDetail(invoice.partyId)
                                        viewModel.setTab(NavTab.PARTIES)
                                        viewModel.selectInvoiceForDetail(null)
                                    } else {
                                        Toast.makeText(context, "Direct cash sale without linked party", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("menu_view_party")
                            )

                            if (invoice.invoiceStatus != "Cancelled" && invoiceSettings.permissions.canCancelInvoice) {
                                DropdownMenuItem(
                                    text = { Text("Cancel Invoice", color = Color(0xFFDC2626)) },
                                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626)) },
                                    onClick = {
                                        showMoreMenu = false
                                        if (invoiceSettings.confirmBeforeCancel) {
                                            showCancelConfirmDialog = true
                                        } else {
                                            viewModel.cancelSalesInvoice(invoice.id) {
                                                Toast.makeText(context, "Invoice cancelled", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("menu_cancel_invoice")
                                )
                            }

                            if (invoiceSettings.allowDeleteSavedInvoice && invoiceSettings.permissions.canDeleteInvoice) {
                                DropdownMenuItem(
                                    text = { Text("Delete Invoice", color = Color(0xFFDC2626)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                                    onClick = {
                                        showMoreMenu = false
                                        if (invoiceSettings.confirmBeforeDelete) {
                                            showDeleteConfirmDialog = true
                                        } else {
                                            viewModel.deleteSalesInvoice(invoice.id) {
                                                Toast.makeText(context, "Invoice deleted", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("menu_delete_invoice")
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBg,
                    titleContentColor = textPrimary,
                    navigationIconContentColor = textPrimary
                )
            )
        },
        bottomBar = {
            // Persistent Bottom Action Bar
            Surface(
                color = cardBg,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (invoice.invoiceStatus != "Cancelled") {
                        if (invoice.dueAmount > 0) {
                            Button(
                                onClick = { showRecordPaymentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("record_payment_bottom_button")
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(17.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Pay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showRecordPaymentDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                                border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("record_payment_bottom_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp), tint = Color(0xFF16A34A))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fully Paid", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showPrintExportModal = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.95f)
                            .height(46.dp)
                            .testTag("bottom_print_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { doDownload(false) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.95f)
                            .height(46.dp)
                            .testTag("bottom_download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    Button(
                        onClick = { doShare(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.95f)
                            .height(46.dp)
                            .testTag("bottom_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }
        },
        containerColor = screenBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("invoice_detail_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // =================================================================
            // 1. BUSINESS HEADER CARD (Section 2)
            // =================================================================
            item {
                BusinessHeaderCard(
                    profile = effectiveBusiness,
                    isTaxInvoice = invoice.isTaxInvoice,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // =================================================================
            // 2. INVOICE SUMMARY CARD (Section 3)
            // =================================================================
            item {
                InvoiceSummaryCard(
                    invoice = invoice,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onCopyNumber = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Invoice Number", invoice.invoiceNumber)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Invoice number copied", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // =================================================================
            // 3. CUSTOMER / PARTY SECTION (Section 4)
            // =================================================================
            item {
                CustomerPartyCard(
                    party = party,
                    partyNameSnapshot = invoice.partyNameSnapshot,
                    partyPhoneSnapshot = invoice.partyPhoneSnapshot,
                    partyAddressSnapshot = invoice.partyAddressSnapshot,
                    partyPanSnapshot = invoice.partyPanSnapshot,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onViewPartyAccount = {
                        if (invoice.partyId != null) {
                            viewModel.selectPartyForDetail(invoice.partyId)
                            viewModel.setTab(NavTab.PARTIES)
                            viewModel.selectInvoiceForDetail(null)
                        } else {
                            Toast.makeText(context, "Customer information unavailable for this direct sale", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCallParty = { phone ->
                        if (!phone.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    }
                )
            }

            // =================================================================
            // 4. ITEM-WISE INVOICE SECTION (Section 5)
            // =================================================================
            item {
                Text(
                    text = "Invoice Items (${items.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_items_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No item details available for this invoice.",
                                color = textSecondary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(items) { itemEntity ->
                    val index = items.indexOf(itemEntity) + 1
                    InvoiceItemCard(
                        item = itemEntity,
                        serialNumber = index,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }

            // =================================================================
            // 5. FINANCIAL SUMMARY (Section 6)
            // =================================================================
            item {
                FinancialSummaryCard(
                    invoice = invoice,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // =================================================================
            // 6. PAYMENT DETAILS & ALLOCATION HISTORY (Section 7)
            // =================================================================
            item {
                PaymentDetailsCard(
                    invoice = invoice,
                    payments = payments,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onRecordPaymentClick = { showRecordPaymentDialog = true }
                )
            }

            // =================================================================
            // 7. ADDITIONAL INFORMATION (Section 8)
            // =================================================================
            item {
                AdditionalInfoCard(
                    invoice = invoice,
                    terms = effectiveBusiness?.invoiceTerms,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // =================================================================
            // 8. INVOICE ACTIVITY AUDIT TRAIL (Section 9)
            // =================================================================
            item {
                InvoiceActivityCard(
                    activities = activities,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // =================================================================
            // 9. ATTACHMENTS (Images & PDFs)
            // =================================================================
            item {
                InvoiceAttachmentsCard(
                    attachments = attachments,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    primaryColor = primaryColor,
                    onAddImage = { detailImagePickerLauncher.launch("image/*") },
                    onAddPdf = { detailPdfPickerLauncher.launch("application/pdf") },
                    onPreviewImage = { previewAttachment = it },
                    onOpenPdf = { AttachmentUtils.openAttachment(context, it) },
                    onShareAttachment = { AttachmentUtils.shareAttachment(context, it) },
                    onDeleteAttachment = { attachmentToDelete = it }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // =========================================================================
    // DIALOGS & CONFIRMATIONS
    // =========================================================================

    // Attachment Image Preview Dialog
    if (previewAttachment != null) {
        val att = previewAttachment!!
        Dialog(onDismissRequest = { previewAttachment = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = cardBg,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = att.fileName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(
                                onClick = { AttachmentUtils.shareAttachment(context, att) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = textSecondary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { previewAttachment = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val file = File(att.uriString)
                        AsyncImage(
                            model = file,
                            contentDescription = att.fileName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AttachmentUtils.formatFileSize(att.fileSize),
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                        OutlinedButton(
                            onClick = { previewAttachment = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Close", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Attachment Delete Confirmation Dialog
    if (attachmentToDelete != null) {
        val att = attachmentToDelete!!
        AlertDialog(
            onDismissRequest = { attachmentToDelete = null },
            title = { Text("Delete Attachment", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove \"${att.fileName}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAttachment(att)
                        attachmentToDelete = null
                        Toast.makeText(context, "Attachment deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { attachmentToDelete = null }) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            invoice = invoice,
            onDismiss = { showRecordPaymentDialog = false },
            onSubmit = { payment ->
                viewModel.recordSalesInvoicePayment(
                    payment = payment,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Payment of Rs. ${formatCurrency(payment.paymentAmount)} recorded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        showRecordPaymentDialog = false
                    },
                    onError = { err ->
                        Toast.makeText(context, "Payment error: $err", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Cancel Invoice Dialog
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Cancel Invoice", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to cancel invoice #${invoice.invoiceNumber}? This will mark the invoice status as Cancelled and record an audit log.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelSalesInvoice(invoice.id) {
                            Toast.makeText(context, "Invoice cancelled", Toast.LENGTH_SHORT).show()
                            showCancelConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_cancel_invoice_button")
                ) {
                    Text("Confirm Cancel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Delete Invoice Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Invoice", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete invoice #${invoice.invoiceNumber} permanently? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSalesInvoice(invoice.id) {
                            Toast.makeText(context, "Invoice deleted", Toast.LENGTH_SHORT).show()
                            showDeleteConfirmDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_delete_invoice_button")
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Print / Export Format Selection Dialog
    if (showPrintExportModal) {
        AlertDialog(
            onDismissRequest = { showPrintExportModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Print & Export Invoice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Choose a document layout format for invoice #${invoice.invoiceNumber}:",
                        fontSize = 13.sp,
                        color = textSecondary
                    )

                    // Card 1: Official A4 Tax Invoice
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF3B82F6) else Color(0xFFBFDBFE))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "A4 Tax Invoice",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimary
                                    )
                                }
                                Surface(
                                    color = if (isDarkMode) Color(0xFF1E3A8A) else Color(0xFFDBEAFE),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Official A4",
                                        color = primaryColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Standard multi-page printable legal document with business crest, PAN/VAT, customer snapshot & terms.",
                                fontSize = 11.5.sp,
                                color = textSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showPrintExportModal = false
                                        doPrint(false)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_print_a4")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Print", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        showPrintExportModal = false
                                        doDownload(false)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_download_a4")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = primaryColor)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Save", fontSize = 11.5.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        showPrintExportModal = false
                                        doShare(false)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_share_a4")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = primaryColor)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Share", fontSize = 11.5.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        showPrintExportModal = false
                                        doPreview(false)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_preview_a4")
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = textSecondary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("View", fontSize = 11.5.sp, color = textSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Card 2: 80mm POS Thermal Receipt
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF10B981) else Color(0xFFA7F3D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "80mm POS Receipt",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimary
                                    )
                                }
                                Surface(
                                    color = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFD1FAE5),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Thermal Slip",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Compact continuous roll receipt layout optimized for counter sales and fast thermal POS printers.",
                                fontSize = 11.5.sp,
                                color = textSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showPrintExportModal = false
                                        doPrint(true)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_print_pos")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Print", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        showPrintExportModal = false
                                        doDownload(true)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_download_pos")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Save", fontSize = 11.5.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        showPrintExportModal = false
                                        doShare(true)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_share_pos")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Share", fontSize = 11.5.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        showPrintExportModal = false
                                        doPreview(true)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_preview_pos")
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = textSecondary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("View", fontSize = 11.5.sp, color = textSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showPrintExportModal = false },
                    modifier = Modifier.testTag("close_print_export_dialog_button")
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = primaryColor)
                }
            }
        )
    }
}

// =============================================================================
// SUB-COMPONENTS
// =============================================================================

@Composable
private fun BusinessHeaderCard(
    profile: com.example.data.local.entity.BusinessProfileEntity?,
    isTaxInvoice: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("business_header_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Business Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val bizName = profile?.businessName?.ifBlank { "Atri Nova Tech Enterprises" } ?: "Atri Nova Tech Enterprises"
                    Text(
                        text = bizName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = textPrimary
                    )

                    val bizAddress = profile?.address?.ifBlank { null }
                    if (!bizAddress.isNullOrBlank()) {
                        Text(
                            text = bizAddress,
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }

                // Invoice Classification Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isTaxInvoice) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTaxInvoice) "TAX INVOICE" else "BILL OF SUPPLY",
                        color = if (isTaxInvoice) Color(0xFF1D4ED8) else textSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val panVat = profile?.panVatNumber?.ifBlank { null }
            val phone = profile?.phone?.ifBlank { null }
            val email = profile?.email?.ifBlank { null }

            if (panVat != null || phone != null || email != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = cardBorder, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (panVat != null) {
                        Text("PAN: $panVat", fontSize = 11.5.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                    }
                    if (phone != null) {
                        Text("Tel: $phone", fontSize = 11.5.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                    }
                    if (email != null) {
                        Text(email, fontSize = 11.5.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceSummaryCard(
    invoice: SalesInvoiceEntity,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    onCopyNumber: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_summary_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Invoice Number + Copy Chip + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCopyNumber() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = invoice.invoiceNumber,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Invoice Number",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Invoice Status Badge
                    StatusBadge(status = invoice.invoiceStatus)
                    // Payment Status Badge
                    PaymentStatusBadge(status = invoice.paymentStatus)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Calendar Dates Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBorder.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nepali Date (BS)", fontSize = 10.5.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
                    Text(invoice.displayBsDate, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(cardBorder)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text("Gregorian Date (AD)", fontSize = 10.5.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
                    Text(invoice.displayAdDate, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Totals Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Grand Total", fontSize = 11.5.sp, color = textSecondary)
                    Text(
                        text = formatCurrency(invoice.grandTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Paid Amount", fontSize = 11.5.sp, color = textSecondary)
                    Text(
                        text = formatCurrency(invoice.paidAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Due Amount", fontSize = 11.5.sp, color = textSecondary)
                    Text(
                        text = formatCurrency(invoice.dueAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.dueAmount > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerPartyCard(
    party: com.example.data.local.entity.PartyEntity?,
    partyNameSnapshot: String,
    partyPhoneSnapshot: String?,
    partyAddressSnapshot: String?,
    partyPanSnapshot: String?,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    onViewPartyAccount: () -> Unit,
    onCallParty: (String?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_party_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Customer Details",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                if (party != null) {
                    TextButton(
                        onClick = onViewPartyAccount,
                        modifier = Modifier.testTag("view_party_account_button")
                    ) {
                        Text("View Account", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val displayName = partyNameSnapshot.ifBlank { party?.name ?: "Customer information unavailable" }
            val phone = partyPhoneSnapshot?.ifBlank { null } ?: party?.phone
            val address = partyAddressSnapshot?.ifBlank { null } ?: party?.address
            val pan = partyPanSnapshot?.ifBlank { null } ?: party?.panVatNumber

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    if (!phone.isNullOrBlank()) {
                        Text(
                            text = "Phone: $phone",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                    if (!address.isNullOrBlank()) {
                        Text(
                            text = "Address: $address",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                    if (!pan.isNullOrBlank()) {
                        Text(
                            text = "PAN/VAT: $pan",
                            fontSize = 12.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (!phone.isNullOrBlank()) {
                    IconButton(
                        onClick = { onCallParty(phone) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7))
                            .testTag("call_party_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Customer",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceItemCard(
    item: SalesInvoiceItemEntity,
    serialNumber: Int,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_item_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // SN badge
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$serialNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = item.productNameSnapshot,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        if (!item.productCodeSnapshot.isNullOrBlank()) {
                            Text(
                                text = "Code: ${item.productCodeSnapshot}",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                // Line Total
                Text(
                    text = formatCurrency(item.lineTotal),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing & Quantity Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.quantity} ${item.unit} × ${formatCurrency(item.rate)}",
                    fontSize = 12.sp,
                    color = textSecondary
                )

                if (item.discount > 0) {
                    Text(
                        text = "Disc: -${formatCurrency(item.discount)}",
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (item.vatRate > 0) {
                    Text(
                        text = "VAT (${item.vatRate}%): ${formatCurrency(item.vatAmount)}",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }

            // Batch and Expiry Chips if present
            if (!item.batchNumber.isNullOrBlank() || !item.expiryDate.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!item.batchNumber.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(cardBorder.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Batch: ${item.batchNumber}", fontSize = 10.sp, color = textSecondary)
                        }
                    }
                    if (!item.expiryDate.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(cardBorder.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Exp: ${item.expiryDate}", fontSize = 10.sp, color = textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialSummaryCard(
    invoice: SalesInvoiceEntity,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("financial_summary_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Financial Summary",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            SummaryRow("Subtotal", formatCurrency(invoice.subtotal), textSecondary, textPrimary)

            if (invoice.discountAmount > 0) {
                val discLabel = if (invoice.discountPercent > 0) "Discount (${invoice.discountPercent}%)" else "Discount"
                SummaryRow(discLabel, "-${formatCurrency(invoice.discountAmount)}", textSecondary, Color(0xFFDC2626))
            }

            if (invoice.vatAmount > 0) {
                SummaryRow("Taxable Amount", formatCurrency(invoice.taxableAmount), textSecondary, textPrimary)
                SummaryRow("VAT (${invoice.vatRate.toInt()}%)", "+${formatCurrency(invoice.vatAmount)}", textSecondary, textPrimary)
            }

            if (invoice.extraChargeAmount > 0) {
                val chargeLabel = invoice.extraChargeDescription?.ifBlank { "Extra Charge" } ?: "Extra Charge"
                SummaryRow(chargeLabel, "+${formatCurrency(invoice.extraChargeAmount)}", textSecondary, textPrimary)
            }

            if (invoice.roundOff != 0.0) {
                val sign = if (invoice.roundOff > 0) "+" else ""
                SummaryRow("Round Off", "$sign${formatCurrency(invoice.roundOff)}", textSecondary, textPrimary)
            }

            HorizontalDivider(color = cardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            SummaryRow("Grand Total", formatCurrency(invoice.grandTotal), textPrimary, Color(0xFF2563EB), isBold = true, fontSize = 15.sp)
            SummaryRow("Paid Amount", formatCurrency(invoice.paidAmount), textSecondary, Color(0xFF16A34A))
            SummaryRow("Due Balance", formatCurrency(invoice.dueAmount), textPrimary, if (invoice.dueAmount > 0) Color(0xFFDC2626) else Color(0xFF16A34A), isBold = true, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PaymentDetailsCard(
    invoice: SalesInvoiceEntity,
    payments: List<PaymentAllocationEntity>,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    onRecordPaymentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_details_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment & Settlement",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                if (invoice.invoiceStatus != "Cancelled") {
                    if (invoice.dueAmount > 0) {
                        TextButton(
                            onClick = onRecordPaymentClick,
                            modifier = Modifier.testTag("record_payment_card_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF16A34A))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Pay", color = Color(0xFF16A34A), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("fully_paid_card_badge")
                        ) {
                            Text(text = "Fully Settled", color = Color(0xFF15803D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            SummaryRow("Payment Method", invoice.paymentMethod, textSecondary, textPrimary)
            if (!invoice.paymentAccountId.isNullOrBlank()) {
                SummaryRow("Cash / Bank Account", invoice.paymentAccountId, textSecondary, textPrimary)
            }
            if (!invoice.dueDateBS.isNullOrBlank() || !invoice.dueDateAD.isNullOrBlank()) {
                val dueStr = "${invoice.dueDateBS ?: ""} ${if (!invoice.dueDateAD.isNullOrBlank()) "(${invoice.dueDateAD})" else ""}".trim()
                SummaryRow("Payment Due Date", dueStr, textSecondary, textPrimary)
            }

            // Payment Allocation History Section
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = cardBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment History (${payments.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                    modifier = Modifier.testTag("payment_history_header")
                )
                if (payments.isNotEmpty()) {
                    Text(
                        text = "Total Paid: ${formatCurrency(payments.sumOf { it.paymentAmount })}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (payments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC).copy(alpha = 0.6f))
                        .border(1.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .testTag("empty_payments_notice"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = textSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No payments recorded yet. Record a payment to settle this invoice.",
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_history_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    payments.forEachIndexed { index, pay ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9).copy(alpha = 0.5f))
                                .padding(10.dp)
                                .testTag("payment_history_item_${pay.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pay.paymentMethod,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    if (!pay.paymentAccountId.isNullOrBlank()) {
                                        Text(
                                            text = " • ${pay.paymentAccountId}",
                                            fontSize = 11.5.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                                val dateStr = pay.displayBsDate.ifBlank { pay.displayAdDate }
                                Text(
                                    text = dateStr + if (pay.displayAdDate.isNotBlank() && pay.displayBsDate.isNotBlank()) " (${pay.displayAdDate})" else "",
                                    fontSize = 10.5.sp,
                                    color = textSecondary
                                )
                                if (!pay.transactionReference.isNullOrBlank()) {
                                    Text(
                                        text = "Ref: ${pay.transactionReference}",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (!pay.remarks.isNullOrBlank()) {
                                    Text(
                                        text = "Note: ${pay.remarks}",
                                        fontSize = 10.5.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrency(pay.paymentAmount),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = "Settled",
                                        color = Color(0xFF15803D),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdditionalInfoCard(
    invoice: SalesInvoiceEntity,
    terms: String?,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val hasSalesperson = !invoice.salespersonName.isNullOrBlank()
    val hasRef = !invoice.referenceNumber.isNullOrBlank()
    val hasRemarks = !invoice.remarks.isNullOrBlank()
    val hasCreatedBy = invoice.createdBy.isNotBlank()
    val hasTerms = !terms.isNullOrBlank()

    if (!hasSalesperson && !hasRef && !hasRemarks && !hasCreatedBy && !hasTerms) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("additional_info_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Additional Information",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            if (hasSalesperson) {
                SummaryRow("Salesperson", invoice.salespersonName ?: "", textSecondary, textPrimary)
            }
            if (hasRef) {
                SummaryRow("Reference No.", invoice.referenceNumber ?: "", textSecondary, textPrimary)
            }
            if (hasCreatedBy) {
                SummaryRow("Created By", invoice.createdBy, textSecondary, textPrimary)
            }
            if (invoice.createdAt > 0) {
                val createdStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(invoice.createdAt))
                SummaryRow("Created On", createdStr, textSecondary, textPrimary)
            }
            if (hasRemarks) {
                SummaryRow("Remarks / Note", invoice.remarks ?: "", textSecondary, textPrimary)
            }
            if (hasTerms) {
                SummaryRow("Terms", terms ?: "", textSecondary, textPrimary)
            }
        }
    }
}

@Composable
private fun InvoiceActivityCard(
    activities: List<InvoiceActivityEntity>,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_activity_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Audit Trail & Activity (${activities.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activities.isEmpty()) {
                Text(
                    text = "No prior activity entries recorded.",
                    fontSize = 12.5.sp,
                    color = textSecondary
                )
            } else {
                activities.forEach { act ->
                    val timeStr = remember(act.timestampMillis) {
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(act.timestampMillis))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                                .padding(top = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = act.description,
                                fontSize = 12.sp,
                                color = textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$timeStr • ${act.performedBy}",
                                fontSize = 10.5.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// RECORD PAYMENT DIALOG & DATE PICKER
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPaymentDialog(
    invoice: SalesInvoiceEntity,
    onDismiss: () -> Unit,
    onSubmit: (PaymentAllocationEntity) -> Unit
) {
    var amountText by remember {
        mutableStateOf(if (invoice.dueAmount > 0) String.format(Locale.US, "%.2f", invoice.dueAmount) else "")
    }
    var selectedAccount by remember { mutableStateOf<String?>("Cash-in-Hand") }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var referenceText by remember { mutableStateOf("") }
    var remarksText by remember { mutableStateOf("") }
    var allowOverpayment by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Date state
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDateBs by remember {
        mutableStateOf(NepaliDateUtils.formatBsDate(System.currentTimeMillis()))
    }
    var selectedDateAd by remember {
        mutableStateOf(NepaliDateUtils.formatAdDate(System.currentTimeMillis()))
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    if (showDatePicker) {
        RecordPaymentDatePickerDialog(
            currentMillis = selectedDateMillis,
            onDateSelected = { millis, bs, ad ->
                selectedDateMillis = millis
                selectedDateBs = bs
                selectedDateAd = ad
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Payments,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Record Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Balance Status Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (invoice.dueAmount <= 0.0) Color(0xFFDCFCE7).copy(alpha = 0.5f) else Color(0xFFFEF2F2)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (invoice.dueAmount <= 0.0) Color(0xFF86EFAC) else Color(0xFFFECACA)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Invoice #${invoice.invoiceNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = if (invoice.dueAmount <= 0.0) "Fully Paid" else "Pending Due",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (invoice.dueAmount <= 0.0) Color(0xFF15803D) else Color(0xFFDC2626)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total: ${formatCurrency(invoice.grandTotal)}", fontSize = 11.5.sp, color = textSecondary)
                            Text("Paid: ${formatCurrency(invoice.paidAmount)}", fontSize = 11.5.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Due:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(
                                text = formatCurrency(invoice.dueAmount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (invoice.dueAmount > 0) Color(0xFFDC2626) else Color(0xFF15803D)
                            )
                        }
                        if (invoice.dueAmount <= 0.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notice: Invoice is already fully paid (Due: Rs. 0.00).",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // 2. Amount Input
                Column {
                    Text(
                        text = "Payment Amount (Rs.) *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMessage = null
                        },
                        label = { Text("Amount") },
                        placeholder = { Text("Enter payment amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_amount_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Quick Chips (Full Due / 50% Due)
                    if (invoice.dueAmount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    amountText = String.format(Locale.US, "%.2f", invoice.dueAmount)
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("quick_full_due_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Full Due: ${formatCurrency(invoice.dueAmount)}", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    amountText = String.format(Locale.US, "%.2f", invoice.dueAmount / 2)
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("quick_half_due_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("50%: ${formatCurrency(invoice.dueAmount / 2)}", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // 3. Select Cash/Bank Account (Requirement 2 & Validate Missing Account)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deposit To Account (Cash/Bank) *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        if (selectedAccount != null) {
                            TextButton(
                                onClick = {
                                    selectedAccount = null
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("clear_account_button"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Deselect", fontSize = 10.5.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val accounts = listOf(
                        Triple("Cash-in-Hand", "Cash-in-Hand (Physical Counter)", "Cash"),
                        Triple("Nabil Bank Ltd.", "Nabil Bank Ltd. (A/C: 0100145228001)", "Bank Transfer"),
                        Triple("Global IME Bank", "Global IME Bank (A/C: 1120038891001)", "Bank Transfer"),
                        Triple("Digital Wallet (eSewa / Fonepay)", "Digital Wallet (eSewa / Fonepay QR)", "UPI / Online"),
                        Triple("Bank Cheque Account", "Bank Cheque / Clearing Account", "Cheque")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        accounts.forEach { (accId, label, method) ->
                            val isSelected = selectedAccount == accId
                            val tag = "account_option_" + accId.replace(" ", "_").replace("/", "_").replace("(", "").replace(")", "").lowercase()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.12f) else if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF2563EB) else borderCol,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedAccount = accId
                                        selectedMethod = method
                                        errorMessage = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .testTag(tag),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (method) {
                                            "Cash" -> Icons.Default.Payments
                                            "Bank Transfer", "Cheque" -> Icons.Default.AccountBalance
                                            else -> Icons.Default.Receipt
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) Color(0xFF2563EB) else textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF2563EB) else textPrimary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Payment Date Selection (Requirement 4)
                Column {
                    Text(
                        text = "Payment Date (BS / AD)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("select_payment_date_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$selectedDateBs BS ($selectedDateAd AD)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }
                        Text(
                            text = "Change",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                // 5. Reference & Remarks (Requirement 5)
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Transaction Reference / UTR / Cheque (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_reference_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("Payment Remarks / Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_remarks_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // 6. Overpayment Allowance Checkbox (Requirement 12)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { allowOverpayment = !allowOverpayment },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = allowOverpayment,
                        onCheckedChange = { allowOverpayment = it },
                        modifier = Modifier.testTag("allow_overpayment_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Allow overpayment / advance credit",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                }

                // 7. Error Message
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .testTag("payment_error_message")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSubmitting) return@Button
                    errorMessage = null

                    // Validation: Missing account
                    if (selectedAccount.isNullOrBlank()) {
                        errorMessage = "Please select a Cash/Bank account"
                        return@Button
                    }

                    // Validation: Zero / Negative / Invalid amount
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Payment amount must be greater than Rs. 0.00"
                        return@Button
                    }

                    // Validation: Already paid invoice without overpayment allowed
                    if (invoice.dueAmount <= 0.0 && !allowOverpayment) {
                        errorMessage = "This invoice is already fully paid (Due: Rs. 0.00). Check 'Allow overpayment' to proceed."
                        return@Button
                    }

                    // Validation: Overpayment without allowOverpayment checked
                    if (invoice.dueAmount > 0.0 && amt > invoice.dueAmount && !allowOverpayment) {
                        errorMessage = "Payment amount (${formatCurrency(amt)}) exceeds remaining due (${formatCurrency(invoice.dueAmount)}). Check 'Allow overpayment' to proceed."
                        return@Button
                    }

                    isSubmitting = true
                    val payment = PaymentAllocationEntity(
                        invoiceId = invoice.id,
                        partyId = invoice.partyId,
                        paymentAmount = amt,
                        paymentDateMillis = selectedDateMillis,
                        paymentDateBS = selectedDateBs,
                        paymentDateAD = selectedDateAd,
                        paymentMethod = selectedMethod,
                        paymentAccountId = selectedAccount,
                        transactionReference = referenceText.trim().ifBlank { null },
                        remarks = remarksText.trim().ifBlank { null },
                        recordedBy = "Admin",
                        createdAt = System.currentTimeMillis()
                    )
                    onSubmit(payment)
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_payment_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recording...", color = Color.White)
                } else {
                    Text("Confirm Payment", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
                modifier = Modifier.testTag("dismiss_payment_dialog_button")
            ) {
                Text("Cancel", color = textSecondary)
            }
        }
    )
}

@Composable
private fun RecordPaymentDatePickerDialog(
    currentMillis: Long,
    onDateSelected: (Long, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val nepaliDate = remember(currentMillis) { NepaliDateUtils.adToBs(currentMillis) }
    var tempYear by remember { mutableIntStateOf(nepaliDate.year) }
    var tempMonth by remember { mutableIntStateOf(nepaliDate.month) }
    var tempDay by remember { mutableIntStateOf(nepaliDate.day) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Payment Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val calculatedMillis = remember(tempYear, tempMonth, tempDay) {
                    NepaliDateUtils.bsToAd(tempYear, tempMonth, tempDay)
                }
                val previewBs = remember(tempYear, tempMonth, tempDay) {
                    String.format(Locale.US, "%04d/%02d/%02d", tempYear, tempMonth, tempDay)
                }
                val previewAd = remember(calculatedMillis) {
                    NepaliDateUtils.formatAdDate(calculatedMillis)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$previewBs BS",
                            color = Color(0xFF38BDF8),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$previewAd AD",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PaymentStepperColumn(
                        label = "BS Year",
                        value = tempYear.toString(),
                        onMinus = { if (tempYear > 2078) tempYear-- },
                        onPlus = { if (tempYear < 2086) tempYear++ }
                    )

                    PaymentStepperColumn(
                        label = "Month",
                        value = "${NepaliDateUtils.NEPALI_MONTHS_EN.getOrElse(tempMonth - 1) { "" }} ($tempMonth)",
                        onMinus = { if (tempMonth > 1) tempMonth-- else tempMonth = 12 },
                        onPlus = { if (tempMonth < 12) tempMonth++ else tempMonth = 1 }
                    )

                    val maxDays = NepaliDateUtils.getDaysInBsMonth(tempYear, tempMonth)
                    PaymentStepperColumn(
                        label = "Day",
                        value = tempDay.toString(),
                        onMinus = { if (tempDay > 1) tempDay-- else tempDay = maxDays },
                        onPlus = { if (tempDay < maxDays) tempDay++ else tempDay = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onDateSelected(calculatedMillis, previewBs, previewAd)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8),
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Apply Date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun PaymentStepperColumn(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        IconButton(
            onClick = onPlus,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF334155))
        ) {
            Icon(Icons.Default.ArrowDropUp, contentDescription = "Up", tint = Color(0xFF38BDF8))
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        IconButton(
            onClick = onMinus,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF334155))
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Down", tint = Color(0xFF38BDF8))
        }
    }
}

// =============================================================================
// UI HELPERS & BADGES
// =============================================================================

@Composable
private fun StatusBadge(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "paid", "confirmed" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "partially paid" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "draft" -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        "cancelled" -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
        else -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = status, color = text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PaymentStatusBadge(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "paid" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "partially paid" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "unpaid", "credit", "credit/due" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = status, color = text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = labelColor, fontSize = fontSize)
        Text(
            text = value,
            color = valueColor,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,##0.00")
    return "Rs. ${formatter.format(amount)}"
}

@Composable
private fun InvoiceAttachmentsCard(
    attachments: List<InvoiceAttachmentEntity>,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    onAddImage: () -> Unit,
    onAddPdf: () -> Unit,
    onPreviewImage: (InvoiceAttachmentEntity) -> Unit,
    onOpenPdf: (InvoiceAttachmentEntity) -> Unit,
    onShareAttachment: (InvoiceAttachmentEntity) -> Unit,
    onDeleteAttachment: (InvoiceAttachmentEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detail_attachments_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Attachments (${attachments.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onAddImage,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, primaryColor),
                        modifier = Modifier.height(32.dp).testTag("detail_btn_add_image")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = primaryColor, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Image", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                    }

                    OutlinedButton(
                        onClick = onAddPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        modifier = Modifier.height(32.dp).testTag("detail_btn_add_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ PDF", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (attachments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBorder.copy(alpha = 0.2f))
                        .padding(vertical = 18.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No attachments linked to this invoice.\nTap [+ Image] or [+ PDF] above to attach files.",
                        color = textSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    attachments.forEach { attachment ->
                        val isImage = attachment.fileType == "IMAGE"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBorder.copy(alpha = 0.15f))
                                .border(1.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isImage) onPreviewImage(attachment) else onOpenPdf(attachment)
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isImage) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(cardBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val file = File(attachment.uriString)
                                        AsyncImage(
                                            model = file,
                                            contentDescription = attachment.fileName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = attachment.fileName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isImage) "Image" else "PDF Document",
                                            fontSize = 11.sp,
                                            color = if (isImage) primaryColor else Color(0xFFDC2626),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = " • ${AttachmentUtils.formatFileSize(attachment.fileSize)}",
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (isImage) onPreviewImage(attachment) else onOpenPdf(attachment) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = "View", tint = textSecondary, modifier = Modifier.size(17.dp))
                                }
                                IconButton(
                                    onClick = { onShareAttachment(attachment) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = textSecondary, modifier = Modifier.size(17.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteAttachment(attachment) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(17.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
