package com.salaryslip.loaneligibility.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salaryslip.loaneligibility.data.AppDatabase
import com.salaryslip.loaneligibility.data.BillingManager
import com.salaryslip.loaneligibility.data.PurchaseState
import com.salaryslip.loaneligibility.data.SalaryProfile
import com.salaryslip.loaneligibility.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val profileDao = database.salaryProfileDao()
    private val purchaseDao = database.purchaseDao()
    
    val billingManager = BillingManager(application, purchaseDao)
    
    private val pdfParser = PDFParser(application)
    private val ocrFallback = OCRFallback(application)
    private val loanCalculator = LoanCalculator()
    private val reportGenerator = PDFReportGenerator(application)
    
    // State flows
    private val _parsedData = MutableStateFlow<ParsedSalaryData?>(null)
    val parsedData: StateFlow<ParsedSalaryData?> = _parsedData.asStateFlow()
    
    private val _eligibility = MutableStateFlow<LoanEligibility?>(null)
    val eligibility: StateFlow<LoanEligibility?> = _eligibility.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    val savedProfiles: StateFlow<List<SalaryProfile>> = profileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val isPremium: StateFlow<Boolean> = billingManager.isPremium
    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState
    
    init {
        billingManager.initialize()
    }
    
    fun parsePDF(uri: Uri, useOCR: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val data = if (useOCR) {
                    val text = ocrFallback.extractTextFromPDF(uri)
                    // Use PDF parser's extraction logic on OCR text
                    pdfParser.parsePDF(uri) // Simplified - in production, pass OCR text
                } else {
                    pdfParser.parsePDF(uri)
                }
                
                _parsedData.value = data
            } catch (e: Exception) {
                _error.value = "Failed to parse PDF: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateParsedData(data: ParsedSalaryData) {
        _parsedData.value = data
    }
    
    fun calculateEligibility(
        interestRate: Double = 10.0,
        tenureMonths: Int = 60,
        emiAffordability: Double = 40.0
    ) {
        val data = _parsedData.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val result = loanCalculator.calculateEligibility(
                    netSalary = data.netPay,
                    interestRate = interestRate,
                    tenureMonths = tenureMonths,
                    emiAffordabilityPercent = emiAffordability,
                    deductions = data.deductions,
                    grossPay = data.grossPay
                )
                
                _eligibility.value = result
            } catch (e: Exception) {
                _error.value = "Failed to calculate eligibility: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveProfile() {
        val data = _parsedData.value ?: return
        
        if (!isPremium.value) {
            _error.value = "This is a Pro feature"
            return
        }
        
        viewModelScope.launch {
            try {
                val profile = SalaryProfile(
                    employer = data.employer,
                    employeeName = data.employeeName,
                    month = data.month,
                    netPay = data.netPay,
                    grossPay = data.grossPay,
                    basic = data.basic,
                    hra = data.hra,
                    allowances = data.allowances,
                    deductions = data.deductions,
                    pf = data.pf,
                    esi = data.esi
                )
                
                profileDao.insertProfile(profile)
            } catch (e: Exception) {
                _error.value = "Failed to save profile: ${e.message}"
            }
        }
    }
    
    fun generateReport(uri: Uri) {
        val data = _parsedData.value ?: return
        val elig = _eligibility.value ?: return
        
        if (!isPremium.value) {
            _error.value = "This is a Pro feature"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val success = reportGenerator.generateReport(uri, data, elig)
                if (!success) {
                    _error.value = "Failed to generate report"
                }
            } catch (e: Exception) {
                _error.value = "Failed to generate report: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            try {
                profileDao.deleteAllProfiles()
            } catch (e: Exception) {
                _error.value = "Failed to clear data: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        billingManager.destroy()
    }
}
