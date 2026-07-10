package com.example.appreporte.codigo_vista_pantalla_padre

import com.example.appreporte.codigo_logica_padre.ChatAdapter
import com.example.appreporte.codigo_logica_padre.UIMessage
import com.example.appreporte.codigo_vista_pantalla_login.InicioActivity
import com.example.appreporte.codigo_vista_pantalla_login.PerfilActivity
import com.example.appreporte.codigo_vista_pantalla_login.AdminDashboardActivity


import com.example.appreporte.R
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import com.google.android.material.button.MaterialButton

class ForoActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<UIMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foro)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvForumMessages)
        chatAdapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = chatAdapter

        // Dummy messages
        messages.add(UIMessage("Padre Carlos", "¿Alguien sabe si mañana hay clases normales?", false))
        messages.add(UIMessage("Prof. María", "Sí, el horario es el habitual de 8am a 2pm.", false))
        messages.add(UIMessage("Tú", "Perfecto, gracias por la confirmación.", true))
        chatAdapter.notifyDataSetChanged()

        findViewById<MaterialButton>(R.id.btnSend).setOnClickListener {
            val et = findViewById<EditText>(R.id.etMessage)
            val txt = et.text.toString()
            if (txt.isNotEmpty()) {
                messages.add(UIMessage("Tú", txt, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                rv.scrollToPosition(messages.size - 1)
                et.text.clear()
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        val userRole = intent.getStringExtra("USER_ROL")?.lowercase() ?: "usuario"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        val schoolId = intent.getStringExtra("SCHOOL_ID") ?: ""

        bottomNav.menu.clear()
        when (userRole) {
            "admin", "superadmin" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_admin)
            "docente" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_docente)
            else -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_padre)
        }

        if (userRole != "admin" && userRole != "superadmin") {
            bottomNav.selectedItemId = R.id.nav_foro
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val targetActivity = when (userRole) {
                        "admin" -> InicioActivity::class.java
                        "docente" -> com.example.appreporte.codigo_vista_pantalla_docente.DocenteDashboardActivity::class.java
                        else -> PadreDashboardActivity::class.java
                    }
                    val intent = Intent(this, targetActivity)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", userRole)
                    intent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_gestion -> {
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    val intent = Intent(this, AsistenteActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", userRole)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", userEmail)
                    perfilIntent.putExtra("USER_ROL", userRole)
                    perfilIntent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(perfilIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_foro -> true
                else -> true
            }
        }
    }
}

