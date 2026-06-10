package com.example.appreporte.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityDashboardAdminBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var classroomAdapter: ClassroomAdapter
    private var currentSchoolId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSchoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"

        setupRecyclerViews()
        setupBottomNavigation()
        setupClickListeners()
        setupThemeToggle()
        setupAdminProfile()
        setupBackPress()
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@AdminDashboardActivity)
                    .setTitle("Salir")
                    .setMessage("¿Estás seguro de que deseas salir de la aplicación?")
                    .setPositiveButton("Sí") { _, _ ->
                        finishAffinity()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        })
    }

    private fun setupAdminProfile() {
        binding.ivAdminProfile.setOnClickListener {
            val userEmail = intent.getStringExtra("USER_EMAIL")
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }
    }

    private fun setupThemeToggle() {
        // Revisar el estado actual del sistema
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Configurar el ícono inicial
        if (isNightMode) {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_sun)
        } else {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_moon)
        }

        // Configurar el clic para alternar
        binding.ivThemeToggle.setOnClickListener {
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun setupRecyclerViews() {
        // Setup User Adapter
        userAdapter = UserAdapter(emptyList(), { userEmail ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userEmail).get().addOnSuccessListener { userSnap ->
                val rol = userSnap.getString("rol") ?: ""
                val phone = userSnap.getString("phone") ?: ""
                if (rol.lowercase() == "docente") {
                    val options = arrayOf("Asignar Salones/Aulas", "Gestionar Cursos / Horario")
                    AlertDialog.Builder(this)
                        .setTitle("Opciones para Docente")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> showAssignClassroomDialog(userEmail)
                                1 -> showManageCoursesDialog(userEmail, phone)
                            }
                        }
                        .show()
                } else {
                    showAssignClassroomDialog(userEmail)
                }
            }
        }, { userMap: Map<String, String> ->
            showUserActionDialog(userMap)
        })
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter
        loadUsers()

        // Setup Classroom Adapter
        classroomAdapter = ClassroomAdapter(emptyList(), onDeleteClick = { classroomId ->
            val db = FirebaseFirestore.getInstance()
            db.collection("classrooms").document(classroomId).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Salón eliminado", Toast.LENGTH_SHORT).show()
                    loadClassrooms()
                }
        })
        binding.rvClassrooms.layoutManager = LinearLayoutManager(this)
        binding.rvClassrooms.adapter = classroomAdapter
        loadClassrooms()
    }

    private fun setupClickListeners() {
        binding.btnViewUsers.setOnClickListener {
            binding.llUserListContainer.visibility = View.VISIBLE
            binding.llClassroomListContainer.visibility = View.GONE
            // Scroll down a bit
            binding.rvUsers.parent.requestChildFocus(binding.rvUsers, binding.rvUsers)
        }

        binding.btnAddUserDirect.setOnClickListener {
            showAddUserDialog()
        }

        binding.btnViewClassrooms.setOnClickListener {
            binding.llClassroomListContainer.visibility = View.VISIBLE
            binding.llUserListContainer.visibility = View.GONE
            binding.rvClassrooms.parent.requestChildFocus(binding.rvClassrooms, binding.rvClassrooms)
        }

        binding.btnAddClassroomDirect.setOnClickListener {
            showAddClassroomDialog()
        }

        binding.btnViewStudents.setOnClickListener {
            val intent = Intent(this, GestionAlumnosSalonesActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddStudentDirect.setOnClickListener {
            val intent = Intent(this, GestionAlumnosSalonesActivity::class.java)
            intent.putExtra("OPEN_ADD_DIALOG", true)
            startActivity(intent)
        }

        // Nuevo: Contactar Súper Administrador
        binding.btnContactarSoporte?.setOnClickListener {
            val intent = Intent(this, DirectChatActivity::class.java)
            intent.putExtra("CURRENT_EMAIL", intent.getStringExtra("USER_EMAIL"))
            intent.putExtra("TARGET_EMAIL", "superadmin@reporte.com")
            startActivity(intent)
        }

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_gestion
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_gestion -> true
                R.id.nav_foro -> {
                    val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
                    val chatIntent = Intent(this, DirectChatActivity::class.java)
                    chatIntent.putExtra("CURRENT_EMAIL", userEmail)
                    chatIntent.putExtra("TARGET_EMAIL", "superadmin@reporte.com")
                    startActivity(chatIntent)
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
                    startActivity(Intent(this, PerfilActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddClassroomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_classroom, null)
        val spinnerNivel = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerNivel)
        val spinnerGrado = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerGrado)
        val spinnerSeccion = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerSeccion)

        spinnerNivel.isEnabled = false
        spinnerNivel.setText("Cargando...")

        FirebaseFirestore.getInstance().collection("colegios")
            .whereEqualTo("name", currentSchoolId)
            .get()
            .addOnSuccessListener { snapshot ->
                val levels = if (!snapshot.isEmpty) {
                    snapshot.documents[0].get("levels") as? List<String> ?: emptyList()
                } else {
                    emptyList()
                }

                val finalNiveles = if (levels.isNotEmpty()) levels.toTypedArray() else arrayOf("Inicial", "Primaria", "Secundaria")
                
                spinnerNivel.isEnabled = true
                spinnerNivel.setText("")
                spinnerNivel.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, finalNiveles))
            }
            .addOnFailureListener {
                spinnerNivel.isEnabled = true
                spinnerNivel.setText("")
                spinnerNivel.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Inicial", "Primaria", "Secundaria")))
            }

        // Update grados depending on Nivel selected
        spinnerNivel.setOnItemClickListener { _, _, _, _ ->
            val selectedNivel = spinnerNivel.text.toString()
            val grados = when (selectedNivel.lowercase()) {
                "inicial" -> arrayOf("3 Años", "4 Años", "5 Años") // Inicial
                "primaria" -> arrayOf("1er Grado", "2do Grado", "3er Grado", "4to Grado", "5to Grado", "6to Grado") // Primaria
                else -> arrayOf("1er Año", "2do Año", "3er Año", "4to Año", "5to Año") // Secundaria
            }
            spinnerGrado.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, grados))
            spinnerGrado.text.clear()
        }

        val secciones = arrayOf("A", "B", "C", "D", "E", "F", "Única", "Sin Sección")
        spinnerSeccion.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, secciones))

        AlertDialog.Builder(this)
            .setTitle("Nuevo Salón / Aula")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val nivel = spinnerNivel.text.toString()
                val grado = spinnerGrado.text.toString()
                val seccion = spinnerSeccion.text.toString()
                
                if (nivel.isNotEmpty() && grado.isNotEmpty()) {
                    val name = if (seccion.isNotEmpty() && seccion != "Sin Sección") {
                        "$grado $seccion - $nivel"
                    } else {
                        "$grado - $nivel"
                    }
                    val map = hashMapOf("name" to name, "school_id" to currentSchoolId)
                    FirebaseFirestore.getInstance().collection("classrooms").add(map)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Salón añadido: $name", Toast.LENGTH_SHORT).show()
                            loadClassrooms()
                        }
                } else {
                    Toast.makeText(this, "Completa nivel y grado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAssignClassroomDialog(userEmail: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("classrooms")
            .whereEqualTo("school_id", currentSchoolId)
            .get().addOnSuccessListener { classroomsSnap ->
            val classroomsList = classroomsSnap.documents.map { Pair(it.id, it.getString("name") ?: "") }
            val names = classroomsList.map { it.second }.toTypedArray()

            db.collection("users").document(userEmail).get().addOnSuccessListener { userSnap ->
                val assignedIds = userSnap.get("classrooms") as? List<String> ?: emptyList()
                val checkedItems = BooleanArray(names.size) { index ->
                    assignedIds.contains(classroomsList[index].first)
                }

                AlertDialog.Builder(this)
                    .setTitle("Asignar Salones a $userEmail")
                    .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                        val classroomId = classroomsList[which].first
                        if (isChecked) {
                            db.collection("users").document(userEmail).update("classrooms", FieldValue.arrayUnion(classroomId))
                        } else {
                            db.collection("users").document(userEmail).update("classrooms", FieldValue.arrayRemove(classroomId))
                        }
                    }
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
        }
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val spinnerRol = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerRol)

        val roles = arrayOf("admin", "docente", "usuario")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRol.setAdapter(adapter)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Usuario")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val email = etEmail.text.toString()
                val pass = etPassword.text.toString()
                val rol = spinnerRol.text.toString()
                val phone = etPhone.text.toString()

                if (email.isNotEmpty() && pass.isNotEmpty() && rol.isNotEmpty()) {
                    val userMap = hashMapOf(
                        "email" to email,
                        "password" to pass,
                        "rol" to rol,
                        "phone" to phone,
                        "school_id" to currentSchoolId
                    )
                    FirebaseFirestore.getInstance().collection("users").document(email).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario añadido", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al añadir usuario", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showUserActionDialog(user: Map<String, String>) {
        val options = arrayOf("Modificar", "Eliminar")
        val email = user["email"] ?: return
        
        AlertDialog.Builder(this)
            .setTitle("Opciones para $email")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditUserDialog(user)
                    1 -> confirmDeleteUser(email)
                }
            }
            .show()
    }

    private fun confirmDeleteUser(email: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar al usuario '$email'?")
            .setPositiveButton("Eliminar") { _, _ ->
                FirebaseFirestore.getInstance().collection("users").document(email).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditUserDialog(user: Map<String, String>) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val spinnerRol = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerRol)

        etEmail.setText(user["email"])
        etEmail.isEnabled = false // No permitimos editar el email que es el ID
        etPassword.setText(user["password"])
        etPhone.setText(user["phone"])
        
        val roles = arrayOf("admin", "docente", "usuario")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRol.setAdapter(adapter)
        spinnerRol.setText(user["rol"], false)

        AlertDialog.Builder(this)
            .setTitle("Editar Usuario")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val email = etEmail.text.toString()
                val pass = etPassword.text.toString()
                val rol = spinnerRol.text.toString()
                val phone = etPhone.text.toString()

                if (pass.isNotEmpty() && rol.isNotEmpty()) {
                    val updateMap = mapOf("password" to pass, "rol" to rol, "phone" to phone)
                    FirebaseFirestore.getInstance().collection("users").document(email).update(updateMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupBottomNavigation() {
        val userEmail = intent.getStringExtra("USER_EMAIL")
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_gestion -> true
                R.id.nav_foro -> true
                R.id.nav_asistente -> true
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUsers() {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("school_id", currentSchoolId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.mapValues { it.value.toString() }
                    }
                    userAdapter.updateUsers(list)
                }
            }
    }

    private fun loadClassrooms() {
        FirebaseFirestore.getInstance().collection("classrooms")
            .whereEqualTo("school_id", currentSchoolId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.map { Pair(it.id, it.getString("name") ?: "") }
                        .sortedBy { it.second.lowercase() }
                    classroomAdapter.updateClassrooms(list)
                }
            }
    }

    private fun showManageCoursesDialog(teacherEmail: String, teacherPhone: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("courses")
            .whereEqualTo("teacher_email", teacherEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                val coursesList = snapshot.documents.map { doc ->
                    val name = doc.getString("course_name") ?: "Curso sin nombre"
                    val schedule = doc.getString("schedule") ?: "Sin horario"
                    Triple(doc.id, name, schedule)
                }

                val options = coursesList.map { "${it.second} (${it.third})" }.toMutableList()
                options.add("+ Agregar Nuevo Curso")

                AlertDialog.Builder(this)
                    .setTitle("Cursos de $teacherEmail")
                    .setItems(options.toTypedArray()) { _, which ->
                        if (which == coursesList.size) {
                            showAddCourseDialog(teacherEmail, teacherPhone)
                        } else {
                            val selectedCourse = coursesList[which]
                            AlertDialog.Builder(this)
                                .setTitle("Eliminar Curso")
                                .setMessage("¿Estás seguro de que deseas quitar el curso '${selectedCourse.second}' de este profesor?")
                                .setPositiveButton("Eliminar") { _, _ ->
                                    db.collection("courses").document(selectedCourse.first).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Curso eliminado", Toast.LENGTH_SHORT).show()
                                            showManageCoursesDialog(teacherEmail, teacherPhone)
                                        }
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar cursos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddCourseDialog(teacherEmail: String, teacherPhone: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null)
        val etCourseName = dialogView.findViewById<TextInputEditText>(R.id.etCourseName)
        val etSchedule = dialogView.findViewById<TextInputEditText>(R.id.etSchedule)
        val etTeacherName = dialogView.findViewById<TextInputEditText>(R.id.etTeacherName)
        val spinnerClassroom = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerClassroom)

        val defaultName = teacherEmail.substringBefore("@")
        etTeacherName.setText(defaultName)

        val db = FirebaseFirestore.getInstance()
        var classroomsList = emptyList<Pair<String, String>>()

        db.collection("classrooms")
            .whereEqualTo("school_id", currentSchoolId)
            .get()
            .addOnSuccessListener { snapshot ->
                classroomsList = snapshot.documents.map { Pair(it.id, it.getString("name") ?: "") }
                val names = classroomsList.map { it.second }.toTypedArray()
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
                spinnerClassroom.setAdapter(adapter)
            }

        AlertDialog.Builder(this)
            .setTitle("Agregar Curso a $teacherEmail")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val courseName = etCourseName.text.toString().trim()
                val schedule = etSchedule.text.toString().trim()
                val teacherName = etTeacherName.text.toString().trim()
                val selectedClassroomName = spinnerClassroom.text.toString()

                if (courseName.isEmpty() || schedule.isEmpty() || teacherName.isEmpty() || selectedClassroomName.isEmpty()) {
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedClassroomPair = classroomsList.find { it.second == selectedClassroomName }
                val selectedClassroomId = selectedClassroomPair?.first ?: ""

                val courseMap = hashMapOf(
                    "course_name" to courseName,
                    "schedule" to schedule,
                    "teacher_name" to teacherName,
                    "teacher_email" to teacherEmail,
                    "teacher_phone" to teacherPhone,
                    "school_id" to currentSchoolId,
                    "classroom_id" to selectedClassroomId,
                    "classroom_name" to selectedClassroomName
                )

                db.collection("courses").add(courseMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Curso asignado exitosamente", Toast.LENGTH_SHORT).show()
                        showManageCoursesDialog(teacherEmail, teacherPhone)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al asignar curso", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar") { _, _ ->
                showManageCoursesDialog(teacherEmail, teacherPhone)
            }
            .show()
    }
}
