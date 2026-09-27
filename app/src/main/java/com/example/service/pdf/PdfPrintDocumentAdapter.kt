package com.example.service.pdf

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Custom PrintDocumentAdapter that feeds a generated PDF file to the Android Print Spooler.
 * Allows users to print directly via connected printers or use "Save as PDF" with system preview.
 */
class PdfPrintDocumentAdapter(
    private val pdfFile: File,
    private val documentName: String = "Financial_Report"
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        if (pdfFile.exists()) {
            val printDocumentInfo = PrintDocumentInfo.Builder("$documentName.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()

            callback?.onLayoutFinished(printDocumentInfo, true)
        } else {
            callback?.onLayoutFailed("PDF file not found")
        }
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        var input: FileInputStream? = null
        var output: FileOutputStream? = null

        try {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
                return
            }

            input = FileInputStream(pdfFile)
            output = FileOutputStream(destination?.fileDescriptor)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } >= 0) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }
                output.write(buffer, 0, bytesRead)
            }

            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: IOException) {
            callback?.onWriteFailed(e.message)
        } finally {
            try {
                input?.close()
                output?.close()
            } catch (ignored: Exception) {
            }
        }
    }
}
