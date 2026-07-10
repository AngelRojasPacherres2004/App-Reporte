package com.example.appreporte

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityChatbotPadreBinding
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatbotPadreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotPadreBinding
    private val messages = mutableListOf<UIMessage>()
    private lateinit var adapter: ChatAdapter
    private var contextData: String = ""
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        
        // Configuración inicial de UI
        binding.btnSend.isEnabled = false
        binding.etMessage.hint = "Cargando datos..."
        
        loadFullContext(userEmail)

        addBotMessage("¡Hola! Soy tu asistente de EduConnect. Estoy analizando la información de tus hijos para responder tus dudas.")

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addUserMessage(text)
                binding.etMessage.text.clear()
                processLocalQuery(text)
            }
        }
    }

    private fun loadFullContext(userEmail: String) {
        if (userEmail.isEmpty()) {
            contextData = "Error: Usuario no identificado."
            return
        }

        val sb = StringBuilder()
        firestore.collection("students").whereEqualTo("parent_email", userEmail).get()
            .addOnSuccessListener { students ->
                if (students.isEmpty) {
                    contextData = "Sin alumnos registrados."
                    enableChat()
                    return@addOnSuccessListener
                }

                var processed = 0
                for (doc in students) {
                    val name = "${doc.getString("names")} ${doc.getString("lastnames")}"
                    val studentId = doc.id
                    sb.append("- Alumno: $name\n")

                    // Cargar Notas
                    firestore.collection("grades").whereEqualTo("student_id", studentId).get()
                        .addOnSuccessListener { grades ->
                            sb.append("  Notas: ")
                            if (grades.isEmpty) sb.append("Sin notas. ")
                            else grades.forEach { g -> sb.append("[${g.getString("subject")}: ${g.getString("value")}] ") }
                            sb.append("\n")

                            // Cargar Reportes
                            firestore.collection("complaints").whereEqualTo("parentEmail", userEmail).get()
                                .addOnSuccessListener { complaints ->
                                    if (!complaints.isEmpty) {
                                        sb.append("  Reportes: ")
                                        complaints.forEach { c -> sb.append("* ${c.getString("content")} (${c.getString("status")}) ") }
                                        sb.append("\n")
                                    }
                                    
                                    processed++
                                    if (processed == students.size()) {
                                        contextData = sb.toString()
                                        enableChat()
                                    }
                                }
                        }
                }
            }
            .addOnFailureListener { enableChat() }
    }

    private fun enableChat() {
        binding.btnSend.isEnabled = true
        binding.etMessage.hint = "Pregúntame sobre notas o reportes..."
    }

    private fun processLocalQuery(userInput: String) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "\"\"" || apiKey == "TU_API_KEY_AQUI") {
            addBotMessage("⚠️ La API de Gemini no está configurada. Por favor, agrega GEMINI_API_KEY en tu archivo local.properties y sincroniza el proyecto para habilitar la Inteligencia Artificial.")
            return
        }

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val model = com.google.ai.client.generativeai.GenerativeModel(
                    modelName = "gemini-flash-latest",
                    apiKey = apiKey.replace("\"", "")
                )

                val prompt = "Eres un asistente escolar experto llamado EduConnect IA. Tus respuestas deben ser amables y muy concisas. Usa tablas de Markdown para mostrar las notas de los alumnos.\nContexto actual de tus hijos:\n$contextData\nPregunta del usuario: $userInput\nRespuesta:"
                val response = model.generateContent(prompt)
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    addBotMessage(response.text ?: "No pude generar una respuesta. Intenta de nuevo.")
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (e.message?.contains("404") == true || e.message?.contains("NOT_FOUND") == true) {
                        addBotMessage("⚠️ Error 404: No pude acceder al modelo. Esto significa que tu GEMINI_API_KEY no es válida, o que no tienes habilitada la API de Gemini en tu cuenta de Google Cloud (Google AI Studio). ¡Crea una clave gratis en aistudio.google.com!")
                    } else {
                        addBotMessage("Hubo un error de conexión con la IA. Error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun extract(type: String): String {
        val lines = contextData.split("\n")
        val result = StringBuilder()
        
        lines.forEach { line ->
            val trimmed = line.trim()
            when (type) {
                "Notas" -> {
                    if (trimmed.contains("[") && trimmed.contains("]")) {
                        result.append("• ").append(trimmed.replace("Notas:", "").trim()).append("\n")
                    }
                }
                "Reportes" -> {
                    if (trimmed.startsWith("*") || (trimmed.contains("Reporte") && !trimmed.contains("Sin reportes"))) {
                        result.append(trimmed).append("\n")
                    }
                }
                "Alumno" -> {
                    if (trimmed.startsWith("- Alumno:")) {
                        result.append("• ").append(trimmed.replace("- Alumno:", "").trim()).append("\n")
                    }
                }
            }
        }
        return result.toString().trim()
    }

    private fun addUserMessage(text: String) {
        messages.add(UIMessage("Tú", text, true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(UIMessage("EduConnect IA", text, false))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }
}
