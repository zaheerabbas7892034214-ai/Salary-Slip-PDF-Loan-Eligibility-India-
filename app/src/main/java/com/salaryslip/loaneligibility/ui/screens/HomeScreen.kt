package com.salaryslip.loaneligibility.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salaryslip.loaneligibility.data.SalaryProfile
import com.salaryslip.loaneligibility.ui.Screen
import com.salaryslip.loaneligibility.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel,
    isPremium: Boolean,
    savedProfiles: List<SalaryProfile>
) {
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.parsePDF(it)
            navController.navigate(Screen.Parsing.route)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Eligibility Calculator") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pdfLauncher.launch("application/pdf") }) {
                Icon(Icons.Default.Add, contentDescription = "Select PDF")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!isPremium) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Unlock Pro Features",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("✓ Export PDF Reports")
                        Text("✓ Save Unlimited Profiles")
                        Text("✓ Share Reports")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate(Screen.Paywall.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go Pro - ₹299")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text(
                text = "Saved Profiles",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (savedProfiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No saved profiles yet")
                }
            } else {
                LazyColumn {
                    items(savedProfiles) { profile ->
                        ProfileCard(profile)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: SalaryProfile) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = profile.employeeName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = profile.employer,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Net Pay", style = MaterialTheme.typography.labelSmall)
                    Text(text = currencyFormat.format(profile.netPay))
                }
                Column {
                    Text(text = "Month", style = MaterialTheme.typography.labelSmall)
                    Text(text = profile.month)
                }
            }
        }
    }
}
