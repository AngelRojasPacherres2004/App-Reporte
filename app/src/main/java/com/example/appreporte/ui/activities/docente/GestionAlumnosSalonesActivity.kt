package com.example.appreporte.ui.activities.docente

import com.example.appreporte.R
import com.example.appreporte.ui.adapters.ClassroomAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityGestionAlumnosSalonesBinding

import com.example.appreporte.utils.NavigationUtils
import com.google.firebase.firestore.FirebaseFirestore

class GestionAlumnosSalonesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionAlumnosSalonesBinding
    private var currentSchoolId: String = ""
    private lateinit var classroomAdapter: ClassroomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAlumnosSalonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSchoolId = intent.getStringExtra(NavigationUtils.SCHOOL_ID) ?: "Colegio San José"
        
        setupRecyclerView()
        loadClassrooms()
    }

    private fun setupRecyclerView() {
        classroomAdapter = ClassroomAdapter(
            emptyList(),
            onDeleteClick = {},
            onItemClick = { id, name ->
                val userEmail = intent.getStringExtra(NavigationUtils.USER_EMAIL)
                val userRole = intent.getStringExtra(NavigationUtils.USER_ROL)
                NavigationUtils.navigateToAlumnosLista(this, id, name, userEmail, userRole, currentSchoolId)
            }
        )
        binding.rvSalonesAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvSalonesAlumnos.adapter = classroomAdapter
    }

    private fun loadClassrooms() {
        FirebaseFirestore.getInstance().collection("classrooms")
            .whereEqualTo("school_id", currentSchoolId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    Pair(doc.id, name)
                }
                classroomAdapter.updateClassrooms(list)
            }
    }
}
