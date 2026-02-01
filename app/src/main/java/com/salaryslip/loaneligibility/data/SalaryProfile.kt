package com.salaryslip.loaneligibility.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_profiles")
data class SalaryProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employer: String,
    val employeeName: String,
    val month: String,
    val netPay: Double,
    val grossPay: Double,
    val basic: Double,
    val hra: Double,
    val allowances: Double,
    val deductions: Double,
    val pf: Double?,
    val esi: Double?,
    val timestamp: Long = System.currentTimeMillis()
)
