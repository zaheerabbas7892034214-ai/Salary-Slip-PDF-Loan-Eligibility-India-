package com.salaryslip.loaneligibility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.salaryslip.loaneligibility.ui.Screen
import com.salaryslip.loaneligibility.ui.screens.*
import com.salaryslip.loaneligibility.ui.theme.SalarySlipLoanEligibilityTheme
import com.salaryslip.loaneligibility.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalarySlipLoanEligibilityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    val isPremium by viewModel.isPremium.collectAsState()
    val parsedData by viewModel.parsedData.collectAsState()
    val eligibility by viewModel.eligibility.collectAsState()
    val savedProfiles by viewModel.savedProfiles.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                isPremium = isPremium
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = viewModel,
                isPremium = isPremium,
                savedProfiles = savedProfiles
            )
        }
        
        composable(Screen.Parsing.route) {
            ParsingScreen(
                navController = navController,
                viewModel = viewModel,
                parsedData = parsedData
            )
        }
        
        composable(Screen.Eligibility.route) {
            EligibilityScreen(
                navController = navController,
                viewModel = viewModel,
                eligibility = eligibility,
                isPremium = isPremium
            )
        }
        
        composable(Screen.Paywall.route) {
            PaywallScreen(
                navController = navController,
                viewModel = viewModel,
                purchaseState = purchaseState
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
