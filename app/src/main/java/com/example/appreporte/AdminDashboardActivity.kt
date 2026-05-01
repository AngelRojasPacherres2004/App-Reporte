package com.example.appreporte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityDashboardAdminBinding
import com.google.android.material.textfield.TextInputEditText

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userAdapter: UserAdapter
    private lateinit var classroomAdapter: ClassroomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        
        setupRecyclerViews()
        setupBottomNavigation()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        // Setup User Adapter
        userAdapter = UserAdapter(dbHelper.getAllUsers()) { userEmail ->
            showAssignClassroomDialog(userEmail)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter

        // Setup Classroom Adapter
        classroomAdapter = ClassroomAdapter(dbHelper.getAllClassrooms()) { classroomId ->
            if (dbHelper.deleteClassroom(classroomId)) {
                Toast.makeText(this, "Salón eliminado", Toast.LENGTH_SHORT).show()
                classroomAdapter.updateClassrooms(dbHelper.getAllClassrooms())
            }
        }
        binding.rvClassrooms.layoutManager = LinearLayoutManager(this)
        binding.rvClassrooms.adapter = classroomAdapter
    }

    private fun setupClickListeners() {
        binding.btnManageUsers.setOnClickListener {
            binding.llUserListContainer.visibility = View.VISIBLE
            binding.llClassroomListContainer.visibility = View.GONE
            binding.fabAddUser.visibility = View.VISIBLE
            binding.btnManageUsers.visibility = View.GONE
            binding.btnManageClassrooms.visibility = View.VISIBLE
        }

        binding.btnManageClassrooms.setOnClickListener {
            binding.llClassroomListContainer.visibility = View.VISIBLE
            binding.llUserListContainer.visibility = View.GONE
            binding.fabAddUser.visibility = View.GONE
            binding.btnManageUsers.visibility = View.VISIBLE
            binding.btnManageClassrooms.visibility = View.GONE
            
            showAddClassroomFab()
        }

        binding.fabAddUser.setOnClickListener {
            if (binding.llUserListContainer.visibility == View.VISIBLE) {
                showAddUserDialog()
            } else {
                showAddClassroomDialog()
            }
        }
    }

    private fun showAddClassroomFab() {
        binding.fabAddUser.visibility = View.VISIBLE
        // Podríamos cambiar el icono del FAB aquí si quisiéramos
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
                    if (dbHelper.addClassroom(name)) {
                        Toast.makeText(this, "Salón añadido", Toast.LENGTH_SHORT).show()
                        classroomAdapter.updateClassrooms(dbHelper.getAllClassrooms())
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAssignClassroomDialog(userEmail: String) {
        val classrooms = dbHelper.getAllClassrooms()
        val names = classrooms.map { it.second }.toTypedArray()
        val assignedIds = dbHelper.getUserClassrooms(userEmail)
        val checkedItems = BooleanArray(names.size) { index ->
            assignedIds.contains(classrooms[index].first)
        }

        AlertDialog.Builder(this)
            .setTitle("Asignar Salones a $userEmail")
            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                val classroomId = classrooms[which].first
                if (isChecked) {
                    dbHelper.assignUserToClassroom(userEmail, classroomId)
                } else {
                    dbHelper.removeUserFromClassroom(userEmail, classroomId)
                }
            }
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
        val spinnerRol = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerRol)

        val roles = arrayOf("admin", "docente", "usuario")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRol.setAdapter(adapter)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val email = etEmail.text.toString()
                val pass = etPassword.text.toString()
                val rol = spinnerRol.text.toString()

                if (email.isNotEmpty() && pass.isNotEmpty() && rol.isNotEmpty()) {
                    val success = dbHelper.addUser(email, pass, rol)
                    if (success) {
                        Toast.makeText(this, "Usuario añadido", Toast.LENGTH_SHORT).show()
                        userAdapter.updateUsers(dbHelper.getAllUsers())
                    } else {
                        Toast.makeText(this, "Error al añadir usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_reportes -> true
                R.id.nav_foro -> true
                R.id.nav_asistente -> true
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }
}
