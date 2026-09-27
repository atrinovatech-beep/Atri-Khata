package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.NavTab
import com.example.ui.components.ActionDialogHost
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PartiesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesInvoiceDetailScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.screens.auth.AuthWelcomeScreen
import com.example.ui.screens.auth.OnboardingSetupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsStateWithLifecycle()
            val userSession by viewModel.userSession.collectAsStateWithLifecycle()
            var isPendingOnboarding by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                when {
                    !isUserAuthenticated -> {
                        AuthWelcomeScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { isNew ->
                                if (isNew) {
                                    isPendingOnboarding = true
                                }
                            }
                        )
                    }
                    isPendingOnboarding -> {
                        OnboardingSetupScreen(
                            viewModel = viewModel,
                            userEmail = userSession?.email ?: "",
                            userName = userSession?.displayName ?: "",
                            onComplete = {
                                isPendingOnboarding = false
                            }
                        )
                    }
                    else -> {
                        AtriNovaApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AtriNovaApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()
    val parties by viewModel.allParties.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.allInventoryItems.collectAsStateWithLifecycle()
    val selectedInvoiceId by viewModel.selectedInvoiceId.collectAsStateWithLifecycle()

    if (selectedInvoiceId != null) {
        BackHandler {
            viewModel.selectInvoiceForDetail(null)
        }
        SalesInvoiceDetailScreen(
            invoiceId = selectedInvoiceId!!,
            viewModel = viewModel,
            onBack = { viewModel.selectInvoiceForDetail(null) }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            containerColor = AppTheme.colors.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(AppTheme.colors.background)
            ) {
                Crossfade(
                    targetState = currentTab,
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        NavTab.HOME -> HomeScreen(viewModel = viewModel)
                        NavTab.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
                        NavTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                        NavTab.PARTIES -> PartiesScreen(viewModel = viewModel)
                        NavTab.INVENTORY -> InventoryScreen(viewModel = viewModel)
                        NavTab.MORE -> MoreScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Modal dialogs and bottom sheets
    ActionDialogHost(
        viewModel = viewModel,
        activeDialog = activeDialog,
        parties = parties,
        inventoryItems = inventoryItems
    )
}
