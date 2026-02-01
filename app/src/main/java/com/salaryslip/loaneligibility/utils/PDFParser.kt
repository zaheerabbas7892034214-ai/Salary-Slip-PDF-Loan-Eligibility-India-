package com.salaryslip.loaneligibility.utils

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ParsedSalaryData(
    val employer: String = "",
    val employeeName: String = "",
    val month: String = "",
    val netPay: Double = 0.0,
    val grossPay: Double = 0.0,
    val basic: Double = 0.0,
    val hra: Double = 0.0,
    val allowances: Double = 0.0,
    val deductions: Double = 0.0,
    val pf: Double? = null,
    val esi: Double? = null,
    val confidence: Map<String, ConfidenceLevel> = emptyMap()
)

enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW
}

class PDFParser(private val context: Context) {
    
    init {
        PDFBoxResourceLoader.init(context)
    }
    
    suspend fun parsePDF(uri: Uri): ParsedSalaryData = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            
            extractDataFromText(text)
        } catch (e: Exception) {
            e.printStackTrace()
            ParsedSalaryData()
        }
    }
    
    private fun extractDataFromText(text: String): ParsedSalaryData {
        val lines = text.lines().map { it.trim() }
        val confidenceMap = mutableMapOf<String, ConfidenceLevel>()
        
        // Extract employer (usually in header)
        val employer = lines.take(5).firstOrNull { 
            it.isNotEmpty() && !it.contains("salary", ignoreCase = true) 
        } ?: ""
        confidenceMap["employer"] = if (employer.isNotEmpty()) ConfidenceLevel.MEDIUM else ConfidenceLevel.LOW
        
        // Extract employee name (look for common patterns)
        val employeeName = extractField(lines, listOf("employee name", "name", "emp name"))
        confidenceMap["employeeName"] = if (employeeName.isNotEmpty()) ConfidenceLevel.MEDIUM else ConfidenceLevel.LOW
        
        // Extract month
        val month = extractMonth(text)
        confidenceMap["month"] = if (month.isNotEmpty()) ConfidenceLevel.HIGH else ConfidenceLevel.LOW
        
        // Extract financial fields
        val netPay = extractAmount(lines, listOf("net pay", "net salary", "take home"))
        confidenceMap["netPay"] = if (netPay > 0) ConfidenceLevel.HIGH else ConfidenceLevel.LOW
        
        val grossPay = extractAmount(lines, listOf("gross pay", "gross salary", "gross earnings"))
        confidenceMap["grossPay"] = if (grossPay > 0) ConfidenceLevel.HIGH else ConfidenceLevel.LOW
        
        val basic = extractAmount(lines, listOf("basic", "basic salary", "basic pay"))
        confidenceMap["basic"] = if (basic > 0) ConfidenceLevel.HIGH else ConfidenceLevel.LOW
        
        val hra = extractAmount(lines, listOf("hra", "house rent allowance"))
        confidenceMap["hra"] = if (hra > 0) ConfidenceLevel.MEDIUM else ConfidenceLevel.LOW
        
        val allowances = extractAmount(lines, listOf("allowances", "special allowance", "other allowances"))
        confidenceMap["allowances"] = if (allowances > 0) ConfidenceLevel.MEDIUM else ConfidenceLevel.LOW
        
        val deductions = extractAmount(lines, listOf("total deductions", "deductions"))
        confidenceMap["deductions"] = if (deductions > 0) ConfidenceLevel.HIGH else ConfidenceLevel.LOW
        
        val pf = extractAmount(lines, listOf("pf", "provident fund", "epf")).takeIf { it > 0 }
        val esi = extractAmount(lines, listOf("esi", "esic", "employee state insurance")).takeIf { it > 0 }
        
        return ParsedSalaryData(
            employer = employer,
            employeeName = employeeName,
            month = month,
            netPay = netPay,
            grossPay = grossPay,
            basic = basic,
            hra = hra,
            allowances = allowances,
            deductions = deductions,
            pf = pf,
            esi = esi,
            confidence = confidenceMap
        )
    }
    
    private fun extractField(lines: List<String>, keywords: List<String>): String {
        for (line in lines) {
            for (keyword in keywords) {
                if (line.contains(keyword, ignoreCase = true)) {
                    // Extract value after colon or spaces
                    val parts = line.split(":", "–", "-")
                    if (parts.size > 1) {
                        return parts[1].trim().takeWhile { !it.isDigit() }.trim()
                    }
                }
            }
        }
        return ""
    }
    
    private fun extractMonth(text: String): String {
        val months = listOf(
            "january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november", "december"
        )
        
        for (month in months) {
            if (text.contains(month, ignoreCase = true)) {
                // Try to extract year as well
                val regex = Regex("$month\\s*(\\d{4})", RegexOption.IGNORE_CASE)
                val match = regex.find(text)
                return if (match != null) {
                    "${month.capitalize()} ${match.groupValues[1]}"
                } else {
                    month.capitalize()
                }
            }
        }
        return ""
    }
    
    private fun extractAmount(lines: List<String>, keywords: List<String>): Double {
        for (line in lines) {
            for (keyword in keywords) {
                if (line.contains(keyword, ignoreCase = true)) {
                    // Extract number from the line
                    val numbers = Regex("\\d+(?:,\\d+)*(?:\\.\\d+)?").findAll(line)
                    val amounts = numbers.map { 
                        it.value.replace(",", "").toDoubleOrNull() ?: 0.0 
                    }.filter { it > 0 }.toList()
                    
                    // Return the largest number found (most likely the amount)
                    return amounts.maxOrNull() ?: 0.0
                }
            }
        }
        return 0.0
    }
}
