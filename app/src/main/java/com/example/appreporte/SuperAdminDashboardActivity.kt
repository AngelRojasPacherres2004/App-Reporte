package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SuperAdminDashboardActivity : AppCompatActivity() {

    private lateinit var rvSchools: RecyclerView
    private lateinit var fabAddSchool: ExtendedFloatingActionButton
    private lateinit var schoolAdapter: SchoolAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_super_admin_dashboard)

        rvSchools = findViewById(R.id.rvSchools)
        fabAddSchool = findViewById(R.id.fabAddSchool)

        setupRecyclerView()
        loadSchools()

        fabAddSchool.setOnClickListener {
            showAddSchoolDialog()
        }

        findViewById<View>(R.id.cardInbox).setOnClickListener {
            showInboxDialog()
        }
    }

    // ────────────────────────────────────────────────────────────
    // INBOX
    // ────────────────────────────────────────────────────────────
    private fun showInboxDialog() {
        val superAdminEmail = intent.getStringExtra("USER_EMAIL") ?: auth.currentUser?.email ?: "superadmin@reporte.com"
        firestore.collection("direct_chats")
            .whereArrayContains("participants", superAdminEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No tienes mensajes nuevos", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val uniqueSenders = mutableListOf<String>()
                for (doc in snapshot.documents) {
                    @Suppress("UNCHECKED_CAST")
                    val participants = doc.get("participants") as? List<String> ?: emptyList()
                    val other = participants.firstOrNull { it != superAdminEmail }
                    if (other != null && !uniqueSenders.contains(other)) uniqueSenders.add(other)
                }
                if (uniqueSenders.isEmpty()) {
                    Toast.makeText(this, "No tienes mensajes", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                AlertDialog.Builder(this)
                    .setTitle("Bandeja de Entrada (Admins)")
                    .setItems(uniqueSenders.toTypedArray()) { _, which ->
                        val intent = Intent(this, DirectChatActivity::class.java)
                        intent.putExtra("CURRENT_EMAIL", superAdminEmail)
                        intent.putExtra("TARGET_EMAIL", uniqueSenders[which])
                        startActivity(intent)
                    }
                    .show()
            }
            .addOnFailureListener { Toast.makeText(this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show() }
    }

    // ────────────────────────────────────────────────────────────
    // RECYCLER
    // ────────────────────────────────────────────────────────────
    private fun setupRecyclerView() {
        schoolAdapter = SchoolAdapter(emptyList()) { school -> showSchoolActionDialog(school) }
        rvSchools.layoutManager = LinearLayoutManager(this)
        rvSchools.adapter = schoolAdapter
    }

    private fun loadSchools() {
        firestore.collection("colegios").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    mapOf(
                        "id" to doc.id,
                        "name" to (doc.getString("name") ?: ""),
                        "adminEmail" to (doc.getString("adminEmail") ?: "Sin admin"),
                        "levels" to (doc.get("levels") as? List<*> ?: emptyList<Any>())
                    )
                }.sortedBy { it["name"]?.toString()?.lowercase() ?: "" }
                schoolAdapter.updateSchools(list)
            }
    }

    // ────────────────────────────────────────────────────────────
    // SCHOOL ACTIONS
    // ────────────────────────────────────────────────────────────
    private fun showSchoolActionDialog(school: Map<String, Any>) {
        val schoolDocId = school["id"]?.toString() ?: return
        val currentName = school["name"] as? String ?: ""
        val options = arrayOf("Ver detalles", "Modificar nombre", "Ver/crear Grados y Secciones", "Gestionar Admins", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle(currentName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSchoolDetailsDialog(schoolDocId, currentName)
                    1 -> showEditSchoolDialog(schoolDocId, currentName)
                    2 -> showGradosSectionesDialog(schoolDocId, currentName)
                    3 -> showManageAdminsDialog(schoolDocId, currentName)
                    4 -> confirmDeleteSchool(schoolDocId, currentName)
                }
            }
            .show()
    }

    // ────────────────────────────────────────────────────────────
    // SHOW DETAILS
    // ────────────────────────────────────────────────────────────
    private fun showSchoolDetailsDialog(schoolDocId: String, name: String) {
        firestore.collection("colegios").document(schoolDocId).get()
            .addOnSuccessListener { doc ->
                val admin = doc.getString("adminEmail") ?: "Sin asignar"
                val levels = (doc.get("levels") as? List<*>)?.joinToString(", ") ?: "No definidos"
                val msg = "Institución: $name\nAdministrador: $admin\nNiveles: $levels"
                AlertDialog.Builder(this)
                    .setTitle("Información del Colegio")
                    .setMessage(msg)
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
    }

    // ────────────────────────────────────────────────────────────
    // EDIT SCHOOL NAME
    // ────────────────────────────────────────────────────────────
    private fun showEditSchoolDialog(schoolDocId: String, currentName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_school, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etSchoolName)
        dialogView.findViewById<TextInputEditText>(R.id.etAdminEmail).visibility = View.GONE
        dialogView.findViewById<TextInputEditText>(R.id.etAdminPassword).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbInicial).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbPrimaria).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbSecundaria).visibility = View.GONE
        dialogView.findViewById<TextInputEditText>(R.id.etSecciones).visibility = View.GONE
        etName.setText(currentName)

        AlertDialog.Builder(this)
            .setTitle("Modificar nombre del Colegio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton
                firestore.collection("colegios").document(schoolDocId).update("name", name)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                        loadSchools()
                        // Update school_id in users
                        firestore.collection("users").whereEqualTo("school_id", currentName).get()
                            .addOnSuccessListener { snap ->
                                val batch = firestore.batch()
                                snap.documents.forEach { batch.update(it.reference, "school_id", name) }
                                batch.commit()
                            }
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ────────────────────────────────────────────────────────────
    // GRADOS & SECCIONES
    // ────────────────────────────────────────────────────────────
    private fun showGradosSectionesDialog(schoolDocId: String, schoolName: String) {
        // First load existing grades to show them
        firestore.collection("colegios").document(schoolDocId)
            .collection("grados").get()
            .addOnSuccessListener { gradesSnap ->
                val gradosList = gradesSnap.documents.map {
                    "${it.getString("nivel") ?: ""} - Grado ${it.getString("nombre") ?: ""}: ${it.getString("secciones") ?: ""}"
                }
                val msgExisting = if (gradosList.isEmpty()) "Sin grados aún" else gradosList.joinToString("\n")

                AlertDialog.Builder(this)
                    .setTitle("Grados en $schoolName")
                    .setMessage("Grados actuales:\n$msgExisting")
                    .setPositiveButton("Agregar Grado") { _, _ -> showAddGradoDialog(schoolDocId, schoolName) }
                    .setNeutralButton("Eliminar Grado") { _, _ -> showDeleteGradoDialog(schoolDocId) }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
    }

    private fun showAddGradoDialog(schoolDocId: String, schoolName: String) {
        val levels = arrayOf("Inicial", "Primaria", "Secundaria")
        var selectedLevel = levels[0]

        AlertDialog.Builder(this)
            .setTitle("Selecciona el nivel")
            .setSingleChoiceItems(levels, 0) { _, which -> selectedLevel = levels[which] }
            .setPositiveButton("Siguiente") { _, _ ->
                showAddGradoNameDialog(schoolDocId, schoolName, selectedLevel)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddGradoNameDialog(schoolDocId: String, schoolName: String, nivel: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_school, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etSchoolName)
        etName.hint = "Nombre del grado (ej: 1°, 2°, etc.)"
        val etSecciones = dialogView.findViewById<TextInputEditText>(R.id.etSecciones)
        etSecciones.hint = "Secciones (ej: A,B,C)"

        // Hide unneeded fields
        dialogView.findViewById<TextInputEditText>(R.id.etAdminEmail).visibility = View.GONE
        dialogView.findViewById<TextInputEditText>(R.id.etAdminPassword).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbInicial).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbPrimaria).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbSecundaria).visibility = View.GONE

        AlertDialog.Builder(this)
            .setTitle("Agregar Grado - $nivel")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val gradoName = etName.text.toString().trim()
                val secciones = etSecciones.text.toString().trim()
                if (gradoName.isEmpty()) {
                    Toast.makeText(this, "Ingresa el grado", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val gradoMap = hashMapOf(
                    "nombre" to gradoName,
                    "nivel" to nivel,
                    "secciones" to secciones,
                    "schoolId" to schoolDocId,
                    "schoolName" to schoolName
                )

                firestore.collection("colegios").document(schoolDocId)
                    .collection("grados").add(gradoMap)
                    .addOnSuccessListener { gradoRef ->
                        Toast.makeText(this, "Grado $gradoName agregado", Toast.LENGTH_SHORT).show()
                        val seccionesList = secciones.split(",")
                            .map { it.trim() }.filter { it.isNotEmpty() }
                        // Add classroom(s)
                        if (seccionesList.isEmpty()) {
                            // No sections: single classroom
                            val classroomMap = hashMapOf(
                                "name" to "$nivel - $gradoName°",
                                "grade" to gradoName,
                                "section" to "",
                                "level" to nivel,
                                "school_id" to schoolName,
                                "schoolDocId" to schoolDocId,
                                "gradoId" to gradoRef.id
                            )
                            firestore.collection("classrooms").add(classroomMap)
                        } else {
                            seccionesList.forEach { sec ->
                                val classroomMap = hashMapOf(
                                    "name" to "$nivel - $gradoName° $sec",
                                    "grade" to gradoName,
                                    "section" to sec,
                                    "level" to nivel,
                                    "school_id" to schoolName,
                                    "schoolDocId" to schoolDocId,
                                    "gradoId" to gradoRef.id
                                )
                                firestore.collection("classrooms").add(classroomMap)
                            }
                        }
                        // Update school levels list
                        firestore.collection("colegios").document(schoolDocId)
                            .collection("grados").get()
                            .addOnSuccessListener { allGrades ->
                                val allLevels = allGrades.documents
                                    .mapNotNull { it.getString("nivel") }.distinct()
                                firestore.collection("colegios").document(schoolDocId)
                                    .update("levels", allLevels)
                            }
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteGradoDialog(schoolDocId: String) {
        firestore.collection("colegios").document(schoolDocId)
            .collection("grados").get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    Toast.makeText(this, "No hay grados para eliminar", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val options = snap.documents.map {
                    "${it.getString("nivel")} - Grado ${it.getString("nombre")}"
                }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Grado")
                    .setItems(options) { _, which ->
                        val docId = snap.documents[which].id
                        firestore.collection("colegios").document(schoolDocId)
                            .collection("grados").document(docId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Grado eliminado", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .show()
            }
    }

    // ────────────────────────────────────────────────────────────
    // MANAGE ADMINS
    // ────────────────────────────────────────────────────────────
    private fun showManageAdminsDialog(schoolDocId: String, schoolName: String) {
        val options = arrayOf("Ver admins del colegio", "Crear nuevo administrador")
        AlertDialog.Builder(this)
            .setTitle("Gestión de Admins - $schoolName")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUsersListDialog(schoolName)
                    1 -> showAddAdminDialog(schoolDocId, schoolName)
                }
            }
            .show()
    }

    private fun showAddAdminDialog(schoolDocId: String, schoolName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_school, null)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etAdminEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etAdminPassword)

        // Hide school fields
        dialogView.findViewById<TextInputEditText>(R.id.etSchoolName).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbInicial).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbPrimaria).visibility = View.GONE
        dialogView.findViewById<CheckBox>(R.id.cbSecundaria).visibility = View.GONE
        dialogView.findViewById<TextInputEditText>(R.id.etSecciones).visibility = View.GONE
        etPassword.setText("admin123")

        AlertDialog.Builder(this)
            .setTitle("Crear Admin para $schoolName")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, "Ingresa el correo", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (password.length < 6) {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                createAdminInFirebase(email, password, schoolDocId, schoolName)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createAdminInFirebase(email: String, password: String, schoolDocId: String, schoolName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                saveAdminToFirestore(email, password, schoolDocId, schoolName)
            }
            .addOnFailureListener { ex ->
                // If user already exists in Auth, just update/create in Firestore
                if (ex.message?.contains("already in use") == true) {
                    saveAdminToFirestore(email, password, schoolDocId, schoolName)
                } else {
                    Toast.makeText(this, "Error: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveAdminToFirestore(email: String, password: String, schoolDocId: String, schoolName: String) {
        val userMap = hashMapOf(
            "email" to email,
            "password" to password,
            "rol" to "admin",
            "phone" to "",
            "school_id" to schoolName,
            "schoolDocId" to schoolDocId
        )
        firestore.collection("users").document(email).set(userMap)
            .addOnSuccessListener {
                // Also update adminEmail in school doc
                firestore.collection("colegios").document(schoolDocId)
                    .update("adminEmail", email)
                Toast.makeText(this, "Admin $email creado para $schoolName\nContraseña: $password", Toast.LENGTH_LONG).show()
                loadSchools()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar en base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUsersListDialog(schoolName: String) {
        firestore.collection("users").whereEqualTo("school_id", schoolName).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No hay usuarios para $schoolName", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val users = snapshot.documents.map {
                    mapOf(
                        "email" to (it.getString("email") ?: it.id),
                        "rol" to (it.getString("rol") ?: "usuario")
                    )
                }
                val labels = users.map { "${it["email"]} (${it["rol"]})" }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Usuarios de $schoolName")
                    .setItems(labels) { _, which ->
                        val u = users[which]
                        val email = u["email"] ?: return@setItems
                        AlertDialog.Builder(this)
                            .setTitle("Acción para $email")
                            .setItems(arrayOf("Restablecer contraseña", "Eliminar usuario")) { _, opt ->
                                when (opt) {
                                    0 -> firestore.collection("users").document(email)
                                        .update("password", "admin123")
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Contraseña restablecida a admin123", Toast.LENGTH_SHORT).show()
                                        }
                                    1 -> firestore.collection("users").document(email).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .show()
                    }
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
    }

    // ────────────────────────────────────────────────────────────
    // ADD SCHOOL (full flow)
    // ────────────────────────────────────────────────────────────
    private fun showAddSchoolDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_school, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etSchoolName)
        val etAdminEmail = dialogView.findViewById<TextInputEditText>(R.id.etAdminEmail)
        val etAdminPassword = dialogView.findViewById<TextInputEditText>(R.id.etAdminPassword)
        val cbInicial = dialogView.findViewById<CheckBox>(R.id.cbInicial)
        val cbPrimaria = dialogView.findViewById<CheckBox>(R.id.cbPrimaria)
        val cbSecundaria = dialogView.findViewById<CheckBox>(R.id.cbSecundaria)

        etAdminPassword.setText("admin123")

        AlertDialog.Builder(this)
            .setTitle("Registrar Nuevo Colegio")
            .setView(dialogView)
            .setPositiveButton("Registrar") { _, _ ->
                val name = etName.text.toString().trim()
                val adminEmail = etAdminEmail.text.toString().trim()
                val adminPassword = etAdminPassword.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(this, "Ingresa el nombre del colegio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val levels = mutableListOf<String>()

                if (cbInicial.isChecked) {
                    levels.add("Inicial")
                }
                if (cbPrimaria.isChecked) {
                    levels.add("Primaria")
                }
                if (cbSecundaria.isChecked) {
                    levels.add("Secundaria")
                }

                val schoolMap = hashMapOf(
                    "name" to name,
                    "levels" to levels,
                    "adminEmail" to adminEmail
                )

                firestore.collection("colegios").add(schoolMap)
                    .addOnSuccessListener { schoolRef ->
                        Toast.makeText(this, "Colegio '$name' registrado", Toast.LENGTH_SHORT).show()
                        loadSchools()

                        // Create admin if email provided
                        if (adminEmail.isNotEmpty()) {
                            val pwd = if (adminPassword.length >= 6) adminPassword else "admin123"
                            createAdminInFirebase(adminEmail, pwd, schoolRef.id, name)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al registrar colegio", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ────────────────────────────────────────────────────────────
    // DELETE SCHOOL
    // ────────────────────────────────────────────────────────────
    private fun confirmDeleteSchool(schoolDocId: String, name: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Colegio")
            .setMessage("¿Seguro que deseas eliminar '$name'? Se eliminarán todos sus datos.")
            .setPositiveButton("Eliminar") { _, _ ->
                firestore.collection("colegios").document(schoolDocId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Colegio eliminado", Toast.LENGTH_SHORT).show()
                        loadSchools()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
