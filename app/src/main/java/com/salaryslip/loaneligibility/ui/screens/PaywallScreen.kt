package com.salaryslip.loaneligibility.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salaryslip.loaneligibility.data.PurchaseState
import com.salaryslip.loaneligibility.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    navController: NavController,
    viewModel: MainViewModel,
    purchaseState: PurchaseState
) {
    val context = LocalContext.current
    
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success -> {
                navController.popBackStack()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unlock Pro Features") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Go Pro",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                FeatureItem("Export PDF Reports")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem("Save Unlimited Profiles")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem("Share Reports")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem("Lifetime Access")
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "One-Time Purchase",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "₹299",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
            
            Column {
                Button(
                    onClick = {
                        viewModel.billingManager.launchPurchaseFlow(
                            context as android.app.Activity
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = purchaseState !is PurchaseState.Loading
                ) {
                    if (purchaseState is PurchaseState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Buy Now - ₹299")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = { viewModel.billingManager.restorePurchases() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restore Purchases")
                }
                
                when (purchaseState) {
                    is PurchaseState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = purchaseState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
