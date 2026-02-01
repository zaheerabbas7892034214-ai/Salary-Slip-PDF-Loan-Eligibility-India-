package com.salaryslip.loaneligibility

import com.salaryslip.loaneligibility.utils.LoanCalculator
import com.salaryslip.loaneligibility.utils.ProfileRisk
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for loan eligibility calculations
 */
class LoanCalculatorTest {

    private val calculator = LoanCalculator()

    @Test
    fun testBasicEligibilityCalculation() {
        // Test with a net salary of 50,000 INR, 10% interest, 60 months tenure
        val result = calculator.calculateEligibility(
            netSalary = 50000.0,
            interestRate = 10.0,
            tenureMonths = 60,
            emiAffordabilityPercent = 40.0
        )

        // Max EMI should be 40% of 50,000 = 20,000
        assertTrue("Monthly EMI should be around 20,000", 
            result.monthlyEMI >= 19900 && result.monthlyEMI <= 20100)
        
        // Eligible amount should be positive
        assertTrue("Eligible amount should be positive", 
            result.eligibleAmount > 0)
        
        // Total interest should be positive
        assertTrue("Total interest should be positive", 
            result.totalInterest > 0)
        
        // EMI schedule should have 60 entries
        assertEquals("EMI schedule should have 60 entries", 
            60, result.emiSchedule.size)
    }

    @Test
    fun testZeroInterestRate() {
        val result = calculator.calculateEligibility(
            netSalary = 50000.0,
            interestRate = 0.0,
            tenureMonths = 60,
            emiAffordabilityPercent = 40.0
        )

        // With 0% interest, eligible amount = EMI * tenure
        val expectedAmount = 20000.0 * 60
        assertTrue("Eligible amount should equal EMI * tenure for 0% interest",
            Math.abs(result.eligibleAmount - expectedAmount) < 100)
        
        // Total interest should be 0
        assertEquals("Total interest should be 0 for 0% rate", 
            0.0, result.totalInterest, 0.01)
    }

    @Test
    fun testProfileRiskAssessment() {
        // Good profile
        val goodResult = calculator.calculateEligibility(
            netSalary = 50000.0,
            interestRate = 10.0,
            tenureMonths = 60,
            deductions = 5000.0,
            grossPay = 55000.0
        )
        assertEquals("Should be GOOD profile", 
            ProfileRisk.GOOD, goodResult.profileRisk)

        // Risky profile
        val riskyResult = calculator.calculateEligibility(
            netSalary = 18000.0,
            interestRate = 10.0,
            tenureMonths = 60,
            deductions = 10000.0,
            grossPay = 28000.0
        )
        assertEquals("Should be RISKY profile", 
            ProfileRisk.RISKY, riskyResult.profileRisk)
    }

    @Test
    fun testEMIAffordabilityVariation() {
        val result20 = calculator.calculateEligibility(
            netSalary = 50000.0,
            interestRate = 10.0,
            tenureMonths = 60,
            emiAffordabilityPercent = 20.0
        )

        val result60 = calculator.calculateEligibility(
            netSalary = 50000.0,
            interestRate = 10.0,
            tenureMonths = 60,
            emiAffordabilityPercent = 60.0
        )

        // Higher affordability should result in higher eligible amount
        assertTrue("60% affordability should give higher eligibility than 20%",
            result60.eligibleAmount > result20.eligibleAmount)
    }

    @Test
    fun testTenureVariation() {
        val result12 = calculator.calculateEligibility(
            netSalary = 50000.0,
            interestRate = 10.0,
            tenureMonths = 12,
            emiAffordabilityPercent = 40.0
        )

        val result240 = calculator.calculateEligibility(
            netSalary = 50000.0,
            interestRate = 10.0,
            tenureMonths = 240,
            emiAffordabilityPercent = 40.0
        )

        // Longer tenure should result in higher eligible amount
        assertTrue("240 months tenure should give higher eligibility than 12 months",
            result240.eligibleAmount > result12.eligibleAmount)
        
        // Check EMI schedule sizes
        assertEquals(12, result12.emiSchedule.size)
        assertEquals(240, result240.emiSchedule.size)
    }
}
