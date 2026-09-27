package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.backup.CloudBackupResult
import com.example.service.backup.CloudBackupSnapshot
import com.example.service.backup.CloudRestoreResult
import com.example.ui.MainViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.SettledBadgeBg
import com.example.ui.theme.SettledBadgeText
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite

@Composable
fun FirebaseCloudBackupScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val isBackingUp by viewModel.isFirebaseBackingUp.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isFirebaseRestoring.collectAsStateWithLifecycle()
    val lastBackupResult by viewModel.lastFirebaseBackupResult.collectAsStateWithLifecycle()
    val lastRestoreResult by viewModel.lastFirebaseRestoreResult.collectAsStateWithLifecycle()
    val snapshots by viewModel.firebaseSnapshots.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val backupFrequency by viewModel.backupFrequency.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Backup & Restore, 1: Snapshots History, 2: Auto-Sync Settings
    var showRestoreConfirmDialog by remember { mutableStateOf<CloudBackupSnapshot?>(null) }
    var showDirectRestoreDialog by remember { mutableStateOf(false) }
    var operationResultMessage by remember { mutableStateOf<String?>(null) }
    var showFrequencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshFirebaseSnapshots()
    }

    // SAF Launchers for File Export and Import
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackupToFile(uri) { success ->
                val msg = if (success) "Backup exported successfully to device storage!" else "Failed to export backup file"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                operationResultMessage = msg
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.restoreBackupFromFile(uri) { result ->
                val msg = if (result.isSuccess) {
                    "Restored ${result.restoredLedgerCount} ledger entries & configuration successfully!"
                } else {
                    "Import failed: ${result.message}"
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                operationResultMessage = msg
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("firebase_cloud_backup_screen")
    ) {
        // App Bar / Top Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SkyBlueCardBg)
                        .border(1.dp, SkyBlueBright.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Cloud Vault",
                        tint = SkyBlueBright,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Firebase Cloud Storage Vault",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Export & Restore Ledger & Settings Securely",
                        color = TextSubtle,
                        fontSize = 11.5.sp
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("firebase_backup_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextGrayLight
                )
            }
        }

        // Segmented Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = SkyBlueBright,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SkyBlueBright,
                    height = 2.5.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Backup & Restore", fontSize = 12.5.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    viewModel.refreshFirebaseSnapshots()
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Snapshots", fontSize = 12.5.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        if (snapshots.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SkyBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${snapshots.size}", color = SkyBlueBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Auto-Sync", fontSize = 12.5.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> BackupAndRestoreTab(
                viewModel = viewModel,
                isBackingUp = isBackingUp,
                isRestoring = isRestoring,
                lastBackupResult = lastBackupResult,
                userEmail = userSession?.email ?: "local.offline.vault@atrikhata.local",
                onExportCloud = {
                    viewModel.exportToFirebaseStorage { result ->
                        val msg = if (result.isSuccess) {
                            "Success! Exported ${result.ledgerCount} ledger entries & configuration settings to Firebase Storage vault."
                        } else {
                            "Backup failed: ${result.message}"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        operationResultMessage = msg
                    }
                },
                onRestoreCloud = {
                    showDirectRestoreDialog = true
                },
                onExportFile = {
                    exportFileLauncher.launch("atri_khata_backup_${System.currentTimeMillis()}.json")
                },
                onImportFile = {
                    importFileLauncher.launch(arrayOf("application/json", "text/*"))
                }
            )
            1 -> SnapshotsHistoryTab(
                snapshots = snapshots,
                isRestoring = isRestoring,
                onRefresh = { viewModel.refreshFirebaseSnapshots() },
                onRestoreSnapshot = { snapshot ->
                    showRestoreConfirmDialog = snapshot
                }
            )
            2 -> AutoSyncSettingsTab(
                autoBackupEnabled = autoBackupEnabled,
                backupFrequency = backupFrequency,
                onToggleAutoBackup = { viewModel.toggleAutoBackup(it) },
                onOpenFrequencyDialog = { showFrequencyDialog = true }
            )
        }
    }

    // Confirmation Dialog for Snapshot Restore
    if (showRestoreConfirmDialog != null) {
        val snapshot = showRestoreConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Restore Cloud Snapshot?",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "You are about to restore the backup snapshot from ${snapshot.formattedDate}.",
                        color = TextGrayLight,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ledger Entries:", color = TextSubtle, fontSize = 12.sp)
                                Text("${snapshot.ledgerEntriesCount} records", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Configurations:", color = TextSubtle, fontSize = 12.sp)
                                Text("${snapshot.configsCount} suites", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SHA-256 Checksum:", color = TextSubtle, fontSize = 12.sp)
                                Text(snapshot.sha256Checksum, color = SkyBlueBright, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Restoring will update your general ledger entries and restore saved business settings.",
                        color = TextSubtle,
                        fontSize = 11.5.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = snapshot.fileName
                        showRestoreConfirmDialog = null
                        viewModel.restoreFromFirebaseStorage(targetId) { result ->
                            val msg = if (result.isSuccess) {
                                "Restored ${result.restoredLedgerCount} ledger entries & configuration successfully!"
                            } else {
                                "Restore failed: ${result.message}"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            operationResultMessage = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    modifier = Modifier.testTag("confirm_restore_snapshot_button")
                ) {
                    Text("Restore Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = null }) {
                    Text("Cancel", color = TextSubtle)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Direct Restore Latest Confirmation Dialog
    if (showDirectRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showDirectRestoreDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = SkyBlueBright,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Restore Latest Cloud Backup?",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "This will download the most recent ledger snapshot and configuration settings from Firebase Storage. All existing ledger records and system settings will be verified against SHA-256 checksum and synchronized.",
                    color = TextGrayLight,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDirectRestoreDialog = false
                        viewModel.restoreFromFirebaseStorage { result ->
                            val msg = if (result.isSuccess) {
                                "Successfully restored ${result.restoredLedgerCount} ledger entries and all configuration settings!"
                            } else {
                                "Restore failed: ${result.message}"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            operationResultMessage = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    modifier = Modifier.testTag("confirm_direct_restore_button")
                ) {
                    Text("Restore Latest", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectRestoreDialog = false }) {
                    Text("Cancel", color = TextSubtle)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Operation Result Dialog
    if (operationResultMessage != null) {
        AlertDialog(
            onDismissRequest = { operationResultMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SkyBlueBright,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Vault Operation Complete",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = operationResultMessage ?: "",
                    color = TextGrayLight,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { operationResultMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Done")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Frequency Selector Dialog
    if (showFrequencyDialog) {
        val frequencies = listOf("On Every Transaction", "Daily (Midnight)", "Every 3 Days", "Weekly", "Manual Only")
        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = {
                Text("Select Auto-Backup Frequency", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    frequencies.forEach { freq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setBackupFrequency(freq)
                                    showFrequencyDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (freq == backupFrequency),
                                onClick = {
                                    viewModel.setBackupFrequency(freq)
                                    showFrequencyDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyBlueBright)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(freq, color = TextWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyDialog = false }) {
                    Text("Close", color = SkyBlueBright)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun BackupAndRestoreTab(
    viewModel: MainViewModel,
    isBackingUp: Boolean,
    isRestoring: Boolean,
    lastBackupResult: CloudBackupResult?,
    userEmail: String,
    onExportCloud: () -> Unit,
    onRestoreCloud: () -> Unit,
    onExportFile: () -> Unit,
    onImportFile: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Status & Encryption Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SkyBlueCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = SkyBlueBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Storage Vault",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SHA-256 Checksum & End-to-End Integrity",
                                    color = SettledBadgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SettledBadgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = SettledBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Connected Account:", color = TextSubtle, fontSize = 11.sp)
                            Text(userEmail, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Cloud Target:", color = TextSubtle, fontSize = 11.sp)
                            Text("Firebase Storage", color = SkyBlueBright, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (lastBackupResult != null && lastBackupResult.isSuccess) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Last Exported:", color = TextSubtle, fontSize = 11.sp)
                                    Text("${lastBackupResult.ledgerCount} ledger entries", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Fingerprint:", color = TextSubtle, fontSize = 11.sp)
                                    Text(lastBackupResult.sha256Checksum.take(12) + "...", color = SkyBlueBright, fontFamily = FontFamily.Monospace, fontSize = 10.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Primary Action Buttons Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cloud Backup Actions",
                        color = TextWhite,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upload complete ledger state & configuration to Firebase Storage",
                        color = TextSubtle,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Backup to Firebase Storage Button
                    Button(
                        onClick = onExportCloud,
                        enabled = !isBackingUp && !isRestoring,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_to_firebase_button")
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Exporting to Cloud...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export & Backup to Firebase Storage", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Restore from Firebase Storage Button
                    OutlinedButton(
                        onClick = onRestoreCloud,
                        enabled = !isBackingUp && !isRestoring,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlueBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restore_from_firebase_button")
                    ) {
                        if (isRestoring) {
                            CircularProgressIndicator(color = SkyBlueBright, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Restoring from Cloud...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore Latest from Firebase Storage", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Scope & Items Included Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Scope & Included Vault Assets",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ScopeItemRow(
                        icon = Icons.Outlined.AccountBalance,
                        title = "General Ledger (Double-Entry)",
                        subtitle = "Frappe GL entries, posting dates (BS/AD), debit/credit legs, vouchers"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ScopeItemRow(
                        icon = Icons.Outlined.Settings,
                        title = "System Configuration & Preferences",
                        subtitle = "AppSettings, VAT rates & PAN, transaction prefixes, invoice templates"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ScopeItemRow(
                        icon = Icons.Default.Storage,
                        title = "Master Entities & Party Ledgers",
                        subtitle = "Customer/supplier profiles, credit limits, inventory SKUs, sales staff"
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Local SAF File Export & Import Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Offline & File-based Vault Access",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Export standalone JSON backup file or restore from external storage",
                        color = TextSubtle,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportFile,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("export_backup_file_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export File", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onImportFile,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("import_backup_file_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import File", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SnapshotsHistoryTab(
    snapshots: List<CloudBackupSnapshot>,
    isRestoring: Boolean,
    onRefresh: () -> Unit,
    onRestoreSnapshot: (CloudBackupSnapshot) -> Unit
) {
    if (snapshots.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = TextSubtle,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Cloud Snapshots Stored Yet",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Perform an export to create your first encrypted ledger snapshot in Firebase Storage.",
                    color = TextSubtle,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Cloud Snapshots (${snapshots.size})",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SkyBlueBright, modifier = Modifier.size(18.dp))
                    }
                }
            }

            items(snapshots, key = { it.snapshotId }) { snapshot ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = SkyBlueBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = snapshot.formattedDate,
                                    color = TextWhite,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = snapshot.formattedSize,
                                color = TextSubtle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SkyBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${snapshot.ledgerEntriesCount} Ledger Entries",
                                    color = SkyBlueBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceDark)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Configs Saved",
                                    color = SettledBadgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = { onRestoreSnapshot(snapshot) },
                                enabled = !isRestoring,
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("restore_snapshot_${snapshot.snapshotId}")
                            ) {
                                Text("Restore", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SHA: ${snapshot.sha256Checksum}",
                            color = TextSubtle,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AutoSyncSettingsTab(
    autoBackupEnabled: Boolean,
    backupFrequency: String,
    onToggleAutoBackup: (Boolean) -> Unit,
    onOpenFrequencyDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automated Cloud Vault Backup",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Continuously snapshot ledger entries and business configuration to Firebase Storage in background",
                                color = TextSubtle,
                                fontSize = 11.5.sp
                            )
                        }
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = onToggleAutoBackup,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SkyBlue
                            ),
                            modifier = Modifier.testTag("toggle_auto_backup_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenFrequencyDialog)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Backup Schedule Frequency",
                                color = TextWhite,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = backupFrequency,
                                color = SkyBlueBright,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Security & Compliance Guarantee",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Atri Khata uses Frappe-standard double-entry bookkeeping safeguards. All ledger archives stored in Firebase Storage are hashed with SHA-256 before transport. When restoring on any new phone or device, the vault verifies transaction checksums and ledger balances (Total Debits == Total Credits) before applying changes into the local Room database.",
                        color = TextGrayLight,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScopeItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SkyBlueBright,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSubtle, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}
