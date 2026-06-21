package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import android.widget.ImageButton

class AsistenteActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<UIMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistente)

        val rv = findViewById<RecyclerView>(R.id.rvBotMessages)
        chatAdapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = chatAdapter

        // Dummy bot greeting
        messages.add(UIMessage("EduConnect IA", "¡Hola! Soy tu asistente de inteligencia artificial. Puedo ayudarte a redactar comunicados, resumir el estado de un salón, o crear reportes masivos. ¿En qué te puedo ayudar hoy?", false))
        chatAdapter.notifyDataSetChanged()

        findViewById<ImageButton>(R.id.btnSendBot).setOnClickListener {
            val et = findViewById<EditText>(R.id.etMessageBot)
            val txt = et.text.toString()
            if (txt.isNotEmpty()) {
                messages.add(UIMessage("Tú", txt, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                rv.scrollToPosition(messages.size - 1)
                et.text.clear()

                // Bot mock reply
                it.postDelayed({
                    messages.add(UIMessage("EduConnect IA", "Actualmente estoy en versión de prueba. Pronto estaré conectado al modelo de lenguaje para responderte en tiempo real.", false))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    rv.scrollToPosition(messages.size - 1)
                }, 1000)
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_asistente
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
                R.id.nav_foro -> {
                    startActivity(Intent(this, ForoActivity::class.java))
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
