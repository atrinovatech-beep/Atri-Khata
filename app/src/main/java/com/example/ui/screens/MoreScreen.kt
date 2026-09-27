package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ActiveDialog
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun MoreScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()
    val businessName by viewModel.selectedBusiness.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .testTag("more_screen_root")
    ) {
        // Top Header: Dark Blue Header with Business Name, Admin tag & Devices icon (matching image_7.png)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Brand Avatar with "Atri" Logo mark
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, SkyBlueBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Atri",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "TECH",
                            color = SkyBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp,
                            lineHeight = 7.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Business Name and Admin Role
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (businessName.isNotBlank()) businessName else session?.businessName ?: "Atri Khata Business",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = session?.email?.takeIf { it.isNotBlank() } ?: "Admin • Offline Ready",
                        color = SkyBlueBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Account & Cloud Sync button
                IconButton(
                    onClick = { viewModel.openDialog(ActiveDialog.USER_ACCOUNT_SYNC) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("more_account_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudSync,
                        contentDescription = "Account & Cloud Sync",
                        tint = SkyBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top Row Quick Utilities (Greeting Cards, Business Card, Reminders) matching image_7.png
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickUtilityItem(
                icon = Icons.Outlined.CardGiftcard,
                label = "Greeting\nCards",
                onClick = { Toast.makeText(context, "Opening Greeting Cards...", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f)
            )

            QuickUtilityItem(
                icon = Icons.Outlined.Badge,
                label = "Business\nCard",
                onClick = { Toast.makeText(context, "Opening Digital Business Card...", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f)
            )

            QuickUtilityItem(
                icon = Icons.Outlined.Event,
                label = "Reminders",
                onClick = { Toast.makeText(context, "Opening Scheduled Reminders...", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // First Section: Utilities Card Group (image_7.png)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    ModernMoreRow(
                        icon = Icons.Outlined.Person,
                        title = "My Account",
                        onClick = { viewModel.openDialog(ActiveDialog.BUSINESS_SWITCHER) }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.Subscriptions,
                        title = "Subscription",
                        onClick = { Toast.makeText(context, "Active: Enterprise Lifetime Tier", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.WorkspacePremium,
                        title = "Refer & Win",
                        hasNewBadge = true,
                        onClick = { viewModel.openDialog(ActiveDialog.REWARDS_WALLET) }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.Collections,
                        title = "Bill Gallery",
                        hasNewBadge = true,
                        onClick = { Toast.makeText(context, "Opening Bill & Invoice Gallery...", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.EditNote,
                        title = "Notebook",
                        hasNewBadge = true,
                        onClick = { Toast.makeText(context, "Opening Business Daily Notebook...", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section Title: "Management" (image_7.png & image_8.png)
        Text(
            text = "Management",
            color = TextSubtle,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // Second Section: Management Group (image_7.png / image_8.png)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    ModernMoreRow(
                        icon = Icons.Outlined.Storefront,
                        title = "Business Profile",
                        onClick = { viewModel.openDialog(ActiveDialog.BUSINESS_SWITCHER) }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.Chat,
                        title = "Credit Reminder",
                        hasNewBadge = true,
                        onClick = { Toast.makeText(context, "Automated Credit Reminders Active", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Staff Management Link: explicitly links to the advanced staff management system
                    ModernMoreRow(
                        icon = Icons.Outlined.Group,
                        title = "Manage Staff",
                        subtitle = "Roles, permissions & PIN access",
                        onClick = { viewModel.openDialog(ActiveDialog.MANAGE_STAFF) },
                        testTag = "more_manage_staff_row"
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Cash & Bank Accounts",
                        onClick = { viewModel.openDialog(ActiveDialog.ACCOUNT_TRANSFER) }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.Category,
                        title = "Manage Categories",
                        onClick = { Toast.makeText(context, "Categories: General, Printer, Hardware", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Reports & Analytics",
                        onClick = { viewModel.openDialog(ActiveDialog.VIEW_REPORTS) }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Settings Option: styled with Corporate Blue Theme, light-blue outline gear icon
                    ModernMoreRow(
                        icon = Icons.Outlined.Settings,
                        title = "Settings",
                        subtitle = "Preferences, regional currency, security & alerts",
                        onClick = { viewModel.openDialog(ActiveDialog.SETTINGS) },
                        testTag = "more_settings_row"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section Title: "App Settings & Privacy"
        Text(
            text = "App Settings & Privacy",
            color = TextSubtle,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // Privacy & Security Card
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    ModernMoreRow(
                        icon = Icons.Outlined.Settings,
                        title = "Configure App Settings",
                        subtitle = "Dark/Light theme, currency, PIN lock & notifications",
                        onClick = { viewModel.openDialog(ActiveDialog.SETTINGS) },
                        testTag = "more_configure_settings_row"
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Privacy Mode Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = if (privacyMode) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                contentDescription = null,
                                tint = SkyBlueBright,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text("Privacy Mode", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text("Mask balance amounts on screen", color = TextSubtle, fontSize = 12.sp)
                            }
                        }

                        Switch(
                            checked = privacyMode,
                            onCheckedChange = { viewModel.togglePrivacyMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SkyBlue
                            )
                        )
                    }

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.Security,
                        title = "App PIN & Security",
                        subtitle = "Configure 4-digit PIN & Biometrics",
                        onClick = { viewModel.openDialog(ActiveDialog.SETTINGS) }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.CloudSync,
                        title = "Google Drive Backup & Cloud Sync",
                        subtitle = "Automated AES-256 cloud backup & restore",
                        hasNewBadge = true,
                        onClick = { viewModel.openDialog(ActiveDialog.USER_ACCOUNT_SYNC) },
                        testTag = "more_backup_cloud_sync_row"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section Title: "Other"
        Text(
            text = "Other",
            color = TextSubtle,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // Bottom Section: About, Share, Rate App, Logout
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    ModernMoreRow(
                        icon = Icons.Outlined.Info,
                        title = "About Atri Nova Suite",
                        subtitle = "Version 2.4.0 (Enterprise)",
                        onClick = { Toast.makeText(context, "Atri Nova Business Suite v2.4.0", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.Share,
                        title = "Share App with Business Partners",
                        onClick = { Toast.makeText(context, "Sharing Atri Nova Business link...", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.StarRate,
                        title = "Rate App on Play Store",
                        onClick = { Toast.makeText(context, "Thank you for rating 5 stars!", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ModernMoreRow(
                        icon = Icons.Outlined.ExitToApp,
                        title = "Log Out",
                        iconTint = Color(0xFFFF5252),
                        onClick = { Toast.makeText(context, "Admin session preserved offline", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Atri Nova Business Suite", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Version 2.4.0 • Built with Google AI Studio", color = TextSubtle, fontSize = 11.sp)
        }
    }
}

/**
 * Top Quick Utility Item matching image_7.png (Greeting Cards, Business Card, Reminders)
 */
@Composable
private fun QuickUtilityItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SkyBlueCardBg)
                .border(1.dp, SkyBlueCardBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SkyBlueBright,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = TextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 15.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Modern Row item inside the card group matching image_7.png & image_8.png:
 * - Light-blue outline icon style
 * - Feature Title & optional Subtitle
 * - Contrasting Orange 'NEW' Badge for designated new features
 * - Right chevron arrow
 */
@Composable
private fun ModernMoreRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    hasNewBadge: Boolean = false,
    iconTint: Color = SkyBlueBright,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Outline Icon in Light Blue
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title and optional subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = TextSubtle,
                    fontSize = 12.sp
                )
            }
        }

        // Contrasting Orange "NEW" Badge (against dark blue background)
        if (hasNewBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE11D48))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "New",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Trailing Chevron arrow
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
