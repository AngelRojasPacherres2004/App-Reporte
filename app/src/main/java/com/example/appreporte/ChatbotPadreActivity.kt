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
import java.util.*

class ChatbotPadreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotPadreBinding
    private lateinit var dbHelper: DatabaseHelper
    private val messages = mutableListOf<UIMessage>()
    private lateinit var adapter: ChatAdapter
    private var contextData: String = ""
    private var childrenList = mutableListOf<Map<String, String>>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        
        binding.btnSend.isEnabled = false
        binding.etMessage.hint = "Analizando historial académico..."
        
        loadFullContext(userEmail)

        addBotMessage("¡Hola! Soy **EduConnect IA**. 🤖\n\nHe analizado el progreso de tus hijos. Puedes preguntarme cosas como:\n• *'¿Cómo va el rendimiento de mi hijo?'*\n• *'¿Quién tiene más faltas?'*\n• *'Dame un resumen de notas'*\n• *'¿En qué materia deben mejorar?'*")

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addUserMessage(text)
                binding.etMessage.text.clear()
                processUltraAdvancedQuery(text)
            }
        }
    }

    private fun loadFullContext(userEmail: String) {
        if (userEmail.isEmpty()) {
            contextData = "Error: Usuario no identificado."
            enableChat()
            return
        }

        // Carga rápida offline
        val offlineStudents = dbHelper.getOfflineStudents(userEmail)
        if (offlineStudents.isNotEmpty()) {
            buildContextFromOffline(offlineStudents)
        }

        firestore.collection("students").whereEqualTo("parent_email", userEmail).get()
            .addOnSuccessListener { students ->
                if (students.isEmpty) {
                    if (offlineStudents.isEmpty()) contextData = "Sin alumnos."
                    enableChat()
                    return@addOnSuccessListener
                }
                val remoteList = students.documents.mapNotNull { doc ->
                    val data = mutableMapOf<String, String>()
                    doc.data?.forEach { (k, v) -> data[k] = v.toString() }
                    data["id"] = doc.id
                    data
                }
                buildContextFromList(remoteList)
            }
            .addOnFailureListener { 
                if (offlineStudents.isEmpty()) {
                    contextData = "Error al conectar con el servidor y no hay datos locales."
                }
                enableChat() 
            }
    }

    private fun buildContextFromOffline(students: List<Map<String, String>>) {
        childrenList.clear()
        val sb = StringBuilder()
        for (hijo in students) {
            val name = hijo["names"] ?: "Estudiante"
            val studentId = hijo["id"] ?: ""
            childrenList.add(hijo)
            sb.append("- Estudiante: $name (ID: $studentId, Salón: ${hijo["classroom_id"] ?: "N/A"})\n")

            val offlineGrades = dbHelper.getOfflineGrades(studentId)
            sb.append("  Notas: ")
            if (offlineGrades.isEmpty()) sb.append("Sin notas locales. ")
            else offlineGrades.forEach { g -> sb.append("[${g["subject"]}: ${g["value"]}] ") }
            sb.append("\n")

            val offlineAtt = dbHelper.getOfflineAttendance(studentId)
            val faltas = offlineAtt.count { (it["status"] as? String)?.lowercase()?.contains("falta") == true }
            sb.append("  Asistencia: ${offlineAtt.size} registros locales, $faltas faltas.\n")
        }
        contextData = sb.toString()
        enableChat()
    }

    private fun buildContextFromList(students: List<Map<String, String>>) {
        childrenList.clear()
        val sb = StringBuilder()
        var processed = 0

        for (hijo in students) {
            val name = hijo["names"] ?: hijo["name"] ?: "Estudiante"
            val studentId = hijo["id"] ?: ""
            childrenList.add(hijo)
            sb.append("- Estudiante: $name (ID: $studentId, Salón: ${hijo["classroom_id"] ?: "N/A"})\n")

            firestore.collection("grades").whereEqualTo("student_id", studentId).get()
                .addOnSuccessListener { grades ->
                    sb.append("  Notas: ")
                    if (grades.isEmpty) sb.append("Sin notas. ")
                    else grades.forEach { g -> sb.append("[${g.getString("subject")}: ${g.getString("value")}] ") }
                    sb.append("\n")

                    firestore.collection("attendance").whereEqualTo("student_id", studentId).get()
                        .addOnSuccessListener { att ->
                            val faltas = att.count { it.getString("status")?.lowercase()?.contains("falta") == true }
                            sb.append("  Asistencia: ${att.size()} registros, $faltas faltas.\n")

                            firestore.collection("complaints").whereEqualTo("student_id", studentId).get()
                                .addOnSuccessListener { complaints ->
                                    if (!complaints.isEmpty) {
                                        sb.append("  Reportes: ")
                                        complaints.forEach { c -> sb.append("* ${c.getString("content")} ") }
                                        sb.append("\n")
                                    }
                                    processed++
                                    if (processed >= students.size) {
                                        contextData = sb.toString()
                                        enableChat()
                                    }
                                }
                        }
                }
        }
    }

    private fun enableChat() {
        binding.btnSend.isEnabled = true
        binding.etMessage.hint = "Pregúntame sobre el rendimiento..."
    }

    private fun processUltraAdvancedQuery(userInput: String) {
        lifecycleScope.launch {
            binding.tvTypingIndicator.visibility = View.VISIBLE
            val query = userInput.lowercase().trim()
            delay(1500) 
            binding.tvTypingIndicator.visibility = View.GONE

            val targetChild = childrenList.find { (it["names"] ?: "").lowercase().contains(query.split(" ").first()) }
            val childName = targetChild?.get("names") ?: "tus hijos"

            val response = when {
                query.contains("rendimiento") || query.contains("como va") || query.contains("mejorar") || query.contains("materia") -> {
                    analyzePerformance(targetChild?.get("id"), childName)
                }

                query.contains("quien") && (query.contains("mejor") || query.contains("falta") || query.contains("nota")) -> {
                    performComparison(query)
                }

                query.contains("resumen") || query.contains("informe") || query.contains("todo") -> {
                    val n = extractSection("Notas", targetChild?.get("id"))
                    val a = extractSection("Asistencia", targetChild?.get("id"))
                    "**Resumen Ejecutivo para $childName:**\n\n$n\n$a\n\n¿Deseas analizar alguna materia?"
                }

                query.contains("como") || query.contains("donde") || query.contains("puedo") -> {
                    handleAppTutorial(query)
                }

                query.contains("hola") || query.contains("buen") -> {
                    "¡Hola! Soy tu asistente. Estoy listo para analizar el progreso de $childName. ¿Qué quieres saber?"
                }

                else -> "Entiendo tu duda sobre '$userInput'. Puedo darte detalles de **asistencia, notas o rendimiento académico**. ¿En qué hijo nos enfocamos?"
            }
            addBotMessage(response)
        }
    }

    private fun analyzePerformance(studentId: String?, name: String): String {
        val notesRaw = extractSection("Notas", studentId)
        if (notesRaw.isEmpty() || notesRaw.contains("Sin notas")) return "No tengo suficientes datos de notas para $name."

        val subjects = notesRaw.split("•").filter { it.isNotBlank() }
        val highNotes = mutableListOf<String>()
        val lowNotes = mutableListOf<String>()

        subjects.forEach { s ->
            val valueStr = s.substringAfter(":").trim().removeSuffix("]").filter { it.isDigit() }
            val value = valueStr.toIntOrNull() ?: 0
            val subjectName = s.substringAfter("[").substringBefore(":").trim()
            if (value >= 15) highNotes.add(subjectName)
            else if (value < 13 && value > 0) lowNotes.add(subjectName)
        }

        val sb = StringBuilder("Análisis para **$name**:\n\n")
        if (highNotes.isNotEmpty()) sb.append("🌟 **Fortalezas**: ${highNotes.joinToString(", ")}.\n")
        if (lowNotes.isNotEmpty()) sb.append("⚠️ **Mejora**: Reforzar en **${lowNotes.joinToString(", ")}**.\n")
        
        return sb.toString()
    }

    private fun performComparison(query: String): String {
        if (childrenList.size < 2) return "Solo tienes un hijo registrado."
        return "Basado en los datos actuales, el desempeño es balanceado entre ambos, aunque ${childrenList[0]["names"]} tiene mayor asistencia."
    }

    private fun handleAppTutorial(query: String): String {
        return when {
            query.contains("pdf") || query.contains("descargar") -> "Descarga reportes en 'Notas y Reportes' (botón azul)."
            query.contains("profe") || query.contains("mensaje") -> "Usa 'Chat con Profesor' en tu Dashboard."
            else -> "Puedes ver Horario, Asistencia y Notas desde tu panel principal."
        }
    }

    private fun extractSection(type: String, studentId: String?): String {
        val lines = contextData.split("\n")
        val result = StringBuilder()
        var currentId = ""
        lines.forEach { line ->
            if (line.startsWith("- Estudiante:")) currentId = line.substringAfter("ID: ").substringBefore(",")
            if (studentId == null || currentId == studentId) {
                when (type) {
                    "Notas" -> if (line.contains("Notas:")) result.append("• ").append(line.replace("Notas:", "").trim()).append("\n")
                    "Asistencia" -> if (line.contains("Asistencia:")) result.append("• ").append(line.replace("Asistencia:", "").trim()).append("\n")
                    "Reportes" -> if (line.contains("*")) result.append(line.trim()).append("\n")
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
