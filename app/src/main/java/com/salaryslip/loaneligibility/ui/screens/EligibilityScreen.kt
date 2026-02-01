package com.salaryslip.loaneligibility.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salaryslip.loaneligibility.ui.Screen
import com.salaryslip.loaneligibility.ui.theme.GreenSuccess
import com.salaryslip.loaneligibility.ui.theme.RedError
import com.salaryslip.loaneligibility.ui.theme.YellowWarning
import com.salaryslip.loaneligibility.utils.LoanEligibility
import com.salaryslip.loaneligibility.utils.ProfileRisk
import com.salaryslip.loaneligibility.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EligibilityScreen(
    navController: NavController,
    viewModel: MainViewModel,
    eligibility: LoanEligibility?,
    isPremium: Boolean
) {
    var interestRate by remember { mutableStateOf(10f) }
    var tenure by remember { mutableStateOf(60f) }
    var emiAffordability by remember { mutableStateOf(40f) }
    
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            viewModel.generateReport(it)
        }
    }
    
    LaunchedEffect(interestRate, tenure, emiAffordability) {
        viewModel.calculateEligibility(
            interestRate = interestRate.toDouble(),
            tenureMonths = tenure.toInt(),
            emiAffordability = emiAffordability.toDouble()
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Eligibility") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (eligibility == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (eligibility.profileRisk) {
                                ProfileRisk.GOOD -> GreenSuccess.copy(alpha = 0.1f)
                                ProfileRisk.MODERATE -> YellowWarning.copy(alpha = 0.1f)
                                ProfileRisk.RISKY -> RedError.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Eligible Loan Amount",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = currencyFormat.format(eligibility.eligibleAmount),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Monthly EMI: ${currencyFormat.format(eligibility.monthlyEMI)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Profile: ${eligibility.profileRisk.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Text(
                        text = "Loan Parameters",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Text("Interest Rate: ${interestRate.toInt()}%")
                    Slider(
                        value = interestRate,
                        onValueChange = { interestRate = it },
                        valueRange = 5f..20f,
                        steps = 14
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Text("Tenure: ${tenure.toInt()} months")
                    Slider(
                        value = tenure,
                        onValueChange = { tenure = it },
                        valueRange = 12f..240f,
                        steps = 18
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Text("EMI Affordability: ${emiAffordability.toInt()}%")
                    Slider(
                        value = emiAffordability,
                        onValueChange = { emiAffordability = it },
                        valueRange = 20f..60f,
                        steps = 7
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (isPremium) {
                                    viewModel.saveProfile()
                                } else {
                                    navController.navigate(Screen.Paywall.route)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isPremium) "Save" else "Save (Pro)")
                        }
                        
                        Button(
                            onClick = {
                                if (isPremium) {
                                    createDocLauncher.launch("loan_report.pdf")
                                } else {
                                    navController.navigate(Screen.Paywall.route)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isPremium) "Export" else "Export (Pro)")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Text(
                        text = "EMI Schedule (First Year)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(eligibility.emiSchedule.take(12)) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Month ${item.month}")
                            Text(currencyFormat.format(item.emi))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
