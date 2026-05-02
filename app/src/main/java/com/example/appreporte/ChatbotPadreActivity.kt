package com.example.appreporte

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityChatbotPadreBinding

class ChatbotPadreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotPadreBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        // Mensaje inicial del Bot
        addBotMessage("¡Hola! Soy tu asistente de EduConnect. ¿En qué puedo ayudarte hoy?")

        // --- BOTÓN PARA VOLVER AL HOME ---
        binding.btnBack.setOnClickListener {
            finish() // Cierra esta actividad y regresa al Dashboard del Padre
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addUserMessage(text)
                binding.etMessage.text.clear()

                // Simular que el bot está "pensando"
                Handler(Looper.getMainLooper()).postDelayed({
                    processResponse(text)
                }, 1000)
            }
        }
    }

    private fun processResponse(userInput: String) {
        val input = userInput.lowercase()
        val reply = when {
            input.contains("foro") -> "Para ver el foro, regresa al inicio y pulsa el botón 'VER FORO DEL SALÓN'."
            input.contains("notas") || input.contains("calificaciones") -> "Las notas se encuentran en la sección de 'Reportes'."
            input.contains("hola") -> "¡Hola! Puedes preguntarme sobre el foro, las notas o cómo usar la app."
            else -> "No estoy seguro de entenderte, pero puedes intentar preguntando por 'foro' o 'notas'."
        }
        addBotMessage(reply)
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(ChatMessage(text, false))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }
}