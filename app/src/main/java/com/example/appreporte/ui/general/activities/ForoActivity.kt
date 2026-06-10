package com.example.appreporte.ui.general

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import android.widget.ImageButton

class ForoActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<UIMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foro)

        val rv = findViewById<RecyclerView>(R.id.rvForumMessages)
        chatAdapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = chatAdapter

        // Dummy messages
        messages.add(UIMessage("Padre Carlos", "¿Alguien sabe si mañana hay clases normales?", false))
        messages.add(UIMessage("Prof. María", "Sí, el horario es el habitual de 8am a 2pm.", false))
        messages.add(UIMessage("Tú", "Perfecto, gracias por la confirmación.", true))
        chatAdapter.notifyDataSetChanged()

        findViewById<ImageButton>(R.id.btnSend).setOnClickListener {
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
        bottomNav.selectedItemId = R.id.nav_foro
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_gestion -> {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    startActivity(Intent(this, AsistenteActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", intent.getStringExtra("USER_EMAIL") ?: "")
                    perfilIntent.putExtra("USER_ROL", intent.getStringExtra("USER_ROL") ?: "admin")
                    perfilIntent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID") ?: "")
                    startActivity(perfilIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> true
            }
        }
    }
}

