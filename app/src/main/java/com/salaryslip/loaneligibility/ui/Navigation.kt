package com.salaryslip.loaneligibility.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Parsing : Screen("parsing")
    object Eligibility : Screen("eligibility")
    object Paywall : Screen("paywall")
    object Settings : Screen("settings")
}
