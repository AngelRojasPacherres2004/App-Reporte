package com.example.appreporte.codigo_vista_pantalla_padre

import com.example.appreporte.codigo_logica_padre.ChatAdapter
import com.example.appreporte.codigo_logica_padre.UIMessage
import com.example.appreporte.codigo_vista_pantalla_login.PerfilActivity


import com.example.appreporte.R
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private var studentId: String = ""
    private var currentState = ChatState.NORMAL
    private enum class ChatState { NORMAL, AWAITING_CONFIRMATION, AWAITING_COMPLAINT_TEXT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        
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
        setupBottomNavigation(userEmail)
    }

    private fun setupBottomNavigation(userEmail: String) {
        binding.bottomNavigation.selectedItemId = R.id.nav_asistente

        // Ajuste de icono de asistente
        val menuView = binding.bottomNavigation.getChildAt(0) as? ViewGroup
        val assistantItem = menuView?.findViewById<View>(R.id.nav_asistente)
        val iconView = assistantItem?.findViewById<android.widget.ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)
        iconView?.post {
            val params = iconView.layoutParams
            val density = resources.displayMetrics.density
            val sizeInPx = (40 * density).toInt()
            params.width = sizeInPx
            params.height = sizeInPx
            iconView.layoutParams = params
            iconView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = android.content.Intent(this, PadreDashboardActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", "usuario")
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    true
                }
                R.id.nav_foro -> {
                    if (studentId.isNotEmpty()) {
                        FirebaseFirestore.getInstance().collection("students").document(studentId).get()
                            .addOnSuccessListener { doc ->
                                val classroomId = doc.getString("classroom_id") ?: ""
                                FirebaseFirestore.getInstance().collection("classrooms").document(classroomId).get()
                                    .addOnSuccessListener { classDoc ->
                                        val classroomName = classDoc.getString("name") ?: "Foro"
                                        val intent = android.content.Intent(this, ForoDetalleActivity::class.java)
                                        intent.putExtra("CLASSROOM_ID", classroomId)
                                        intent.putExtra("SALON_NAME", classroomName)
                                        intent.putExtra("USER_EMAIL", userEmail)
                                        intent.putExtra("USER_ROL", "usuario")
                                        intent.putExtra("STUDENT_ID", studentId)
                                        startActivity(intent)
                                    }
                            }
                    } else {
                        Toast.makeText(this, "Por favor, seleccione un hijo en el Inicio primero", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_asistente -> true
                R.id.nav_reportes -> {
                    if (studentId.isNotEmpty()) {
                        val intent = android.content.Intent(this, PadreReporteActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        intent.putExtra("STUDENT_ID", studentId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Seleccione un hijo en el Inicio primero", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_perfil -> {
                    val intent = android.content.Intent(this, PerfilActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", "usuario")
                    startActivity(intent)
                    true
                }
                else -> false
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
        lifecycleScope.launch {
            val query = userInput.lowercase().trim()
            delay(600) // Simulación de pensamiento

            when (currentState) {
                ChatState.AWAITING_CONFIRMATION -> {
                    if (query.contains("si") || query.contains("sì") || query.contains("claro") || query.contains("acepto") || query.contains("vale")) {
                        currentState = ChatState.AWAITING_COMPLAINT_TEXT
                        addBotMessage("Entendido. Por favor, escribe a continuación el detalle de tu queja o reporte. Lo registraré en el sistema.")
                    } else {
                        currentState = ChatState.NORMAL
                        addBotMessage("Está bien. Si necesitas consultar notas o saber más sobre tus hijos, solo dímelo.")
                    }
                }
                ChatState.AWAITING_COMPLAINT_TEXT -> {
                    saveComplaintToFirestore(userInput)
                    currentState = ChatState.NORMAL
                }
                ChatState.NORMAL -> {
                    val response = when {
                        // Prioridad 1: Notas y Calificaciones
                        query.contains("nota") || query.contains("tota") || query.contains("califica") || 
                        query.contains("promedio") || query.contains("curso") || query.contains("materia") -> {
                            val data = extract("Notas")
                            if (data.isNotEmpty() && !data.contains("Sin notas", true)) {
                                "He revisado los registros de **notas** para tus hijos:\n\n$data"
                            } else {
                                "He buscado en el sistema y actualmente **no hay notas publicadas** todavía. Te recomiendo consultar con el docente en unos días."
                            }
                        }
                        
                        // Prioridad 2: Reportes y Asistencia/Conducta
                        query.contains("reporte") || query.contains("queja") || query.contains("conducta") || 
                        query.contains("comportamiento") || query.contains("incidencia") -> {
                            val data = extract("Reportes")
                            val baseMsg = if (data.isNotEmpty() && !data.contains("Sin reportes", true)) {
                                "He encontrado los siguientes **reportes** registrados:\n\n$data"
                            } else {
                                "¡Buenas noticias! **No hay reportes ni incidencias** registradas actualmente."
                            }
                            
                            currentState = ChatState.AWAITING_CONFIRMATION
                            "$baseMsg\n\n¿Deseas redactar y enviar una **nueva queja o reporte** formal a la institución? (Responde Si/No)"
                        }
                        
                        // Prioridad 3: Información sobre los hijos
                        query.contains("hijo") || query.contains("alumno") || query.contains("quien") || 
                        query.contains("nombre") || query.contains("llaman") -> {
                            val data = extract("Alumno")
                            if (data.isNotEmpty()) {
                                "Tienes registrado(s) a:\n$data\n\n¿Deseas saber sus notas o ver si tienen algún reporte?"
                            } else {
                                "No logro encontrar el nombre de tus hijos en mi base de datos actual. Por favor, contacta a soporte."
                            }
                        }
                        
                        // Saludos
                        query.contains("hola") || query.contains("buen") || query.contains("tal") -> {
                            "¡Hola! Soy tu asistente de EduConnect. Puedo darte información sobre **notas**, **reportes de conducta** o recordarte los datos de tus **hijos**. ¿En qué te ayudo?"
                        }
                        
                        // Despedidas
                        query.contains("gracias") || query.contains("adios") || query.contains("chau") -> {
                            "¡De nada! Estoy aquí para ayudarte. Que tengas un excelente día."
                        }

                        else -> "Entiendo que me preguntas por '$userInput', pero mi conocimiento actual se limita a **notas**, **reportes** y datos de tus **hijos**. ¿Te gustaría que revise alguno de esos temas?"
                    }
                    addBotMessage(response)
                }
            }
        }
    }

    private fun saveComplaintToFirestore(text: String) {
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        if (studentId.isEmpty()) {
            addBotMessage("Lo siento, no puedo enviar la queja porque no hay un estudiante seleccionado. Por favor, selecciona a tu hijo en la pantalla de inicio.")
            return
        }

        // Obtener el classroom_id del estudiante antes de guardar la queja
        firestore.collection("students").document(studentId).get()
            .addOnSuccessListener { studentDoc ->
                val classroomId = studentDoc.getString("classroom_id") ?: ""
                
                val complaint = hashMapOf(
                    "student_id" to studentId,
                    "classroom_id" to classroomId, // Crucial para que el docente lo vea
                    "parentEmail" to userEmail,
                    "content" to text,
                    "status" to "Pendiente",
                    "date" to SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                    "timestamp" to System.currentTimeMillis() // Para mejor ordenamiento
                )

                firestore.collection("complaints").add(complaint)
                    .addOnSuccessListener {
                        addBotMessage("Tu reporte ha sido enviado exitosamente. Se le dará seguimiento a la brevedad. ¿Algo más en lo que pueda ayudarte?")
                    }
                    .addOnFailureListener {
                        addBotMessage("Hubo un error al enviar el reporte. Por favor, intenta más tarde.")
                    }
            }
            .addOnFailureListener {
                addBotMessage("Error al validar la información del estudiante. Intenta de nuevo.")
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

