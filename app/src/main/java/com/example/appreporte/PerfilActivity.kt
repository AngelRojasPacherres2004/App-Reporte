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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        tvEmail = findViewById(R.id.tvProfileEmail)
        tvInitials = findViewById(R.id.tvProfileInitials)
        tvRole = findViewById(R.id.chipProfileRole)
        tvSchool = findViewById(R.id.tvProfileSchool)
        btnLogout = findViewById(R.id.btnLogout)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_perfil
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_foro -> {
                    startActivity(Intent(this, ForoActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadProfileData()
    }

    private fun loadProfileData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val email = user.email ?: ""
            tvEmail.text = email
            tvInitials.text = email.take(1).uppercase()

            FirebaseFirestore.getInstance().collection("users").document(email).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        tvRole.text = doc.getString("rol")?.uppercase() ?: "ADMINISTRADOR"
                        tvSchool.text = doc.getString("school_id") ?: "Sin Colegio"
                    }
                }
        }
    }
}