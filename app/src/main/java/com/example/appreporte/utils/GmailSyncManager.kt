package com.example.appreporte.utils

import com.example.appreporte.data.local.DatabaseHelper

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import java.util.Date
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import javax.mail.Folder
import javax.mail.Session
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

object GmailSyncManager {
    private const val TAG = "GmailSyncManager"
    private const val EMAIL = "notificacioneseduconnect2026@gmail.com"
    private const val PASSWORD = "nibr djya jbhc anoz"

    private fun getTextFromMessage(message: javax.mail.Message): String {
        return try {
            val content = message.content
            var fullText = when {
                content is String -> content
                content is MimeMultipart -> extractTextFromMultipart(content)
                else -> "Contenido en formato no soportado"
            }
            
            // Limpieza agresiva de hilos de respuesta (Gmail suele usar 'On ... wrote:' o 'El ... escribió:')
            val delimiters = listOf("On ", "El ", "---------- Forwarded message ---------", "<$EMAIL>")
            for (delimiter in delimiters) {
                if (fullText.contains(delimiter)) {
                    fullText = fullText.substringBefore(delimiter).trim()
                }
            }
            
            // Si después de limpiar queda vacío (ej. solo respondió con una imagen o adjunto), dar un aviso
            fullText.ifEmpty { "Respuesta recibida vía Gmail (consulte adjuntos si aplica)" }
        } catch (e: Exception) {
            Log.e(TAG, "Error al extraer texto: ${e.message}")
            "Contenido no disponible"
        }
    }

    private fun extractTextFromMultipart(mimeMultipart: MimeMultipart): String {
        var textResult = ""
        var htmlResult = ""
        try {
            val count = mimeMultipart.count
            for (i in 0 until count) {
                val bodyPart = mimeMultipart.getBodyPart(i)
                if (bodyPart.isMimeType("text/plain")) {
                    textResult += bodyPart.content.toString()
                } else if (bodyPart.isMimeType("multipart/*")) {
                    textResult += extractTextFromMultipart(bodyPart.content as MimeMultipart)
                } else if (bodyPart.isMimeType("text/html")) {
                    htmlResult += bodyPart.content.toString()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en multipart: ${e.message}")
        }
        
        return if (textResult.isNotEmpty()) textResult else {
            htmlResult.replace("<[^>]*>".toRegex(), " ").replace("\\s+".toRegex(), " ").trim()
        }
    }

    suspend fun syncReplies(context: Context) = withContext(Dispatchers.IO) {
        // Configuración de SSL para evitar CertPathValidatorException (Trust Anchor not found)
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val props = Properties().apply {
            setProperty("mail.store.protocol", "imaps")
            setProperty("mail.imaps.host", "imap.gmail.com")
            setProperty("mail.imaps.port", "993")
            setProperty("mail.imaps.ssl.enable", "true")
            setProperty("mail.imaps.timeout", "45000") // Aumentado para mayor estabilidad
            setProperty("mail.imaps.connectiontimeout", "45000")
            setProperty("mail.imaps.ssl.trust", "*")
            setProperty("mail.imaps.ssl.checkserveridentity", "false")
            setProperty("mail.imaps.ssl.socketFactory.fallback", "false")
            // Habilitar debug solo si es necesario para diagnóstico profundo
            // setProperty("mail.debug", "true")
        }

        try {
            val sslContext = SSLContext.getInstance("TLSv1.2") // Gmail prefiere TLS 1.2+
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sf = sslContext.socketFactory
            props.put("mail.imaps.ssl.socketFactory", sf)
            Log.d(TAG, "SSLSocketFactory TLSv1.2 configurado exitosamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error configurando SSLSocketFactory: ${e.message}")
        }

        val sharedPrefs = context.getSharedPreferences("GmailSyncPrefs", Context.MODE_PRIVATE)
        val lastSyncTime = sharedPrefs.getLong("last_sync_time", 0L)
        Log.d(TAG, "Iniciando sincronización. Último sync: ${Date(lastSyncTime)}")

        try {
            val session = Session.getInstance(props)
            val store = session.getStore("imaps")
            store.connect("imap.gmail.com", EMAIL, PASSWORD)

            val inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)

            val totalMessages = inbox.messageCount
            if (totalMessages == 0) {
                Log.d(TAG, "Buzón vacío.")
                inbox.close(false)
                store.close()
                return@withContext
            }

            // Aumentamos a los últimos 100 mensajes para no perder hilos antiguos reactivados
            val scanLimit = 100
            val start = (totalMessages - scanLimit + 1).coerceAtLeast(1)
            val messages = inbox.getMessages(start, totalMessages)
            
            val db = FirebaseFirestore.getInstance()
            var newestMessageTime = lastSyncTime

            // Procesamos TODOS los mensajes del rango escaneado (idempotencia vía Firestore ID)
            for (i in messages.size - 1 downTo 0) {
                val message = messages[i]
                try {
                    // Usamos receivedDate preferentemente para sincronización ya que es cuando llegó al servidor
                    val syncDate = message.receivedDate ?: message.sentDate ?: Date()
                    val messageTime = syncDate.time
                    
                    if (messageTime > newestMessageTime) {
                        newestMessageTime = messageTime
                    }

                    val from = message.from?.firstOrNull()?.toString() ?: ""
                    val subject = message.subject ?: ""
                    
                    // Solo procesamos si no es enviado por nosotros mismos
                    if (!from.contains(EMAIL, ignoreCase = true)) {
                        
                        // Somos más permisivos con el asunto para capturar todas las respuestas de padres
                        val content = getTextFromMessage(message)
                        val senderName = extractSenderName(from)
                        
                        val rawMessageId = try {
                            (message as? MimeMessage)?.messageID
                        } catch (e: Exception) { null }
                        
                        // Sanitización para Firestore (evitar caracteres prohibidos como '/')
                        val messageId = rawMessageId?.trim()?.removeSurrounding("<", ">")
                                        ?.replace("/", "_")?.replace(".", "_")
                                        ?: "msg_${syncDate.time}_${from.hashCode()}"

                        val notificationData = hashMapOf(
                            "recipient_email" to EMAIL.lowercase(),
                            "sender" to senderName,
                            "subject" to subject,
                            "message" to content.trim(),
                            "timestamp" to Timestamp(syncDate),
                            "type" to "gmail_reply",
                            "external_id" to messageId
                        )

                        // Guardar en Firestore usando el ID del mensaje para evitar duplicados
                        db.collection("notifications").document("gmail_$messageId")
                            .set(notificationData, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d(TAG, "Notificación guardada/actualizada: $subject de $senderName")
                            }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando mensaje ${messages[i].messageNumber}: ${e.message}")
                }
            }

            // Actualizar el puntero de sincronización al más nuevo encontrado
            sharedPrefs.edit().putLong("last_sync_time", newestMessageTime).apply()

            inbox.close(false)
            store.close()
            Log.d(TAG, "Sincronización finalizada exitosamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error crítico en sincronización Gmail: ${e.message}", e)
        }
    }

    private fun extractSenderName(from: String): String {
        return try {
            if (from.contains("<")) {
                var name = from.substringBefore("<").trim()
                if (name.startsWith("'") || name.startsWith("\"")) name = name.substring(1)
                if (name.endsWith("'") || name.endsWith("\"")) name = name.substring(0, name.length - 1)
                name.ifEmpty { from.substringAfter("<").substringBefore(">") }
            } else {
                from
            }
        } catch (e: Exception) {
            from
        }
    }
}
