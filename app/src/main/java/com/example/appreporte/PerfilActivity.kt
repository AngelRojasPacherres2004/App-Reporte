package com.example.appreporte

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appreporte.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        displayUserData(userEmail)

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun displayUserData(email: String) {
        val userData = dbHelper.getUserData(email)
        if (userData != null) {
            binding.tvProfileEmail.text = userData["email"]
            binding.tvProfileRole.text = userData["rol"]?.replaceFirstChar { it.uppercase() }
            binding.tvProfilePhone.text = userData["phone"]
        }
    }

    private fun logout() {
        // Limpiar SharedPreferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Regresar al Login
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}