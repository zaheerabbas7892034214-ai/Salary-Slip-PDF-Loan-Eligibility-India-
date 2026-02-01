package com.salaryslip.loaneligibility.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salaryslip.loaneligibility.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    var showClearDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("Restore Purchases") },
                supportingContent = { Text("Restore your Pro purchase") },
                modifier = Modifier.clickable {
                    viewModel.billingManager.restorePurchases()
                }
            )
            Divider()
            
            ListItem(
                headlineContent = { Text("Clear Saved Data") },
                supportingContent = { Text("Delete all saved profiles") },
                modifier = Modifier.clickable {
                    showClearDialog = true
                }
            )
            Divider()
            
            ListItem(
                headlineContent = { Text("Privacy Policy") },
                supportingContent = { Text("View our privacy policy") },
                modifier = Modifier.clickable {
                    // Open privacy policy URL
                }
            )
            Divider()
            
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0") }
            )
        }
    }
    
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to delete all saved profiles? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
