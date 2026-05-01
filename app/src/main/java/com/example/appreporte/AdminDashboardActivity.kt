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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        
        setupRecyclerView()
        setupBottomNavigation()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(dbHelper.getAllUsers())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter
    }

    private fun setupClickListeners() {
        binding.btnManageUsers.setOnClickListener {
            // Mostrar la lista y el FAB, ocultar el botón de gestión
            binding.llUserListContainer.visibility = View.VISIBLE
            binding.fabAddUser.visibility = View.VISIBLE
            binding.btnManageUsers.visibility = View.GONE
        }

        binding.fabAddUser.setOnClickListener {
            showAddUserDialog()
        }
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
