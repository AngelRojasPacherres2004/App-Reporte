package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class InicioActivity : AppCompatActivity() {

    private var currentSchoolId: String = ""
    private lateinit var staffAdapter: StaffAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        currentSchoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"

        val rvFeaturedStaff = findViewById<RecyclerView>(R.id.rvFeaturedStaff)
        staffAdapter = StaffAdapter(emptyList())
        rvFeaturedStaff.layoutManager = LinearLayoutManager(this)
        rvFeaturedStaff.adapter = staffAdapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_inicio
        bottomNav.setOnItemSelectedListener { item ->
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
                else -> true
            }
        }
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
                findViewById<TextView>(R.id.tvTotalUsers).text = snap.size().toString()
            }

        // Count classrooms
        db.collection("classrooms").whereEqualTo("school_id", currentSchoolId).get()
            .addOnSuccessListener { snap ->
                findViewById<TextView>(R.id.tvTotalClassrooms).text = snap.size().toString()
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
