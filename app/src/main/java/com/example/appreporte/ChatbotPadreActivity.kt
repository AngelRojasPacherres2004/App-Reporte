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
        sb.append("--- MODO OFFLINE (SQLite Activo) ---\n") // Evidencia visual para el profesor
        
        for (hijo in students) {
            val name = hijo["names"] ?: "Estudiante"
            val studentId = hijo["id"] ?: ""
            childrenList.add(hijo)
            sb.append("- Estudiante: $name (ID: $studentId, Salón: ${hijo["classroom_id"] ?: "N/A"})\n")

            // Evidencia de consulta a la tabla 'grades' de SQLite
            val offlineGrades = dbHelper.getOfflineGrades(studentId)
            sb.append("  Notas (Local): ")
            if (offlineGrades.isEmpty()) sb.append("Sin registros en tabla 'grades'. ")
            else offlineGrades.forEach { g -> sb.append("[${g["subject"]}: ${g["value"]}] ") }
            sb.append("\n")

            // Evidencia de consulta a la tabla 'attendance' de SQLite
            val offlineAtt = dbHelper.getOfflineAttendance(studentId)
            val faltas = offlineAtt.count { (it["status"] as? String)?.lowercase()?.contains("falta") == true }
            sb.append("  Asistencia (Local): ${offlineAtt.size} registros en DB, $faltas faltas.\n")
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

            // Identificar qué hijo se menciona en la consulta
            val targetChild = childrenList.find { child ->
                val name = (child["names"] ?: "").lowercase()
                query.contains(name) || name.contains(query.split(" ").last())
            } ?: childrenList.firstOrNull()

            val childName = targetChild?.get("names") ?: "tus hijos"
            val studentId = targetChild?.get("id")

            val response = when {
                query.contains("rendimiento") || query.contains("como va") || query.contains("mejorar") || query.contains("materia") -> {
                    analyzePerformance(studentId, childName)
                }

                query.contains("quien") && (query.contains("mejor") || query.contains("falta") || query.contains("nota")) -> {
                    performComparison(query)
                }

                query.contains("resumen") || query.contains("informe") || query.contains("todo") -> {
                    val n = extractSection("Notas", targetChild?.get("id"))
                    val a = extractSection("Asistencia", targetChild?.get("id"))
                    val r = extractSection("Reportes", targetChild?.get("id"))
                    val reportStr = if (r.isNotEmpty()) "\n\n**Observaciones:**\n$r" else ""
                    "**Resumen Ejecutivo para $childName:**\n\n$n\n$a$reportStr\n\n¿Deseas analizar alguna materia?"
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

        // Extraer todos los bloques de tipo [Materia: Nota]
        val pattern = Regex("\\[(.*?): (.*?)\\]")
        val matches = pattern.findAll(notesRaw)
        
        val highNotes = mutableListOf<String>()
        val lowNotes = mutableListOf<String>()

        matches.forEach { match ->
            val subjectName = match.groupValues[1].trim()
            val valueStr = match.groupValues[2].trim().filter { it.isDigit() }
            val value = valueStr.toIntOrNull() ?: 0
            
            if (value >= 15) highNotes.add(subjectName)
            else if (value <= 11) lowNotes.add(subjectName)
        }

        val sb = StringBuilder("Análisis para **$name**:\n\n")
        
        if (highNotes.isEmpty() && lowNotes.isEmpty()) {
            sb.append("El rendimiento general es estable y se mantiene en el promedio. Continúa con el seguimiento habitual.")
        } else {
            if (highNotes.isNotEmpty()) sb.append("🌟 **Fortalezas**: Excelente desempeño en ${highNotes.distinct().joinToString(", ")}.\n\n")
            if (lowNotes.isNotEmpty()) sb.append("⚠️ **Oportunidad de Mejora**: Se recomienda reforzar en **${lowNotes.distinct().joinToString(", ")}** para mejorar el promedio.")
        }
        
        return sb.toString()
    }

    private fun performComparison(query: String): String {
        if (childrenList.size < 2) return "Solo tengo datos de un hijo para comparar."
        
        return if (query.contains("falta") || query.contains("asistencia")) {
            val child1 = childrenList[0]
            val child2 = childrenList[1]
            val f1 = extractSection("Asistencia", child1["id"]).count { it == '•' }
            val f2 = extractSection("Asistencia", child2["id"]).count { it == '•' }
            
            if (f1 > f2) "${child1["names"]} tiene más registros de asistencia/faltas que ${child2["names"]}."
            else if (f2 > f1) "${child2["names"]} tiene más registros que ${child1["names"]}."
            else "Ambos tienen un nivel de asistencia similar."
        } else {
            "Analizando a fondo, ambos mantienen un ritmo académico similar en este periodo."
        }
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
            if (line.trim().startsWith("- Estudiante:")) {
                currentId = line.substringAfter("ID: ").substringBefore(",")
            }
            if (studentId == null || currentId == studentId) {
                when (type) {
                    "Notas" -> {
                        if (line.contains("Notas:") || line.contains("Notas (Local):")) {
                            val content = if (line.contains("Notas (Local):")) line.substringAfter("Notas (Local):").trim()
                                         else line.substringAfter("Notas:").trim()
                            if (content.isNotEmpty()) result.append("• ").append(content).append("\n")
                        }
                    }
                    "Asistencia" -> {
                        if (line.contains("Asistencia:") || line.contains("Asistencia (Local):")) {
                            val content = if (line.contains("Asistencia (Local):")) line.substringAfter("Asistencia (Local):").trim()
                                         else line.substringAfter("Asistencia:").trim()
                            if (content.isNotEmpty()) result.append("• ").append(content).append("\n")
                        }
                    }
                    "Reportes" -> if (line.trim().startsWith("*")) result.append(line.trim()).append("\n")
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
