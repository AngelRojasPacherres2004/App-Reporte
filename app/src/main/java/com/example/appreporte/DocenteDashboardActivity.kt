package com.example.appreporte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appreporte.databinding.ActivityDashboardDocenteBinding

class DocenteDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardDocenteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_reportes -> true
                R.id.nav_foro -> true
                R.id.nav_asistente -> true
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }
}