package com.example.appreporte

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityChatbotPadreBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore

class ChatbotPadreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotPadreBinding
    private val messages = mutableListOf<UIMessage>()
    private lateinit var adapter: ChatAdapter
    private var contextData: String = "Cargando datos de los hijos..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        loadContextData(userEmail)

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
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            addBotMessage("Por favor, configura tu API Key de Gemini en local.properties.")
            return
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = apiKey
        )

        lifecycleScope.launch {
            try {
                val prompt = "Eres EduConnect IA, un asistente amable para una app escolar. El usuario dice: '$userInput'.\n\n$contextData\n\nResponde la consulta del usuario de forma amigable, clara y corta."
                val response = generativeModel.generateContent(prompt)
                val reply = response.text ?: "Lo siento, no pude generar una respuesta."
                addBotMessage(reply)
            } catch (e: Exception) {
                addBotMessage("Hubo un error de conexión con la IA.")
                e.printStackTrace()
            }
        }
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

    private fun loadContextData(userEmail: String) {
        if (userEmail.isEmpty()) {
            contextData = "No se pudo identificar al usuario."
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("students").whereEqualTo("parent_email", userEmail).get()
            .addOnSuccessListener { studentsSnap ->
                val sb = StringBuilder("Información secreta de contexto (No la muestres toda, solo usa lo que responda a la pregunta del usuario):\n")
                sb.append("Horario general de clases para mañana: Matemáticas (8:00 AM), Comunicación (10:00 AM) y Ciencias (12:00 PM).\n")
                
                if (studentsSnap.isEmpty) {
                    contextData = sb.append("El usuario no tiene hijos registrados en el sistema.").toString()
                    return@addOnSuccessListener
                }

                var pendingStudents = studentsSnap.size()
                for (studentDoc in studentsSnap) {
                    val studentName = "${studentDoc.getString("names")} ${studentDoc.getString("lastnames")}"
                    sb.append("\nHijo/a: $studentName. ")
                    
                    db.collection("grades").whereEqualTo("student_id", studentDoc.id).get()
                        .addOnSuccessListener { gradesSnap ->
                            sb.append("Notas: ")
                            if (gradesSnap.isEmpty) {
                                sb.append("Aún no tiene notas registradas. ")
                            } else {
                                val badSubjects = mutableListOf<String>()
                                for (gradeDoc in gradesSnap) {
                                    val subject = gradeDoc.getString("subject") ?: ""
                                    val value = gradeDoc.getString("value") ?: ""
                                    val type = gradeDoc.getString("type") ?: ""
                                    sb.append("[$subject - $type: $value] ")
                                    
                                    val numValue = value.toIntOrNull()
                                    if (numValue != null && numValue < 11) {
                                        badSubjects.add(subject)
                                    }
                                }
                                if (badSubjects.isNotEmpty()) {
                                    sb.append("Atención: $studentName está desaprobado(a) o mal en: ${badSubjects.joinToString(", ")}. Recomiéndale hablar con el profesor.")
                                } else {
                                    sb.append("¡Le va bien en todos los cursos!")
                                }
                            }
                            pendingStudents--
                            if (pendingStudents == 0) {
                                contextData = sb.toString()
                            }
                        }
                        .addOnFailureListener {
                            pendingStudents--
                            if (pendingStudents == 0) {
                                contextData = sb.toString()
                            }
                        }
                }
            }
            .addOnFailureListener {
                contextData = "No se pudo obtener la información de la base de datos."
            }
    }
}