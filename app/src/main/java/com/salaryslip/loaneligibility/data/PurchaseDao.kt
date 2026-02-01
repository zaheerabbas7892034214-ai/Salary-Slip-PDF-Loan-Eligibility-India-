package com.salaryslip.loaneligibility.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE productId = :productId")
    suspend fun getPurchase(productId: String): Purchase?
    
    @Query("SELECT * FROM purchases")
    fun getAllPurchases(): Flow<List<Purchase>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)
    
    @Query("DELETE FROM purchases WHERE productId = :productId")
    suspend fun deletePurchase(productId: String)
}
