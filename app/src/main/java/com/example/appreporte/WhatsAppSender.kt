package com.example.appreporte

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object WhatsAppSender {

    fun sendPdfToWhatsApp(context: Context, phone: String, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        // Clean phone number (remove +, spaces, etc if necessary, but keep it as specified in international format)
        val cleanPhone = phone.replace("+", "").replace(" ", "")

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra("jid", "$cleanPhone@s.whatsapp.net") // This might work on some versions
        intent.setPackage("com.whatsapp")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(Intent.createChooser(intent, "Compartir reporte vía WhatsApp"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
