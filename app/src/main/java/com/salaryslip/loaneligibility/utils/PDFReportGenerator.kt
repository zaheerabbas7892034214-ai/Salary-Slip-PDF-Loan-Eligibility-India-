package com.salaryslip.loaneligibility.utils

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class PDFReportGenerator(private val context: Context) {
    
    suspend fun generateReport(
        uri: Uri,
        salaryData: ParsedSalaryData,
        eligibility: LoanEligibility
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val document = PDDocument()
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)
            
            val contentStream = PDPageContentStream(document, page)
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            
            var yPosition = 750f
            
            // Title
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20f)
            contentStream.newLineAtOffset(50f, yPosition)
            contentStream.showText("Loan Eligibility Report")
            contentStream.endText()
            yPosition -= 40
            
            // Salary Information
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
            contentStream.newLineAtOffset(50f, yPosition)
            contentStream.showText("Salary Information")
            contentStream.endText()
            yPosition -= 25
            
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            writeField(contentStream, 50f, yPosition, "Employee Name:", salaryData.employeeName)
            yPosition -= 20
            writeField(contentStream, 50f, yPosition, "Employer:", salaryData.employer)
            yPosition -= 20
            writeField(contentStream, 50f, yPosition, "Month:", salaryData.month)
            yPosition -= 20
            writeField(contentStream, 50f, yPosition, "Net Pay:", currencyFormat.format(salaryData.netPay))
            yPosition -= 20
            writeField(contentStream, 50f, yPosition, "Gross Pay:", currencyFormat.format(salaryData.grossPay))
            yPosition -= 30
            
            // Eligibility Results
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
            contentStream.newLineAtOffset(50f, yPosition)
            contentStream.showText("Eligibility Results")
            contentStream.endText()
            yPosition -= 25
            
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            writeField(contentStream, 50f, yPosition, "Eligible Loan Amount:", currencyFormat.format(eligibility.eligibleAmount))
            yPosition -= 20
            writeField(contentStream, 50f, yPosition, "Monthly EMI:", currencyFormat.format(eligibility.monthlyEMI))
            yPosition -= 20
            writeField(contentStream, 50f, yPosition, "Total Interest:", currencyFormat.format(eligibility.totalInterest))
            yPosition -= 20
            writeField(contentStream, 50f, yPosition, "Profile Status:", eligibility.profileRisk.name)
            yPosition -= 30
            
            // EMI Schedule (first 12 months)
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
            contentStream.newLineAtOffset(50f, yPosition)
            contentStream.showText("EMI Schedule (First Year)")
            contentStream.endText()
            yPosition -= 25
            
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            writeField(contentStream, 50f, yPosition, "Month", "EMI", 100f)
            writeField(contentStream, 150f, yPosition, "Principal", "", 100f)
            writeField(contentStream, 250f, yPosition, "Interest", "", 100f)
            writeField(contentStream, 350f, yPosition, "Balance", "", 100f)
            yPosition -= 15
            
            val scheduleToShow = eligibility.emiSchedule.take(12)
            for (item in scheduleToShow) {
                if (yPosition < 50) break
                writeField(contentStream, 50f, yPosition, item.month.toString(), "", 100f)
                writeField(contentStream, 150f, yPosition, String.format("%.0f", item.principal), "", 100f)
                writeField(contentStream, 250f, yPosition, String.format("%.0f", item.interest), "", 100f)
                writeField(contentStream, 350f, yPosition, String.format("%.0f", item.balance), "", 100f)
                yPosition -= 15
            }
            
            contentStream.close()
            
            // Save to URI
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                document.save(outputStream)
                outputStream.close()
            }
            document.close()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun writeField(
        contentStream: PDPageContentStream,
        x: Float,
        y: Float,
        label: String,
        value: String = "",
        spacing: Float = 150f
    ) {
        contentStream.beginText()
        contentStream.newLineAtOffset(x, y)
        contentStream.showText(label)
        if (value.isNotEmpty()) {
            contentStream.newLineAtOffset(spacing, 0f)
            contentStream.showText(value)
        }
        contentStream.endText()
    }
}
