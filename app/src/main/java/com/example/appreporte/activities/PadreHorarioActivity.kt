package com.example.appreporte

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityPadreHorarioBinding
import com.google.firebase.firestore.FirebaseFirestore

class PadreHorarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPadreHorarioBinding
    private val db = FirebaseFirestore.getInstance()
    private var classroomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPadreHorarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classroomId = intent.getStringExtra("CLASSROOM_ID") ?: ""
        
        binding.rvPadreHorario.layoutManager = LinearLayoutManager(this)
        
        loadSchedule()
    }

    private fun loadSchedule() {
        if (classroomId.isEmpty()) return
        
        db.collection("courses")
            .whereEqualTo("classroom_id", classroomId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc -> doc.data }
                if (list.isEmpty()) {
                    Toast.makeText(this, "No hay cursos asignados para este salón.", Toast.LENGTH_SHORT).show()
                }
                binding.rvPadreHorario.adapter = CourseScheduleAdapter(list, isPadre = true)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando horario", Toast.LENGTH_SHORT).show()
            }
    }
}
