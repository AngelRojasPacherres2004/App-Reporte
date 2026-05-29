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
            when (item.itemId) {
                R.id.nav_gestion -> {
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    intent.putExtra("SCHOOL_ID", currentSchoolId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
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
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", intent.getStringExtra("USER_EMAIL") ?: "")
                    perfilIntent.putExtra("USER_ROL", "admin")
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
