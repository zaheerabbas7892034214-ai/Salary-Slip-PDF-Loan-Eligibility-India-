package com.salaryslip.loaneligibility.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryProfileDao {
    @Query("SELECT * FROM salary_profiles ORDER BY timestamp DESC")
    fun getAllProfiles(): Flow<List<SalaryProfile>>
    
    @Query("SELECT * FROM salary_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): SalaryProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SalaryProfile): Long
    
    @Delete
    suspend fun deleteProfile(profile: SalaryProfile)
    
    @Query("DELETE FROM salary_profiles")
    suspend fun deleteAllProfiles()
}
