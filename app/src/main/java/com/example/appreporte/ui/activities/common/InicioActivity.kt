package com.example.appreporte.ui.activities.common

import com.example.appreporte.R
import com.example.appreporte.ui.adapters.StaffAdapter
import com.example.appreporte.ui.activities.admin.AdminDashboardActivity
import com.example.appreporte.ui.activities.padre.ChatbotPadreActivity
import com.example.appreporte.utils.NavigationUtils
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityInicioBinding
import com.google.firebase.firestore.FirebaseFirestore

class InicioActivity : AppCompatActivity() {

    private var currentSchoolId: String = ""
    private lateinit var staffAdapter: StaffAdapter
    private lateinit var binding: ActivityInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSchoolId = intent.getStringExtra(NavigationUtils.SCHOOL_ID) ?: "Colegio San José"

        staffAdapter = StaffAdapter(emptyList())
        binding.rvFeaturedStaff.layoutManager = LinearLayoutManager(this)
        binding.rvFeaturedStaff.adapter = staffAdapter

        setupBackPress()

        binding.bottomNavigation.selectedItemId = R.id.nav_inicio
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL)
            val userRole = intent.getStringExtra(NavigationUtils.USER_ROL) ?: "admin"
            when (item.itemId) {
                R.id.nav_gestion -> {
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    intent.putExtra(NavigationUtils.SCHOOL_ID, currentSchoolId)
                    intent.putExtra(NavigationUtils.USER_EMAIL, userEmail)
                    intent.putExtra(NavigationUtils.USER_ROL, userRole)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_foro -> {
                    NavigationUtils.navigateToForo(this, userEmail, userRole, currentSchoolId)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    NavigationUtils.navigateToAsistente(this, userEmail, null)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    NavigationUtils.navigateToPerfil(this, userEmail, userRole, currentSchoolId)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> true
            }
        }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@InicioActivity)
                    .setTitle("Salir")
                    .setMessage("¿Estás seguro de que deseas salir de la aplicación?")
                    .setPositiveButton("Sí") { _, _ ->
                        finishAffinity() // Cierra todas las actividades y sale
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
        loadFeaturedStaff()
    }

    private fun loadStatistics() {
        val db = FirebaseFirestore.getInstance()
        
        // Count users
        db.collection("users").whereEqualTo("school_id", currentSchoolId).get()
            .addOnSuccessListener { snap ->
                binding.tvTotalUsers.text = snap.size().toString()
            }

        // Count classrooms
        db.collection("classrooms").whereEqualTo("school_id", currentSchoolId).get()
            .addOnSuccessListener { snap ->
                binding.tvTotalClassrooms.text = snap.size().toString()
            }
    }

    private fun loadFeaturedStaff() {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("school_id", currentSchoolId)
            .whereEqualTo("rol", "docente")
            .limit(3)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.data?.mapValues { it.value.toString() }
                }
                staffAdapter.updateStaff(list)
            }
    }
}
