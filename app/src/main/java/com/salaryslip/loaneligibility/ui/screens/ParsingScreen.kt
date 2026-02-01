package com.salaryslip.loaneligibility.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salaryslip.loaneligibility.ui.Screen
import com.salaryslip.loaneligibility.utils.ConfidenceLevel
import com.salaryslip.loaneligibility.utils.ParsedSalaryData
import com.salaryslip.loaneligibility.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParsingScreen(
    navController: NavController,
    viewModel: MainViewModel,
    parsedData: ParsedSalaryData?
) {
    var data by remember { mutableStateOf(parsedData ?: ParsedSalaryData()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extracted Data") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = data.employer,
                onValueChange = { data = data.copy(employer = it) },
                label = { Text("Employer") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = data.employeeName,
                onValueChange = { data = data.copy(employeeName = it) },
                label = { Text("Employee Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = data.month,
                onValueChange = { data = data.copy(month = it) },
                label = { Text("Month") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = data.netPay.toString(),
                onValueChange = { data = data.copy(netPay = it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Net Pay (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    ConfidenceBadge(data.confidence["netPay"] ?: ConfidenceLevel.LOW)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = data.grossPay.toString(),
                onValueChange = { data = data.copy(grossPay = it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Gross Pay (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    ConfidenceBadge(data.confidence["grossPay"] ?: ConfidenceLevel.LOW)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = data.basic.toString(),
                onValueChange = { data = data.copy(basic = it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Basic (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = data.hra.toString(),
                onValueChange = { data = data.copy(hra = it.toDoubleOrNull() ?: 0.0) },
                label = { Text("HRA (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = data.deductions.toString(),
                onValueChange = { data = data.copy(deductions = it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Deductions (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    viewModel.updateParsedData(data)
                    viewModel.calculateEligibility()
                    navController.navigate(Screen.Eligibility.route)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = data.netPay > 0
            ) {
                Text("Calculate Eligibility")
            }
        }
    }
}

@Composable
fun ConfidenceBadge(level: ConfidenceLevel) {
    val color = when (level) {
        ConfidenceLevel.HIGH -> MaterialTheme.colorScheme.primary
        ConfidenceLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
        ConfidenceLevel.LOW -> MaterialTheme.colorScheme.error
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = level.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
