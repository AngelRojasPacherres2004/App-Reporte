package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appreporte.databinding.ActivityDashboardDocenteBinding

class DocenteDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardDocenteBinding
    private var userRole: String = "docente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = intent.getStringExtra("USER_ROL") ?: "docente"
        val schoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"

        setupBottomNavigation()
        setupClickListeners()
        setupDocenteProfile()
    }

    private fun setupDocenteProfile() {
        binding.ivDocenteProfile.setOnClickListener {
            val userEmail = intent.getStringExtra("USER_EMAIL")
            val perfilIntent = Intent(this, PerfilActivity::class.java)
            perfilIntent.putExtra("USER_EMAIL", userEmail)
            perfilIntent.putExtra("USER_ROL", "docente")
            perfilIntent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID") ?: "")
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
            intent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID"))
            startActivity(intent)
        }

        // Botones de Horario y Asistencia
        binding.root.findViewById<android.view.View>(R.id.btnDocenteHorario)?.setOnClickListener {
            val intent = Intent(this, DocenteHorarioActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }

        binding.root.findViewById<android.view.View>(R.id.btnDocenteAsistencia)?.setOnClickListener {
            val intent = Intent(this, DocenteAsistenciaActivity::class.java)
            intent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID"))
            startActivity(intent)
        }

        binding.btnAssignGradesDocente.setOnClickListener {
            val intent = Intent(this, GestionReportesSalonesActivity::class.java)
            intent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID"))
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
                    intent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID"))
                    startActivity(intent)
                    true
                }
                R.id.nav_foro -> {
                    val intent = Intent(this, ForoSalonesActivity::class.java)
                    intent.putExtra("USER_ROL", userRole)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID"))
                    startActivity(intent)
                    true
                }
                R.id.nav_asistente -> true
                R.id.nav_perfil -> {
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", userEmail)
                    perfilIntent.putExtra("USER_ROL", "docente")
                    perfilIntent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID") ?: "")
                    startActivity(perfilIntent)
                    true
                }
                else -> false
            }
        }
    }
}
