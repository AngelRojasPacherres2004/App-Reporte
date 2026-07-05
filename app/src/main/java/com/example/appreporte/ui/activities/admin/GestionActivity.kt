package com.example.appreporte.ui.activities.admin

import com.example.appreporte.R
import com.example.appreporte.ui.activities.foro.ForoSalonesActivity
import com.example.appreporte.ui.activities.padre.ChatbotPadreActivity
import com.example.appreporte.utils.NavigationUtils
import com.example.appreporte.ui.activities.common.PerfilActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class GestionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_gestion
        bottomNav.setOnItemSelectedListener { item ->
            val userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL)
            val schoolId = intent.getStringExtra(NavigationUtils.SCHOOL_ID)
            when (item.itemId) {
                R.id.nav_inicio -> {
                    NavigationUtils.navigateToDashboard(this, userEmail, "admin", schoolId)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_foro -> {
                    NavigationUtils.navigateToForo(this, userEmail, "admin", schoolId)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    NavigationUtils.navigateToAsistente(this, userEmail, null)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    NavigationUtils.navigateToPerfil(this, userEmail, "admin", schoolId)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> true
            }
        }
    }
}
