package com.example.appreporte.ui.activities.common

import com.example.appreporte.R
import com.example.appreporte.utils.GmailSyncManager
import com.example.appreporte.utils.NavigationUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var rvNotificaciones: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: NotificacionesAdapter
    private var userEmail: String = ""
    private var gmailReportes: String = ""
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL) ?: ""
        
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        rvNotificaciones = findViewById(R.id.rvNotificaciones)
        rvNotificaciones.layoutManager = LinearLayoutManager(this)
        
        swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshNotif)
        swipeRefresh.setOnRefreshListener {
            syncGmailReplies()
        }
        
        adapter = NotificacionesAdapter(emptyList())
        rvNotificaciones.adapter = adapter

        fetchUserGmailAndLoad()
        syncGmailReplies()
    }

    private fun syncGmailReplies() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            GmailSyncManager.syncReplies(this@NotificacionesActivity)
            swipeRefresh.isRefreshing = false
        }
    }

    private fun fetchUserGmailAndLoad() {
        if (userEmail.isEmpty()) return

        FirebaseFirestore.getInstance().collection("users").document(userEmail)
            .get()
            .addOnSuccessListener { doc ->
                gmailReportes = doc.getString("correo_reportes") ?: userEmail
                userRole = doc.getString("rol") ?: ""
                loadNotifications()
            }
    }

    private fun loadNotifications() {
        if (gmailReportes.isEmpty()) return

        val schoolEmail = "notificacioneseduconnect2026@gmail.com"
        val emails = mutableListOf(gmailReportes.lowercase())
        
        if (userEmail.isNotEmpty() && userEmail.lowercase() != gmailReportes.lowercase()) {
            emails.add(userEmail.lowercase())
        }
        
        // Solo incluir el correo de la institución si es docente para que vea las respuestas de Gmail
        if (userRole == "docente" && !emails.contains(schoolEmail)) {
            emails.add(schoolEmail)
        }

        FirebaseFirestore.getInstance().collection("notifications")
            .whereIn("recipient_email", emails)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap<String, Any>()
                        NotificationItem(
                            asunto = data["subject"] as? String ?: "Sin asunto",
                            mensaje = data["message"] as? String ?: "",
                            fecha = data["timestamp"] as? Timestamp,
                            sender = data["sender"] as? String ?: "EduConnect - Institución",
                            type = data["type"] as? String ?: "general"
                        )
                    }.sortedByDescending { it.fecha?.seconds ?: 0L }

                    adapter.updateData(list)
                }
            }
    }

    data class NotificationItem(
        val asunto: String,
        val mensaje: String,
        val fecha: Timestamp?,
        val sender: String,
        val type: String
    )

    inner class NotificacionesAdapter(private var items: List<NotificationItem>) :
        RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

        fun updateData(newItems: List<NotificationItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacion, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvAsunto.text = item.asunto
            holder.tvMensaje.text = item.mensaje
            holder.tvSender.text = item.sender
            holder.tvInitial.text = item.sender.take(1).uppercase()
            
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateSdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            
            val date = item.fecha?.toDate()
            if (date != null) {
                val now = Calendar.getInstance()
                val msgDate = Calendar.getInstance().apply { time = date }
                
                if (now.get(Calendar.DATE) == msgDate.get(Calendar.DATE)) {
                    holder.tvFecha.text = sdf.format(date)
                } else {
                    holder.tvFecha.text = dateSdf.format(date)
                }
            } else {
                holder.tvFecha.text = ""
            }
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvAsunto: TextView = view.findViewById(R.id.tvNotifAsunto)
            val tvMensaje: TextView = view.findViewById(R.id.tvNotifMensaje)
            val tvFecha: TextView = view.findViewById(R.id.tvNotifFecha)
            val tvSender: TextView = view.findViewById(R.id.tvNotifSender)
            val tvInitial: TextView = view.findViewById(R.id.tvNotifInitial)
        }
    }
}
