package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite

@Composable
fun GoogleDriveBackupSheet(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val isConnected by viewModel.isGoogleAccountConnected.collectAsStateWithLifecycle()
    val connectedEmail by viewModel.connectedGoogleEmail.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
    val lastBackupSize by viewModel.lastBackupSize.collectAsStateWithLifecycle()
    val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val backupFrequency by viewModel.backupFrequency.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showFrequencyPicker by remember { mutableStateOf(false) }
    var showAccountSwitchDialog by remember { mutableStateOf(false) }
    var newAccountEmailInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("google_drive_backup_sheet")
    ) {
        // Sheet Header
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
                        .background(SkyBlueCardBg)
                        .border(1.dp, SkyBlueBright.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CloudSync,
                        contentDescription = null,
                        tint = SkyBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Google Drive Backup",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Encrypted SQLite cloud synchronization",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Jump to Firebase Cloud Storage Vault
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SkyBlueCardBg)
                .border(1.dp, SkyBlueBright.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable {
                    onClose()
                    viewModel.openDialog(com.example.ui.ActiveDialog.FIREBASE_CLOUD_BACKUP)
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("open_firebase_vault_from_gdrive_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = SkyBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Firebase Cloud Storage Vault",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Backup & restore double-entry ledger & configurations",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    }
                }
                Text("Open", color = SkyBlueBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 1: Google Account Connection Status Card (OAuth integration)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color(0xFF0F2A4A) else Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                tint = if (isConnected) SkyBlueBright else TextSubtle,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isConnected) "Connected Account" else "Account Disconnected",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isConnected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = SkyBlueBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isConnected) connectedEmail else "Sign in to enable cloud backup",
                                color = if (isConnected) SkyBlueBright else TextSubtle,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Connect / Switch Account Button
                    OutlinedButton(
                        onClick = {
                            if (isConnected) {
                                showAccountSwitchDialog = true
                            } else {
                                viewModel.connectGoogleAccount("business.admin@gmail.com")
                                Toast.makeText(context, "Connected to Google Drive", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SkyBlueBright
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(SkyBlueBright.copy(alpha = 0.5f))
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (isConnected) "Switch" else "Sign In",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isConnected) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Drive Folder: /AtriNova/Backups", color = TextSubtle, fontSize = 11.sp)
                        }
                        TextButton(
                            onClick = {
                                viewModel.disconnectGoogleAccount()
                                Toast.makeText(context, "Google Drive disconnected", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Disconnect", color = Color(0xFFFF6B6B), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Last Backup Info & Action Center
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Last Cloud Snapshot",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CloudDone, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = lastBackupTime,
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Snapshot Size: $lastBackupSize • SHA-256 Encrypted",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SkyBlueCardBg)
                            .border(1.dp, SkyBlueBright.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "UP TO DATE",
                            color = SkyBlueBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(16.dp))

                // "Backup Now" Glowing Pill-shaped Button
                Button(
                    onClick = {
                        if (!isConnected) {
                            Toast.makeText(context, "Please sign in to Google Drive first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.triggerGoogleDriveBackup()
                        Toast.makeText(context, "Cloud backup started in background...", Toast.LENGTH_SHORT).show()
                    },
                    enabled = !isBackingUp && isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("backup_now_button")
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encrypting & Uploading to Drive...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // "Restore from Drive" Button
                OutlinedButton(
                    onClick = {
                        if (!isConnected) {
                            Toast.makeText(context, "Please sign in to Google Drive first", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        showRestoreConfirmDialog = true
                    },
                    enabled = !isRestoring && isConnected,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SkyBlueBright
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SkyBlueBright.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restore_from_drive_button")
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            color = SkyBlueBright,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying & Restoring Database...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore from Drive Backup", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 3: Automation & Schedule Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Automated Cloud Schedules",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure hands-free background backups",
                    color = TextSubtle,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // Auto-backup toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Automatic Daily Backup", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Syncs silently on Wi-Fi connection", color = TextSubtle, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = autoBackupEnabled,
                        onCheckedChange = { viewModel.toggleAutoBackup(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SkyBlue,
                            uncheckedThumbColor = TextSubtle,
                            uncheckedTrackColor = BackgroundDark,
                            uncheckedBorderColor = CardBorder
                        )
                    )
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // Frequency Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFrequencyPicker = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Backup Frequency", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Currently: $backupFrequency", color = SkyBlueBright, fontSize = 12.sp)
                        }
                    }

                    Text("Change", color = SkyBlueBright, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // Security Encryption Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AES-256 Cloud Encryption", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Your database is encrypted before being sent to Google Drive", color = TextSubtle, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Confirmation Dialog for Database Restore
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "Confirm Cloud Restore",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Restoring from '$lastBackupTime' will merge cloud snapshots with your current offline database. Do you want to proceed?",
                    color = TextGrayLight,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        viewModel.triggerGoogleDriveRestore {
                            Toast.makeText(context, "Database successfully restored from Google Drive!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.Black)
                ) {
                    Text("Proceed Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Frequency Selector Dialog
    if (showFrequencyPicker) {
        val frequencies = listOf("Hourly", "Every 6 Hours", "Daily (Midnight)", "Weekly")
        AlertDialog(
            onDismissRequest = { showFrequencyPicker = false },
            containerColor = SurfaceDark,
            title = { Text("Select Backup Frequency", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    frequencies.forEach { freq ->
                        val isSelected = backupFrequency == freq
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setBackupFrequency(freq)
                                    showFrequencyPicker = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setBackupFrequency(freq)
                                    showFrequencyPicker = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyBlueBright, unselectedColor = TextSubtle)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(freq, color = TextWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyPicker = false }) {
                    Text("Close", color = SkyBlueBright)
                }
            }
        )
    }

    // Account Switcher Dialog
    if (showAccountSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showAccountSwitchDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Switch Google Account", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter the Google Account to sync backups with:", color = TextSubtle, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newAccountEmailInput,
                        onValueChange = { newAccountEmailInput = it },
                        placeholder = { Text("e.g. store.owner@gmail.com", color = TextSubtle) },
                        colors = staffTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAccountEmailInput.isNotBlank()) {
                            viewModel.connectGoogleAccount(newAccountEmailInput.trim())
                            Toast.makeText(context, "Account updated to $newAccountEmailInput", Toast.LENGTH_SHORT).show()
                        }
                        showAccountSwitchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountSwitchDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
