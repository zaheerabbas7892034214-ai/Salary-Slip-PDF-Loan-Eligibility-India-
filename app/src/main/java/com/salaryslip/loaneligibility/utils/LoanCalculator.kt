package com.salaryslip.loaneligibility.utils

import kotlin.math.pow

data class LoanEligibility(
    val eligibleAmount: Double,
    val monthlyEMI: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val emiSchedule: List<EMIScheduleItem>,
    val profileRisk: ProfileRisk
)

data class EMIScheduleItem(
    val month: Int,
    val emi: Double,
    val principal: Double,
    val interest: Double,
    val balance: Double
)

enum class ProfileRisk {
    GOOD,
    MODERATE,
    RISKY
}

class LoanCalculator {
    
    fun calculateEligibility(
        netSalary: Double,
        interestRate: Double,
        tenureMonths: Int,
        emiAffordabilityPercent: Double = 40.0,
        deductions: Double = 0.0,
        grossPay: Double = 0.0
    ): LoanEligibility {
        
        // Calculate maximum EMI based on affordability
        val maxEMI = (netSalary * emiAffordabilityPercent) / 100.0
        
        // Calculate eligible loan amount using EMI formula
        // EMI = [P x R x (1+R)^N]/[(1+R)^N-1]
        // Rearranging: P = EMI x [(1+R)^N-1] / [R x (1+R)^N]
        
        val monthlyRate = (interestRate / 12.0) / 100.0
        val eligibleAmount = if (monthlyRate > 0) {
            val term = (1 + monthlyRate).pow(tenureMonths)
            maxEMI * (term - 1) / (monthlyRate * term)
        } else {
            maxEMI * tenureMonths
        }
        
        // Calculate actual EMI for the eligible amount
        val actualEMI = calculateEMI(eligibleAmount, interestRate, tenureMonths)
        
        // Generate EMI schedule
        val schedule = generateEMISchedule(eligibleAmount, interestRate, tenureMonths, actualEMI)
        
        // Calculate total interest and payment
        val totalPayment = actualEMI * tenureMonths
        val totalInterest = totalPayment - eligibleAmount
        
        // Assess profile risk
        val risk = assessProfileRisk(netSalary, deductions, grossPay)
        
        return LoanEligibility(
            eligibleAmount = eligibleAmount,
            monthlyEMI = actualEMI,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            emiSchedule = schedule,
            profileRisk = risk
        )
    }
    
    private fun calculateEMI(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        if (annualRate == 0.0) return principal / tenureMonths
        
        val monthlyRate = (annualRate / 12.0) / 100.0
        val term = (1 + monthlyRate).pow(tenureMonths)
        return (principal * monthlyRate * term) / (term - 1)
    }
    
    private fun generateEMISchedule(
        principal: Double,
        annualRate: Double,
        tenureMonths: Int,
        emi: Double
    ): List<EMIScheduleItem> {
        val schedule = mutableListOf<EMIScheduleItem>()
        var balance = principal
        val monthlyRate = (annualRate / 12.0) / 100.0
        
        for (month in 1..tenureMonths) {
            val interest = balance * monthlyRate
            val principalPaid = emi - interest
            balance -= principalPaid
            
            // Ensure last payment closes the loan
            if (month == tenureMonths) {
                balance = 0.0
            }
            
            schedule.add(
                EMIScheduleItem(
                    month = month,
                    emi = emi,
                    principal = principalPaid,
                    interest = interest,
                    balance = maxOf(0.0, balance)
                )
            )
        }
        
        return schedule
    }
    
    private fun assessProfileRisk(
        netSalary: Double,
        deductions: Double,
        grossPay: Double
    ): ProfileRisk {
        // Calculate deduction ratio
        val deductionRatio = if (grossPay > 0) {
            (deductions / grossPay) * 100
        } else {
            0.0
        }
        
        // Risk assessment criteria
        return when {
            netSalary < 20000 || deductionRatio > 30 -> ProfileRisk.RISKY
            netSalary < 40000 || deductionRatio > 20 -> ProfileRisk.MODERATE
            else -> ProfileRisk.GOOD
        }
    }
}
