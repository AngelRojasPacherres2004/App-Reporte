package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appreporte.databinding.ActivityDashboardPadreBinding

class PadreDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardPadreBinding
    private lateinit var dbHelper: DatabaseHelper
    private var userEmail: String = ""
    private var userRole: String = "usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setupUIByRole(userRole)
        setupBottomNavigation()
        setupClickListeners()
    }

    private fun setupUIByRole(role: String) {
        binding.tvWelcomeTitle.text = getString(R.string.welcome_parent)
        binding.tvWelcomeDesc.text = getString(R.string.welcome_parent_desc)
    }

    private fun setupClickListeners() {
        binding.btnViewForo.setOnClickListener {
            navigateToForo()
        }
    }

    private fun navigateToForo() {
        val classrooms = dbHelper.getUserClassroomsWithNames(userEmail)
        if (classrooms.size == 1) {
            val intent = Intent(this, ForoDetalleActivity::class.java)
            intent.putExtra("SALON_NAME", classrooms[0].second)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        } else {
            val intent = Intent(this, ForoSalonesActivity::class.java)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_inicio
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_foro -> {
                    navigateToForo()
                    true
                }
                R.id.nav_reportes -> {
                    Toast.makeText(this, "Reportes en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_perfil -> {
                    Toast.makeText(this, "Perfil en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}
