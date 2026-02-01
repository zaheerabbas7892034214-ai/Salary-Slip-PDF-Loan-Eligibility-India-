package com.salaryslip.loaneligibility.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey
    val productId: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val acknowledged: Boolean = true
)
