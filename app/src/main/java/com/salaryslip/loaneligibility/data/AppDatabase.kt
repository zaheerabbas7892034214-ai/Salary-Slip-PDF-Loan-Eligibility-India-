package com.salaryslip.loaneligibility.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SalaryProfile::class, Purchase::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salaryProfileDao(): SalaryProfileDao
    abstract fun purchaseDao(): PurchaseDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loan_eligibility_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
