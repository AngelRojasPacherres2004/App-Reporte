package com.example.appreporte

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
// CAMBIO AQUÍ: Ahora usamos el binding que coincide con tu nuevo nombre de XML
import com.example.appreporte.databinding.ActivityDocenteComplaintsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class DocenteComplaintsActivity : AppCompatActivity() {

    // CAMBIO AQUÍ: Nombre de la clase de binding actualizado
    private lateinit var binding: ActivityDocenteComplaintsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ComplaintAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // CAMBIO AQUÍ: Inflamos con la clase correcta
        binding = ActivityDocenteComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupRecyclerView()
        setupThemeToggle()
        setupBottomNavigation()
    }

    private fun setupThemeToggle() {
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_sun)
        } else {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_moon)
        }

        binding.ivThemeToggle.setOnClickListener {
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun setupBottomNavigation() {
        // Mantenemos marcado el ícono de reportes (llave inglesa) del menú del docente
        binding.bottomNavigation.selectedItemId = R.id.nav_reportes

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val userEmail = intent.getStringExtra("USER_EMAIL")
            val userRole = "docente"

            when (item.itemId) {
                R.id.nav_inicio -> {
                    // Retorno limpio al panel del docente
                    val intent = Intent(this, DocenteDashboardActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", userRole)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_reportes -> true
                R.id.nav_foro -> {
                    val intent = Intent(this, ForoSalonesActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", userRole)
                    startActivity(intent)
                    true
                }
                R.id.nav_asistente -> true
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        val quejasIniciales = dbHelper.getAllQuejas()

        adapter = ComplaintAdapter(quejasIniciales) { queja ->
            mostrarDialogoAsignarEstado(queja)
        }

        binding.rvQuejas.layoutManager = LinearLayoutManager(this)
        binding.rvQuejas.adapter = adapter
    }

    private fun mostrarDialogoAsignarEstado(queja: Complaint) {
        val opciones = arrayOf("En Proceso", "Resuelto", "Rechazado")
        val estadoAnterior = queja.estado

        MaterialAlertDialogBuilder(this)
            .setTitle("Clasificar y Asignar")
            .setIcon(R.drawable.logo_original)
            .setItems(opciones) { _, which ->
                val estadoSeleccionado = opciones[which]
                val exito = dbHelper.updateQuejaStatus(queja.id, estadoSeleccionado)

                if (exito) {
                    adapter.updateData(dbHelper.getAllQuejas())

                    // Interactividad avanzada: Snackbar con opción de deshacer
                    Snackbar.make(binding.root, "Estado actualizado a: $estadoSeleccionado", Snackbar.LENGTH_LONG)
                        .setAction("DESHACER") {
                            dbHelper.updateQuejaStatus(queja.id, estadoAnterior)
                            adapter.updateData(dbHelper.getAllQuejas())
                        }
                        .setActionTextColor(Color.parseColor("#FFCA28"))
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}