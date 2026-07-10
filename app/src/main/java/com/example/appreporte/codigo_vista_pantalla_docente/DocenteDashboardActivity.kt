package com.example.appreporte.codigo_vista_pantalla_docente

import com.example.appreporte.codigo_vista_pantalla_padre.ForoSalonesActivity
import com.example.appreporte.codigo_vista_pantalla_padre.DirectChatActivity
import com.example.appreporte.codigo_vista_pantalla_padre.ComplaintsActivity
import com.example.appreporte.codigo_vista_pantalla_padre.AsistenteActivity
import com.example.appreporte.codigo_vista_pantalla_login.PerfilActivity
import com.example.appreporte.codigo_vista_pantalla_login.NotificacionesActivity
import com.example.appreporte.codigo_logica_login.GmailSyncManager


import com.example.appreporte.R
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

        userRole = intent.getStringExtra("USER_ROL") ?: "docente"
        schoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

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
            val userEmail = intent.getStringExtra("USER_EMAIL")
            val perfilIntent = Intent(this, PerfilActivity::class.java)
            perfilIntent.putExtra("USER_EMAIL", userEmail)
            perfilIntent.putExtra("USER_ROL", "docente")
            perfilIntent.putExtra("SCHOOL_ID", schoolId)
            startActivity(perfilIntent)
        }
    }

    private fun setupClickListeners() {
        val userEmail = intent.getStringExtra("USER_EMAIL")
        // Lógica para el botón "CREAR PUBLICACIÓN" de la tarjeta azul
        binding.root.findViewById<android.view.View>(R.id.btnCreatePost)?.setOnClickListener {
            val intent = Intent(this, ForoSalonesActivity::class.java)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            intent.putExtra("SCHOOL_ID", schoolId)
            startActivity(intent)
        }

        // Botón de Notificaciones
        binding.root.findViewById<android.view.View>(R.id.btnVerNotificacionesDocente)?.setOnClickListener {
            val intent = Intent(this, NotificacionesActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }

        binding.btnAssignGradesDocente.setOnClickListener {
            val intent = Intent(this, GestionReportesSalonesActivity::class.java)
            intent.putExtra("SCHOOL_ID", schoolId)
            startActivity(intent)
        }

        // Botón "REVISAR QUEJAS" de la Welcome Card
        binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReviewComplaints)?.setOnClickListener {
            val intent = Intent(this, ComplaintsActivity::class.java)
            startActivity(intent)
        }

        // Add a listener to a potential button for Mensajes (we'll add it in XML next)
        binding.root.findViewById<android.view.View>(R.id.btnInboxDocente)?.setOnClickListener {
            showInboxDialog()
        }
    }

    private fun showInboxDialog() {
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: return
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
                        val intent = Intent(this, DirectChatActivity::class.java)
                        intent.putExtra("CURRENT_EMAIL", userEmail)
                        intent.putExtra("TARGET_EMAIL", targetEmail)
                        startActivity(intent)
                    }
                    .show()
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(this, "Error al cargar mensajes", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        val userEmail = intent.getStringExtra("USER_EMAIL")
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_reportes -> {
                    val intent = Intent(this, GestionReportesSalonesActivity::class.java)
                    intent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(intent)
                    true
                }
                R.id.nav_foro -> {
                    val intent = Intent(this, ForoSalonesActivity::class.java)
                    intent.putExtra("USER_ROL", userRole)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(intent)
                    true
                }
                R.id.nav_asistente -> {
                    val intent = Intent(this, AsistenteActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", "docente")
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", userEmail)
                    perfilIntent.putExtra("USER_ROL", "docente")
                    perfilIntent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(perfilIntent)
                    true
                }
                else -> false
            }
        }
    }
}

