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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // INYECCIÓN DE DATOS DE PRUEBA DESACTIVADA POR SEGURIDAD
        // MockDataInjector.injectData()

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

            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            binding.btnIngresar.isEnabled = false

            // --- BYPASS DE LOGIN DE PRUEBA REMOVIDO POR SEGURIDAD ---

            // Usuarios por defecto para sembrar (seeding) la BD
            val seedUsers = listOf(
                Triple("admin@reporte.com", "admin123", "admin"),
                Triple("user@reporte.com", "user123", "usuario"),
                Triple("docente@reporte.com", "docente123", "docente")
            )

            // Primero verificamos en Firestore (para usuarios creados por el Admin)
            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.getString("password") == password) {
                        binding.btnIngresar.isEnabled = true
                        val role = document.getString("rol") ?: "usuario"
                        val schoolId = document.getString("school_id") ?: "Colegio San José"
                        navigateToSplash(role, email, schoolId)
                    } else {
                        // Si no está en Firestore o la contraseña no coincide, intentamos con Auth normal
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                db.collection("users").document(email).get()
                                    .addOnSuccessListener { doc2 ->
                                        binding.btnIngresar.isEnabled = true
                                        if (doc2.exists()) {
                                            val role = doc2.getString("rol") ?: "usuario"
                                            var schoolId = doc2.getString("school_id")
                                            if (schoolId == null) {
                                                schoolId = "Colegio San José"
                                                db.collection("users").document(email).update("school_id", schoolId)
                                            }
                                            navigateToSplash(role, email, schoolId)
                                        } else {
                                            Toast.makeText(this@MainActivity, "Rol no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                            .addOnFailureListener {
                                val seedUser = seedUsers.find { it.first == email && it.second == password }
                                if (seedUser != null) {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener {
                                            val userMap = hashMapOf(
                                                "email" to email,
                                                "password" to password,
                                                "rol" to seedUser.third,
                                                "phone" to "",
                                                "school_id" to "Colegio San José"
                                            )
                                            db.collection("users").document(email).set(userMap)
                                                .addOnSuccessListener {
                                                    binding.btnIngresar.isEnabled = true
                                                    navigateToSplash(seedUser.third, email, "Colegio San José")
                                                }
                                        }
                                } else {
                                    binding.btnIngresar.isEnabled = true
                                    binding.tvErrorMessage.visibility = View.VISIBLE
                                    binding.tvErrorMessage.text = "Credenciales incorrectas"
                                    binding.tilPassword.error = " "
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    binding.btnIngresar.isEnabled = true
                    Toast.makeText(this@MainActivity, "Error conectando a Firebase", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToSplash(role: String, email: String, schoolId: String) {
        val intent = Intent(this, LoadingActivity::class.java)
        intent.putExtra("USER_ROL", role)
        intent.putExtra("USER_EMAIL", email)
        intent.putExtra("SCHOOL_ID", schoolId)
        startActivity(intent)
        finish()
    }
}