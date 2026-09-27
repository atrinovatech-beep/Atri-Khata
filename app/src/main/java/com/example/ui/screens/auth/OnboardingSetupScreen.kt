package com.example.ui.screens.auth

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.data.auth.AccountType
import com.example.ui.MainViewModel
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite

@Composable
fun OnboardingSetupScreen(
    viewModel: MainViewModel,
    userEmail: String,
    userName: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAccountType by remember { mutableStateOf(AccountType.BUSINESS) }
    var businessName by remember { mutableStateOf(if (userName.isNotBlank()) "$userName's Business" else "My Business") }
    var selectedBusinessType by remember { mutableStateOf("Retail Shop") }
    var panNumber by remember { mutableStateOf("") }
    var isVatEnabled by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    val businessTypes = listOf(
        "Retail Shop",
        "Wholesale & Distributor",
        "Services & Agency",
        "Restaurant & Cafe",
        "Pharmacy & Medical",
        "Manufacturing",
        "General Trade"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF071228))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp)
            .testTag("onboarding_setup_screen")
    ) {
        // Welcome Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F2A4A))
                    .border(1.dp, SkyBlueBright.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (userName.takeIf { it.isNotBlank() } ?: userEmail).take(1).uppercase(),
                    color = SkyBlueBright,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Welcome to Atri Khata",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail,
                    color = SkyBlueBright,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "STEP 1: SELECT ACCOUNT MODE",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Account Type Selector Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Business Card
            AccountTypeOptionCard(
                title = "Business",
                subtitle = "Invoicing, inventory, party ledger & tax",
                icon = Icons.Default.Business,
                isSelected = selectedAccountType == AccountType.BUSINESS,
                onClick = { selectedAccountType = AccountType.BUSINESS },
                modifier = Modifier.weight(1f)
            )

            // Personal Card
            AccountTypeOptionCard(
                title = "Personal",
                subtitle = "Daily khata, lending & personal expenses",
                icon = Icons.Default.Person,
                isSelected = selectedAccountType == AccountType.PERSONAL,
                onClick = { selectedAccountType = AccountType.PERSONAL },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // If Business, show Business Configuration
        AnimatedVisibility(visible = selectedAccountType == AccountType.BUSINESS) {
            Column {
                Text(
                    text = "STEP 2: BUSINESS PROFILE",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Business Name
                        Text("Business / Store Name", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = SkyBlueBright,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_business_name_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Business Type Chips
                        Text("Business Type", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            businessTypes.forEach { type ->
                                val isSelected = selectedBusinessType == type
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF0F2A4A) else SurfaceDark)
                                        .border(1.dp, if (isSelected) SkyBlueBright.copy(alpha = 0.6f) else CardBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedBusinessType = type }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = type,
                                        color = if (isSelected) TextWhite else TextSubtle,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SkyBlueBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PAN / VAT optional
                        Text("PAN / VAT Number (Optional)", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = panNumber,
                            onValueChange = { panNumber = it },
                            placeholder = { Text("e.g. 601234567", color = TextSubtle) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = SkyBlueBright,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // VAT toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Enable VAT (13%)", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Applies Nepal standard 13% tax to invoices", color = TextSubtle, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isVatEnabled,
                                onCheckedChange = { isVatEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SkyBlue,
                                    uncheckedThumbColor = TextSubtle,
                                    uncheckedTrackColor = SurfaceDark
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Complete Button
        Button(
            onClick = {
                if (selectedAccountType == AccountType.BUSINESS && businessName.isBlank()) {
                    Toast.makeText(context, "Please enter your business name", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitting = true
                viewModel.completeOnboarding(
                    accountType = selectedAccountType,
                    businessType = selectedBusinessType,
                    businessName = if (selectedAccountType == AccountType.BUSINESS) businessName.trim() else "Personal Khata",
                    panVatNumber = panNumber.trim(),
                    isVatEnabled = isVatEnabled,
                    onDone = {
                        isSubmitting = false
                        Toast.makeText(context, "Welcome to Atri Khata!", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                )
            },
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.Black),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("complete_onboarding_button")
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Initializing Cloud & Database...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text("Get Started & Open Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun AccountTypeOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF0F2A4A) else CardDark),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(
                1.5.dp,
                if (isSelected) SkyBlueBright else CardBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SkyBlueBright.copy(alpha = 0.2f) else SurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) SkyBlueBright else TextSubtle,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SkyBlueBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = title,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
