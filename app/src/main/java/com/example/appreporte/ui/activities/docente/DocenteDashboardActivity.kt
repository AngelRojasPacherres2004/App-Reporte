package com.example.appreporte.ui.activities.docente

import com.example.appreporte.R
import com.example.appreporte.ui.activities.common.PerfilActivity
import com.example.appreporte.ui.activities.common.NotificacionesActivity
import com.example.appreporte.ui.activities.common.DirectChatActivity
import com.example.appreporte.ui.activities.foro.ForoSalonesActivity
import com.example.appreporte.utils.NavigationUtils
import com.example.appreporte.utils.GmailSyncManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appreporte.databinding.ActivityDashboardDocenteBinding

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DocenteDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardDocenteBinding
    private var userRole: String = "docente"
    private var schoolId: String = "Colegio San José"
    private var userEmail: String = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = intent.getStringExtra(NavigationUtils.USER_ROL) ?: "docente"
        schoolId = intent.getStringExtra(NavigationUtils.SCHOOL_ID) ?: "Colegio San José"
        userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL) ?: ""

        setupBottomNavigation()
        setupClickListeners()
        setupDocenteProfile()
        loadRealTimeData()
        syncGmailNotifications()
    }

    private fun syncGmailNotifications() {
        lifecycleScope.launch {
            GmailSyncManager.syncReplies(this@DocenteDashboardActivity)
        }
    }

    private fun loadRealTimeData() {
        if (userEmail.isEmpty()) return

        // 1. Cargar conteo de quejas pendientes
        db.collection("complaints")
            .whereEqualTo("status", "en proceso")
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                binding.tvPendingComplaintsCount.text = count.toString()
            }

        // 2. Cargar conteo de notificaciones sin leer
        db.collection("users").document(userEmail).get().addOnSuccessListener { doc ->
            val schoolEmail = "notificacioneseduconnect2026@gmail.com"
            val gmailReportes = (doc.getString("correo_reportes") ?: userEmail).lowercase()
            val primaryEmail = userEmail.lowercase()
            
            val emails = mutableListOf(gmailReportes)
            if (primaryEmail != gmailReportes) emails.add(primaryEmail)
            if (!emails.contains(schoolEmail)) emails.add(schoolEmail)

            db.collection("notifications")
                .whereIn("recipient_email", emails)
                .addSnapshotListener { snapshot, _ ->
                    val count = snapshot?.size() ?: 0
                    binding.tvNotifPreviewCount.text = if (count > 0) {
                        "Tienes $count alertas y comunicados nuevos."
                    } else {
                        "No hay nuevas notificaciones."
                    }
                }
        }

        // 3. Cargar última publicación del foro
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null) {
                    binding.tvForumPostTitle.text = doc.getString("title") ?: "Sin título"
                    val author = doc.getString("author") ?: "Docente"
                    binding.tvForumPostMeta.text = "Publicado por $author"
                } else {
                    binding.tvForumPostTitle.text = "Sin publicaciones"
                    binding.tvForumPostMeta.text = "El foro está vacío"
                }
            }
    }

    private fun setupDocenteProfile() {
        binding.ivDocenteProfile.setOnClickListener {
            val userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL)
            NavigationUtils.navigateToPerfil(this, userEmail, "docente", schoolId)
        }
    }

    private fun setupClickListeners() {
        val userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL)
        // Lógica para el botón "CREAR PUBLICACIÓN" de la tarjeta azul
        binding.root.findViewById<android.view.View>(R.id.btnCreatePost)?.setOnClickListener {
            NavigationUtils.navigateToForo(this, userEmail, userRole, schoolId)
        }

        // Botón de Notificaciones
        binding.root.findViewById<android.view.View>(R.id.btnVerNotificacionesDocente)?.setOnClickListener {
            NavigationUtils.navigateToNotificaciones(this, userEmail)
        }

        binding.btnAssignGradesDocente.setOnClickListener {
            NavigationUtils.navigateToGestionReportes(this, schoolId, userEmail, userRole)
        }

        // Botón "REVISAR QUEJAS" de la Welcome Card
        binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReviewComplaints)?.setOnClickListener {
            NavigationUtils.navigateToComplaints(this, userEmail, userRole, schoolId)
        }

        // Add a listener to a potential button for Mensajes (we'll add it in XML next)
        binding.root.findViewById<android.view.View>(R.id.btnInboxDocente)?.setOnClickListener {
            showInboxDialog()
        }
    }

    private fun showInboxDialog() {
        val userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL) ?: return
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("direct_chats")
            .whereArrayContains("participants", userEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    android.widget.Toast.makeText(this, "No tienes mensajes nuevos", android.widget.Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val options = snapshot.documents.map { doc ->
                    val participants = doc.get("participants") as? List<String> ?: emptyList()
                    val otherEmail = participants.find { it != userEmail } ?: "Desconocido"
                    otherEmail
                }.toTypedArray()

                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Buzón de Mensajes")
                    .setItems(options) { _, which ->
                        val targetEmail = options[which]
                        NavigationUtils.navigateToDirectChat(this, userEmail, targetEmail)
                    }
                    .show()
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(this, "Error al cargar mensajes", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_reportes -> {
                    NavigationUtils.navigateToGestionReportes(this, schoolId, userEmail, userRole)
                    true
                }
                R.id.nav_foro -> {
                    NavigationUtils.navigateToForo(this, userEmail, userRole, schoolId)
                    true
                }
                R.id.nav_asistente -> {
                    NavigationUtils.navigateToAsistente(this, userEmail, null)
                    true
                }
                R.id.nav_perfil -> {
                    NavigationUtils.navigateToPerfil(this, userEmail, userRole, schoolId)
                    true
                }
                else -> false
            }
        }
    }
}
