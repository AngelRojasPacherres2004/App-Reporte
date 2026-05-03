package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appreporte.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedRole = "admin" // Default selection
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupProfileSelection()

        // 1. Revisamos en qué modo está la app actualmente
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES

        // 2. Dependiendo del modo, mostramos el ícono correcto (Sol o Luna)
        if (isNightMode) {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_sun)
        } else {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_moon)
        }

        // 3. Lógica para cambiar de modo al hacer clic en el ícono
        binding.ivThemeToggle.setOnClickListener {
            if (isNightMode) {
                // Si estaba oscuro, pasamos a claro
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                // Si estaba claro, pasamos a oscuro
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        // Lógica del botón de inicio de sesión
        binding.btnIngresar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userRole = dbHelper.checkUser(email, password)
            if (userRole != null) {
                navigateToSplash(userRole, email)
            } else {
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tilPassword.error = " "
            }
        }
    }

    private fun setupProfileSelection() {
        binding.cardAdmin.setOnClickListener {
            selectProfile("admin")
        }

        binding.cardUser.setOnClickListener {
            selectProfile("usuario")
        }
    }

    private fun selectProfile(role: String) {
        selectedRole = role

        // Reset all
        val unselectedBg = ContextCompat.getColor(this, R.color.profile_unselected)
        val onSurface = ContextCompat.getColor(this, R.color.on_surface)
        val primary = ContextCompat.getColor(this, R.color.primary)
        val onPrimary = ContextCompat.getColor(this, R.color.on_primary)

        binding.cardAdmin.setCardBackgroundColor(unselectedBg)
        binding.ivAdminIcon.setColorFilter(onSurface)
        binding.tvAdminText.setTextColor(onSurface)

        binding.cardUser.setCardBackgroundColor(unselectedBg)
        binding.ivUserIcon.setColorFilter(onSurface)
        binding.tvUserText.setTextColor(onSurface)

        // Select one
        when (role) {
            "admin" -> {
                binding.cardAdmin.setCardBackgroundColor(primary)
                binding.ivAdminIcon.setColorFilter(onPrimary)
                binding.tvAdminText.setTextColor(onPrimary)
            }
            "usuario" -> {
                binding.cardUser.setCardBackgroundColor(primary)
                binding.ivUserIcon.setColorFilter(onPrimary)
                binding.tvUserText.setTextColor(onPrimary)
            }
        }
    }

    private fun navigateToSplash(role: String, email: String) {
        val intent = Intent(this, LoadingActivity::class.java)
        intent.putExtra("USER_ROL", role)
        intent.putExtra("USER_EMAIL", email)
        startActivity(intent)
        finish()
    }
}