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

        setupBottomNavigation()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        val userEmail = intent.getStringExtra("USER_EMAIL")
        // Lógica para el botón "CREAR PUBLICACIÓN" de la tarjeta azul
        binding.root.findViewById<android.view.View>(R.id.btnCreatePost)?.setOnClickListener {
            val intent = Intent(this, ForoSalonesActivity::class.java)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val userEmail = intent.getStringExtra("USER_EMAIL")
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_reportes -> true
                R.id.nav_foro -> {
                    val intent = Intent(this, ForoSalonesActivity::class.java)
                    intent.putExtra("USER_ROL", userRole)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                R.id.nav_asistente -> true
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }
}
