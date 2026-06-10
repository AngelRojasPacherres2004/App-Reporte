package com.example.appreporte.ui.docente

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityDocenteHorarioBinding
import com.google.firebase.firestore.FirebaseFirestore

class DocenteHorarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocenteHorarioBinding
    private val db = FirebaseFirestore.getInstance()
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocenteHorarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        
        binding.rvDocenteHorario.layoutManager = LinearLayoutManager(this)
        
        loadSchedule()
    }

    private fun loadSchedule() {
        if (userEmail.isEmpty()) return
        
        db.collection("courses")
            .whereEqualTo("teacher_email", userEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc -> doc.data }
                if (list.isEmpty()) {
                    Toast.makeText(this, "No tienes cursos asignados aún.", Toast.LENGTH_SHORT).show()
                }
                binding.rvDocenteHorario.adapter = CourseScheduleAdapter(list, isPadre = false)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando horario", Toast.LENGTH_SHORT).show()
            }
    }
}

