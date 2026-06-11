package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var tvEmail: TextView
    private lateinit var tvInitials: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvSchool: TextView
    private lateinit var btnLogout: Button

    // Guardamos el rol y escuela recibidos por intent para navegación segura
    private var currentRole: String = "usuario"
    private var currentSchoolId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        tvEmail = findViewById(R.id.tvProfileEmail)
        tvInitials = findViewById(R.id.tvProfileInitials)
        tvRole = findViewById(R.id.chipProfileRole)
        tvSchool = findViewById(R.id.tvProfileSchool)
        btnLogout = findViewById(R.id.btnLogout)

        // Leer el rol y escuela que nos pasó la actividad anterior (sincrónico y confiable)
        currentRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        currentSchoolId = intent.getStringExtra("SCHOOL_ID") ?: ""

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Cargar el menú correcto según el rol
        bottomNav.menu.clear()
        when (currentRole.lowercase()) {
            "superadmin", "admin" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_admin)
            "docente" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_docente)
            else -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_padre)
        }
        bottomNav.selectedItemId = R.id.nav_perfil
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val targetActivity = when (currentRole.lowercase()) {
                        "superadmin" -> SuperAdminDashboardActivity::class.java
                        "admin" -> InicioActivity::class.java
                        "docente" -> DocenteDashboardActivity::class.java
                        else -> PadreDashboardActivity::class.java
                    }
                    val navIntent = Intent(this, targetActivity)
                    navIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    navIntent.putExtra("USER_ROL", currentRole)
                    navIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    navIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(navIntent)
                    finish()
                    true
                }
                R.id.nav_gestion -> {
                    val gestionIntent = Intent(this, AdminDashboardActivity::class.java)
                    gestionIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    gestionIntent.putExtra("USER_ROL", currentRole)
                    gestionIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    startActivity(gestionIntent)
                    finish()
                    true
                }
                R.id.nav_foro -> {
                    val foroIntent = Intent(this, ForoSalonesActivity::class.java)
                    foroIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    foroIntent.putExtra("USER_ROL", currentRole)
                    foroIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    startActivity(foroIntent)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    val targetAsistente = if (currentRole.lowercase() == "usuario") {
                        ChatbotPadreActivity::class.java
                    } else {
                        AsistenteActivity::class.java
                    }
                    val asistenteIntent = Intent(this, targetAsistente)
                    asistenteIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    asistenteIntent.putExtra("USER_ROL", currentRole)
                    asistenteIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    startActivity(asistenteIntent)
                    finish()
                    true
                }
                R.id.nav_reportes -> {
                    val targetActivity = when (currentRole.lowercase()) {
                        "docente" -> DocenteDashboardActivity::class.java
                        else -> PadreDashboardActivity::class.java
                    }
                    val navIntent = Intent(this, targetActivity)
                    navIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    navIntent.putExtra("USER_ROL", currentRole)
                    navIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    startActivity(navIntent)
                    finish()
                    true
                }
                R.id.nav_perfil -> true
                else -> true
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val logoutIntent = Intent(this, MainActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }

        loadProfileData()
    }

    private fun loadProfileData() {
        val intentEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        if (intentEmail.isNotEmpty()) {
            tvEmail.text = intentEmail
            tvInitials.text = intentEmail.take(1).uppercase()
            FirebaseFirestore.getInstance().collection("users").document(intentEmail).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val rolFromFirestore = doc.getString("rol") ?: currentRole
                        tvRole.text = rolFromFirestore.uppercase()
                        
                        val schoolFromFirestore = doc.getString("school_id")
                        if (schoolFromFirestore != null) {
                            currentSchoolId = schoolFromFirestore
                            tvSchool.text = schoolFromFirestore
                        } else {
                            tvSchool.text = currentSchoolId.ifEmpty { "Sin Colegio" }
                        }
                    } else {
                        tvRole.text = currentRole.uppercase()
                        tvSchool.text = currentSchoolId.ifEmpty { "Sin Colegio" }
                    }
                }
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val email = user.email ?: ""
                tvEmail.text = email
                tvInitials.text = email.take(1).uppercase()
                FirebaseFirestore.getInstance().collection("users").document(email).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val rolFromFirestore = doc.getString("rol") ?: currentRole
                            tvRole.text = rolFromFirestore.uppercase()
                            
                            val schoolFromFirestore = doc.getString("school_id")
                            if (schoolFromFirestore != null) {
                                currentSchoolId = schoolFromFirestore
                                tvSchool.text = schoolFromFirestore
                            } else {
                                tvSchool.text = currentSchoolId.ifEmpty { "Sin Colegio" }
                            }
                        }
                    }
            }
        }
    }
}