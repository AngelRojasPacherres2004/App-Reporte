package com.example.appreporte

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
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userAdapter: UserAdapter
    private lateinit var classroomAdapter: ClassroomAdapter
    private var currentSchoolId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSchoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"
        dbHelper = DatabaseHelper(this)

        setupRecyclerViews()
        setupBottomNavigation()
        setupClickListeners()
        setupThemeToggle()
        setupAdminProfile()
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
            showAssignClassroomDialog(userEmail)
        }, { userMap: Map<String, String> ->
            showEditUserDialog(userMap)
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
                    startActivity(Intent(this, ForoActivity::class.java))
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
        val input = TextInputEditText(this)
        input.hint = "Nombre del salón"
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(48, 16, 48, 16)
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Salón")
            .setView(container)
            .setPositiveButton("Añadir") { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) {
                    val map = hashMapOf("name" to name, "school_id" to currentSchoolId)
                    FirebaseFirestore.getInstance().collection("classrooms").add(map)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Salón añadido", Toast.LENGTH_SHORT).show()
                            loadClassrooms()
                        }
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
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.data?.mapValues { it.value.toString() }
                }
                userAdapter.updateUsers(list)
            }
    }

    private fun loadClassrooms() {
        FirebaseFirestore.getInstance().collection("classrooms")
            .whereEqualTo("school_id", currentSchoolId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { Pair(it.id, it.getString("name") ?: "") }
                classroomAdapter.updateClassrooms(list)
            }
    }
}