package com.example.appreporte.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityGestionAlumnosSalonesBinding

import com.google.firebase.firestore.FirebaseFirestore

class GestionReportesSalonesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionAlumnosSalonesBinding
    private var currentSchoolId: String = ""
    private lateinit var classroomAdapter: ClassroomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAlumnosSalonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSchoolId = intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"
        
        // Reutilizamos el layout de selección de salones
        
        setupRecyclerView()
        loadClassrooms()
    }

    private fun setupRecyclerView() {
        classroomAdapter = ClassroomAdapter(
            emptyList(),
            onDeleteClick = {},
            onItemClick = { id, name ->
                val intent = Intent(this, AlumnosReporteListaActivity::class.java)
                intent.putExtra("CLASSROOM_ID", id) // now string
                intent.putExtra("CLASSROOM_NAME", name)
                startActivity(intent)
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

