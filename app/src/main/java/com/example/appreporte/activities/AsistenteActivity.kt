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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "\"\"" || apiKey == "TU_API_KEY_AQUI") {
            addBotMessage("⚠️ La API de Gemini no está configurada. Por favor, agrega GEMINI_API_KEY en tu archivo local.properties y sincroniza el proyecto para habilitar la Inteligencia Artificial.")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Build dynamic context string from current data
                val sb = java.lang.StringBuilder()
                sb.append("Eres un asistente escolar experto llamado EduConnect IA. Responde de forma muy concisa y amable.\nContexto actual del colegio:\n")
                studentsList.forEach { s ->
                    val sName = "${s["names"]} ${s["lastnames"]}"
                    sb.append("- Alumno: $sName. ")
                    val sGrades = gradesList.filter { it["student_id"] == s["id"] }
                    if (sGrades.isNotEmpty()) {
                        sb.append("Notas: " + sGrades.joinToString(", ") { "${it["subject"]}: ${it["value"]}" } + ".\n")
                    } else {
                        sb.append("Sin notas.\n")
                    }
                }

                val model = com.google.ai.client.generativeai.GenerativeModel(
                    modelName = "gemini-flash-latest",
                    apiKey = apiKey.replace("\"", "")
                )

                val prompt = "$sb\nPregunta del usuario: $userInput\nTu respuesta concisa (usa tablas de Markdown si te piden mostrar notas):"
                val response = model.generateContent(prompt)
                
                withContext(Dispatchers.Main) {
                    addBotMessage(response.text ?: "No pude generar una respuesta.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (e.message?.contains("404") == true || e.message?.contains("NOT_FOUND") == true) {
                        addBotMessage("⚠️ Error 404: No pude acceder al modelo. Esto significa que tu GEMINI_API_KEY no es válida, o que no tienes habilitada la API de Gemini en tu cuenta de Google Cloud (Google AI Studio). ¡Crea una clave gratis en aistudio.google.com!")
                    } else {
                        addBotMessage("Hubo un error de conexión con la IA. Error: ${e.message}")
                    }
                }
            }
        }
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
