package com.salaryslip.loaneligibility.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OCRFallback(private val context: Context) {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun extractTextFromPDF(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            if (parcelFileDescriptor != null) {
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                val fullText = StringBuilder()
                
                // Process first 3 pages (most salary slips are 1-3 pages)
                val pageCount = minOf(pdfRenderer.pageCount, 3)
                
                for (i in 0 until pageCount) {
                    val page = pdfRenderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(
                        page.width * 2,
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    
                    // Perform OCR on the bitmap
                    val text = recognizeText(bitmap)
                    fullText.append(text).append("\n")
                    
                    bitmap.recycle()
                }
                
                pdfRenderer.close()
                parcelFileDescriptor.close()
                
                fullText.toString()
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    private suspend fun recognizeText(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
