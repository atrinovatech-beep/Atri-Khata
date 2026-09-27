package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.InvoiceAttachmentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object AttachmentUtils {

    fun saveUriToInternalStorage(
        context: Context,
        uri: Uri,
        forcedPdf: Boolean = false
    ): InvoiceAttachmentEntity? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: if (forcedPdf) "application/pdf" else "image/jpeg"
            val isPdf = forcedPdf || mimeType.contains("pdf", ignoreCase = true)

            // Resolve file name
            var resolvedName: String? = null
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    resolvedName = cursor.getString(nameIndex)
                }
            }

            val extension = if (isPdf) {
                "pdf"
            } else {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            }

            val finalFileName = resolvedName ?: "doc_${System.currentTimeMillis()}.$extension"

            // Save to internal storage: filesDir/attachments/
            val attachmentsDir = File(context.filesDir, "attachments")
            if (!attachmentsDir.exists()) {
                attachmentsDir.mkdirs()
            }

            val uniqueFileName = "${System.currentTimeMillis()}_$finalFileName"
            val destFile = File(attachmentsDir, uniqueFileName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            InvoiceAttachmentEntity(
                invoiceId = 0L,
                fileName = finalFileName,
                fileType = if (isPdf) "PDF" else "IMAGE",
                mimeType = mimeType,
                uriString = destFile.absolutePath,
                fileSize = destFile.length(),
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFile(attachment: InvoiceAttachmentEntity): File? {
        val file = File(attachment.uriString)
        return if (file.exists()) file else null
    }

    fun openAttachment(context: Context, attachment: InvoiceAttachmentEntity) {
        val file = getFile(attachment)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Attachment file not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, attachment.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAttachment(context: Context, attachment: InvoiceAttachmentEntity) {
        val file = getFile(attachment)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Attachment file not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = attachment.mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Attachment"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to share attachment", Toast.LENGTH_SHORT).show()
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
    }
}
