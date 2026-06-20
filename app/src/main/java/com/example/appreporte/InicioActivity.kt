package com.example.appreporte

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

        currentSchoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"

        staffAdapter = StaffAdapter(emptyList())
        binding.rvFeaturedStaff.layoutManager = LinearLayoutManager(this)
        binding.rvFeaturedStaff.adapter = staffAdapter

        setupBackPress()

        binding.bottomNavigation.selectedItemId = R.id.nav_inicio
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
            val userRol = intent.getStringExtra("USER_ROL") ?: "admin"
            when (item.itemId) {
                R.id.nav_gestion -> {
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    intent.putExtra("SCHOOL_ID", currentSchoolId)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", userRol)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_foro -> {
                    val foroIntent = Intent(this, ForoSalonesActivity::class.java)
                    foroIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    foroIntent.putExtra("USER_EMAIL", userEmail)
                    foroIntent.putExtra("USER_ROL", userRol)
                    startActivity(foroIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    val asistenteIntent = Intent(this, AsistenteActivity::class.java)
                    asistenteIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    asistenteIntent.putExtra("USER_EMAIL", userEmail)
                    asistenteIntent.putExtra("USER_ROL", userRol)
                    startActivity(asistenteIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", userEmail)
                    perfilIntent.putExtra("USER_ROL", userRol)
                    perfilIntent.putExtra("SCHOOL_ID", currentSchoolId)
                    startActivity(perfilIntent)
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
        db.collection("users").whereEqualTo("school_id", currentSchoolId)
            .addSnapshotListener { snap, error ->
                if (error != null) return@addSnapshotListener
                if (snap != null) {
                    binding.tvTotalUsers.text = snap.size().toString()
                }
            }

        // Count classrooms
        db.collection("classrooms").whereEqualTo("school_id", currentSchoolId)
            .addSnapshotListener { snap, error ->
                if (error != null) return@addSnapshotListener
                if (snap != null) {
                    binding.tvTotalClassrooms.text = snap.size().toString()
                }
            }
    }

    private fun loadFeaturedStaff() {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("school_id", currentSchoolId)
            .whereEqualTo("rol", "docente")
            .limit(3)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.mapValues { it.value.toString() }
                    }
                    staffAdapter.updateStaff(list)
                }
            }
    }
}
