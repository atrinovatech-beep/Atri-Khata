package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.NavTab
import com.example.ui.components.GenerateFinancialReportSheet
import com.example.ui.theme.BackgroundDark

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = 8.dp)
    ) {
        GenerateFinancialReportSheet(
            viewModel = viewModel,
            onClose = { viewModel.setTab(NavTab.HOME) }
        )
    }
}
