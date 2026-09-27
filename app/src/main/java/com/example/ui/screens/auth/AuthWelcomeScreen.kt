package com.example.ui.screens.auth

import android.app.Activity
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.auth.GoogleSignInResult
import com.example.ui.MainViewModel
import com.example.ui.screens.AtriKhataLogo
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun AuthWelcomeScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (isNewUser: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    var showEmailFallbackDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071228),
                        Color(0xFF0A1B3B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .testTag("auth_welcome_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header & Branding Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Large 3D Geometric Atri Khata Brand Mark
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF0284C7))
                            )
                        )
                        .border(1.5.dp, SkyBlueBright.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AtriKhataLogo(modifier = Modifier.size(54.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Atri Khata",
                    color = TextWhite,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Modern & Free Business Accounting for Nepal & Beyond",
                    color = SkyBlueBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Badge: Free & Open
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0C244A))
                        .border(1.dp, SkyBlueBright.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Free Core Accounting • Dual BS/AD Calendar",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Value Proposition Feature Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureRow(
                    icon = Icons.Default.OfflinePin,
                    iconTint = Color(0xFF34D399),
                    title = "Offline-First Reliability",
                    subtitle = "Create invoices, record sales & check stock even without internet"
                )
                FeatureRow(
                    icon = Icons.Default.CloudSync,
                    iconTint = SkyBlueBright,
                    title = "Encrypted Cloud Sync",
                    subtitle = "Sign in on any Android phone to restore your business data instantly"
                )
                FeatureRow(
                    icon = Icons.Default.Lock,
                    iconTint = Color(0xFFF472B6),
                    title = "Private & Isolated Storage",
                    subtitle = "Your accounts and party ledgers belong strictly to your identity"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Authentication Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SkyBlueBright,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = loadingMessage,
                            color = SkyBlueBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Google Sign-In Button (Official Brand Styling)
                    Button(
                        onClick = {
                            if (activity != null) {
                                isLoading = true
                                loadingMessage = "Connecting to Google Account..."
                                scope.launch {
                                    viewModel.handleGoogleSignIn(
                                        activity = activity,
                                        onResult = { result ->
                                            isLoading = false
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "Signed in as ${result.displayName}", Toast.LENGTH_SHORT).show()
                                                val isNew = viewModel.isNewUserSession()
                                                onLoginSuccess(isNew)
                                            } else if (result.fallbackRequired) {
                                                // Show quick Google email dialog for devices without Play Store
                                                showEmailFallbackDialog = true
                                            } else if (!result.isCancelled) {
                                                Toast.makeText(context, result.errorMessage ?: "Sign in failed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            } else {
                                showEmailFallbackDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2937)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("continue_with_google_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }

                    // Direct Login / Email Option
                    OutlinedButton(
                        onClick = { showEmailFallbackDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(SkyBlueBright.copy(alpha = 0.5f), SkyBlue.copy(alpha = 0.5f)))
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button")
                    ) {
                        Text(
                            text = "Login with Google Email",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Offline / Guest Mode Access
                    TextButton(
                        onClick = {
                            viewModel.continueAsGuest()
                            Toast.makeText(context, "Opened in Offline Mode", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(false)
                        },
                        modifier = Modifier.testTag("guest_offline_mode_button")
                    ) {
                        Text(
                            text = "Continue in Offline Mode (Skip for now)",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer info
            Text(
                text = "By continuing, you agree to Atri Khata's Terms & Privacy Policy.\nYour local data is encrypted and protected on-device.",
                color = TextSubtle.copy(alpha = 0.7f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }

    // Google Account Chooser Dialog (used when Credential Manager falls back)
    if (showEmailFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showEmailFallbackDialog = false },
            containerColor = SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleIcon(modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your Google Account email. No password needed. Atri Khata links your cloud backup to this identity.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Google Email", color = TextSubtle) },
                        placeholder = { Text("e.g. store.owner@gmail.com", color = TextSubtle) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = SkyBlueBright,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name (Optional)", color = TextSubtle) },
                        placeholder = { Text("e.g. Ramesh Shrestha", color = TextSubtle) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = SkyBlueBright,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = emailInput.trim()
                        if (trimmed.isNotBlank() && trimmed.contains("@")) {
                            showEmailFallbackDialog = false
                            viewModel.signInWithEmailDirectly(
                                email = trimmed,
                                name = nameInput.trim().ifBlank { null },
                                onComplete = { isNew ->
                                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(isNew)
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please enter a valid Google email address", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_google_signin_button")
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailFallbackDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark.copy(alpha = 0.6f))
            .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f))
                .border(1.dp, iconTint.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            color = Color(0xFF4285F4),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}
