package com.example.appreporte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appreporte.databinding.ActivityDashboardPadreBinding

class PadreDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardPadreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRole = intent.getStringExtra("USER_ROL") ?: "padre"
        setupUIByRole(userRole)
    }

    private fun setupUIByRole(role: String) {
        // En esta actividad (Padre) solo manejamos la UI base de usuario/padre
        binding.tvWelcomeTitle.text = getString(R.string.welcome_parent)
        binding.tvWelcomeDesc.text = getString(R.string.welcome_parent_desc)
    }
}