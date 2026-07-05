package com.example.appreporte.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.appreporte.ui.activities.padre.PadreDashboardActivity
import com.example.appreporte.workers.EmailNotificationWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // CA1 & CA3: Encolar el procesamiento de la notificación para envío por Gmail
        val studentName = remoteMessage.data["student_name"] ?: "Estudiante"
        val message = remoteMessage.data["message"] ?: "Nueva notificación académica disponible."
        val parentEmail = remoteMessage.data["parent_email"] ?: ""

        if (parentEmail.isNotEmpty()) {
            scheduleEmailNotification(studentName, message, parentEmail)
        }

        showNotification(remoteMessage.notification?.title ?: "Aviso Académico", 
                         remoteMessage.notification?.body ?: message)
    }

    private fun scheduleEmailNotification(studentName: String, message: String, email: String) {
        val data = Data.Builder()
            .putString("student_name", studentName)
            .putString("subject", "Notificación de EduConnect")
            .putString("message", message)
            .putString("recipient_email", email)
            .build()

        // CA3 & CA4: Uso de WorkManager para entrega ordenada y sin afectar el rendimiento
        val workRequest = OneTimeWorkRequestBuilder<EmailNotificationWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "academic_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notificaciones Académicas", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, PadreDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // CA5: Mapear el token FCM al usuario actual en la base de datos
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        user?.email?.let { email ->
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(email)
                .update("fcmToken", token)
        }
    }
}
