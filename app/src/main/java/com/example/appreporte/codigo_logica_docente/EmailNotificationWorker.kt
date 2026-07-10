package com.example.appreporte.codigo_logica_docente


import com.example.appreporte.R
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailNotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val studentName = inputData.getString("student_name") ?: "Estudiante"
        val subject = inputData.getString("subject") ?: "Notificación Académica"
        val messageText = inputData.getString("message") ?: ""
        val recipientEmail = inputData.getString("recipient_email") ?: ""
        val attachmentPath = inputData.getString("attachment_path")

        if (recipientEmail.isEmpty()) {
            Log.e("EmailWorker", "No se proporcionó correo del destinatario")
            return Result.failure()
        }

        val props = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.socketFactory.port", "465")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.auth", "true")
            put("mail.smtp.port", "465")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.connectiontimeout", "10000")
        }

        val senderEmail = "notificacioneseduconnect2026@gmail.com"
        val senderPassword = "nibr djya jbhc anoz"

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        return try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                setSubject("EduConnect: $subject")
            }

            val multipart = MimeMultipart()
            val textPart = MimeBodyPart().apply { setText(messageText) }
            multipart.addBodyPart(textPart)

            attachmentPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val attachmentPart = MimeBodyPart().apply {
                        dataHandler = DataHandler(FileDataSource(file))
                        fileName = file.name
                    }
                    multipart.addBodyPart(attachmentPart)
                }
            }

            mimeMessage.setContent(multipart)
            Transport.send(mimeMessage)
            Log.d("EmailWorker", "¡Correo enviado con éxito a $recipientEmail!")
            Result.success()
        } catch (e: Exception) {
            Log.e("EmailWorker", "Error al enviar correo: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

