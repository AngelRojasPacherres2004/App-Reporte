package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AsistenteActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<UIMessage>()
    private var schoolId: String = ""
    private val studentsList = mutableListOf<Map<String, Any>>()
    private val gradesList = mutableListOf<Map<String, Any>>()
    private val classroomsList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistente)

        schoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"

        val rv = findViewById<RecyclerView>(R.id.rvBotMessages)
        chatAdapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = chatAdapter

        // Initial bot greeting
        addBotMessage("¡Hola! Soy tu asistente de EduConnect IA. Estoy listo para ayudarte a consultar información sobre los alumnos, ver qué calificaciones les faltan registrar, sus promedios o revisar salones.\n\nEscribe *'hola'* para ver ejemplos de preguntas.")

        loadContextData()

        findViewById<ImageButton>(R.id.btnSendBot).setOnClickListener {
            val et = findViewById<EditText>(R.id.etMessageBot)
            val txt = et.text.toString().trim()
            if (txt.isNotEmpty()) {
                addUserMessage(txt)
                et.text.clear()
                
                // Process the chatbot query dynamically
                it.postDelayed({
                    processBotQuery(txt)
                }, 800)
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_asistente
        bottomNav.setOnItemSelectedListener { item ->
            val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
            val userRol = intent.getStringExtra("USER_ROL") ?: "admin"
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val targetActivity = when (userRol.lowercase()) {
                        "superadmin" -> SuperAdminDashboardActivity::class.java
                        "admin" -> InicioActivity::class.java
                        "docente" -> DocenteDashboardActivity::class.java
                        else -> PadreDashboardActivity::class.java
                    }
                    val initIntent = Intent(this, targetActivity)
                    initIntent.putExtra("SCHOOL_ID", schoolId)
                    initIntent.putExtra("USER_EMAIL", userEmail)
                    initIntent.putExtra("USER_ROL", userRol)
                    startActivity(initIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_gestion -> {
                    if (userRol.lowercase() == "admin" || userRol.lowercase() == "superadmin") {
                        val gestionIntent = Intent(this, AdminDashboardActivity::class.java)
                        gestionIntent.putExtra("SCHOOL_ID", schoolId)
                        gestionIntent.putExtra("USER_EMAIL", userEmail)
                        gestionIntent.putExtra("USER_ROL", userRol)
                        startActivity(gestionIntent)
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_foro -> {
                    val foroIntent = Intent(this, ForoSalonesActivity::class.java)
                    foroIntent.putExtra("SCHOOL_ID", schoolId)
                    foroIntent.putExtra("USER_EMAIL", userEmail)
                    foroIntent.putExtra("USER_ROL", userRol)
                    startActivity(foroIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_asistente -> true
                R.id.nav_perfil -> {
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", userEmail)
                    perfilIntent.putExtra("USER_ROL", userRol)
                    perfilIntent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(perfilIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> true
            }
        }
    }

    private fun loadContextData() {
        val db = FirebaseFirestore.getInstance()
        if (schoolId.isEmpty()) return
        
        // Load classrooms
        db.collection("classrooms").whereEqualTo("school_id", schoolId).get()
            .addOnSuccessListener { classroomsSnap ->
                classroomsList.clear()
                classroomsSnap.documents.forEach { doc ->
                    classroomsList.add(mapOf("id" to doc.id, "name" to (doc.getString("name") ?: "")))
                }
                
                // Load students
                db.collection("students").whereEqualTo("school_id", schoolId).get()
                    .addOnSuccessListener { studentsSnap ->
                        studentsList.clear()
                        val studentIds = mutableListOf<String>()
                        studentsSnap.documents.forEach { doc ->
                            studentsList.add(mapOf(
                                "id" to doc.id,
                                "names" to (doc.getString("names") ?: ""),
                                "lastnames" to (doc.getString("lastnames") ?: ""),
                                "parent_email" to (doc.getString("parent_email") ?: ""),
                                "classroom_id" to (doc.getString("classroom_id") ?: "")
                            ))
                            studentIds.add(doc.id)
                        }
                        
                        // Load grades
                        if (studentIds.isNotEmpty()) {
                            db.collection("grades").get()
                                .addOnSuccessListener { gradesSnap ->
                                    gradesList.clear()
                                    gradesSnap.documents.forEach { doc ->
                                        val sId = doc.getString("student_id") ?: ""
                                        if (studentIds.contains(sId)) {
                                            gradesList.add(mapOf(
                                                "student_id" to sId,
                                                "subject" to (doc.getString("subject") ?: ""),
                                                "value" to (doc.getString("value") ?: "")
                                            ))
                                        }
                                    }
                                }
                        }
                    }
            }
    }

    private fun processBotQuery(userInput: String) {
        val query = userInput.lowercase().trim()
        
        val response = when {
            query.contains("hola") || query.contains("buen") -> {
                "¡Hola! Soy tu asistente de EduConnect IA. Puedo darte información detallada sobre los alumnos, sus notas, promedios, qué notas les faltan registrar, y más.\n\nPrueba preguntando:\n• *'notas de [Nombre del alumno]'*\n• *'qué le falta a [Nombre del alumno]'*\n• *'promedio de [Nombre]'*\n• *'lista de alumnos'*\n• *'salones'*"
            }
            
            query.contains("lista de alumnos") || query.contains("listar alumnos") || query.contains("mostrar alumnos") -> {
                if (studentsList.isEmpty()) {
                    "No hay alumnos registrados en este colegio."
                } else {
                    val sb = java.lang.StringBuilder("Aquí tienes la lista de alumnos en el colegio:\n")
                    studentsList.forEach { s ->
                        val classroom = classroomsList.find { it["id"] == s["classroom_id"] }?.get("name") ?: "Sin salón"
                        sb.append("• **${s["names"]} ${s["lastnames"]}** (Salón: $classroom)\n")
                    }
                    sb.toString()
                }
            }
            
            query.contains("salon") || query.contains("aula") -> {
                if (classroomsList.isEmpty()) {
                    "No hay salones registrados en este colegio."
                } else {
                    val sb = java.lang.StringBuilder("Lista de salones registrados:\n")
                    classroomsList.forEach { c ->
                        val count = studentsList.count { it["classroom_id"] == c["id"] }
                        sb.append("• **${c["name"]}** ($count alumnos)\n")
                    }
                    sb.toString()
                }
            }
            
            query.contains("nota") || query.contains("califica") || query.contains("promedio") || query.contains("falta") -> {
                val targetStudent = findStudentInQuery(query)
                if (targetStudent == null) {
                    "¿De qué alumno te gustaría consultar? Por favor escribe su nombre (ej: *'notas de Juanito'*)."
                } else {
                    val sId = targetStudent["id"] as String
                    val sName = "${targetStudent["names"]} ${targetStudent["lastnames"]}"
                    val sGrades = gradesList.filter { it["student_id"] == sId }
                    
                    when {
                        query.contains("falta") || query.contains("debe") -> {
                            val standardSubjects = listOf("Matemáticas", "Comunicación", "Ciencia y Tecnología", "Personal Social", "Inglés")
                            val registeredSubjects = sGrades.mapNotNull { it["subject"]?.toString()?.lowercase() }
                            val missing = standardSubjects.filter { !registeredSubjects.contains(it.lowercase()) }
                            
                            if (missing.isEmpty()) {
                                "¡Excelente! **$sName** tiene todas sus calificaciones completas registradas en el sistema."
                            } else {
                                "A **$sName** le falta registrar notas en las siguientes materias:\n" + missing.joinToString("\n") { "• $it" } + "\n\nActualmente tiene notas en: " + (sGrades.joinToString(", ") { "${it["subject"]}: ${it["value"]}" }.ifEmpty { "ningún curso" })
                            }
                        }
                        
                        query.contains("promedio") -> {
                            val values = sGrades.mapNotNull {
                                val v = it["value"]?.toString() ?: ""
                                v.toIntOrNull() ?: when (v.uppercase()) {
                                    "AD" -> 20
                                    "A" -> 17
                                    "B" -> 13
                                    "C" -> 9
                                    else -> null
                                }
                            }
                            if (values.isEmpty()) {
                                "**$sName** no tiene calificaciones numéricas válidas registradas todavía para calcular un promedio."
                            } else {
                                val avg = values.average()
                                val formattedAvg = String.format(Locale.US, "%.2f", avg)
                                "El promedio ponderado de **$sName** es **$formattedAvg**.\nDetalle:\n" + 
                                sGrades.joinToString("\n") { "• ${it["subject"]}: ${it["value"]}" }
                            }
                        }
                        
                        else -> {
                            if (sGrades.isEmpty()) {
                                "**$sName** no tiene calificaciones registradas en el sistema en este momento."
                            } else {
                                val sb = java.lang.StringBuilder("Aquí tienes las calificaciones de **$sName**:\n")
                                sGrades.forEach { g ->
                                    sb.append("• **${g["subject"]}**: ${g["value"]}\n")
                                }
                                sb.toString()
                            }
                        }
                    }
                }
            }
            
            else -> {
                val targetStudent = findStudentInQuery(query)
                if (targetStudent != null) {
                    val sId = targetStudent["id"] as String
                    val sName = "${targetStudent["names"]} ${targetStudent["lastnames"]}"
                    val sGrades = gradesList.filter { it["student_id"] == sId }
                    val classroom = classroomsList.find { it["id"] == targetStudent["classroom_id"] }?.get("name") ?: "Sin salón"
                    val gradesStr = sGrades.joinToString(", ") { "${it["subject"]}: ${it["value"]}" }.ifEmpty { "Sin notas aún" }
                    
                    "Información de **$sName**:\n• **Salón:** $classroom\n• **Notas:** $gradesStr\n\n¿Deseas saber el promedio o qué notas le faltan registrar?"
                } else {
                    "No entiendo la consulta. Puedes preguntarme por:\n• Notas de un alumno (ej: *'notas de Juanito'*)\n• Lo que le falta a un alumno (ej: *'que le falta a Juanito'*)\n• Promedio de un alumno (ej: *'promedio de Juanito'*)\n• Lista de alumnos del colegio\n• Lista de salones"
                }
            }
        }
        
        addBotMessage(response)
    }

    private fun findStudentInQuery(query: String): Map<String, Any>? {
        return studentsList.find { s ->
            val firstName = s["names"]?.toString()?.lowercase() ?: ""
            val lastName = s["lastnames"]?.toString()?.lowercase() ?: ""
            (firstName.isNotEmpty() && query.contains(firstName)) || (lastName.isNotEmpty() && query.contains(lastName))
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(UIMessage("Tú", text, true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        val rv = findViewById<RecyclerView>(R.id.rvBotMessages)
        rv.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(UIMessage("EduConnect IA", text, false))
        chatAdapter.notifyItemInserted(messages.size - 1)
        val rv = findViewById<RecyclerView>(R.id.rvBotMessages)
        rv.scrollToPosition(messages.size - 1)
    }
}
