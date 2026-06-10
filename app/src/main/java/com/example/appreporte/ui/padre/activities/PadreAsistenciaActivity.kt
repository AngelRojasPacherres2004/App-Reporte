package com.example.appreporte.ui.padre

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityPadreAsistenciaBinding
import com.google.firebase.firestore.FirebaseFirestore

class PadreAsistenciaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPadreAsistenciaBinding
    private val db = FirebaseFirestore.getInstance()
    private var studentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPadreAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        
        binding.rvPadreAsistencia.layoutManager = LinearLayoutManager(this)
        
        loadAttendance()
    }

    private fun loadAttendance() {
        if (studentId.isEmpty()) return
        
        db.collection("attendance")
            .whereEqualTo("student_id", studentId)
            .get()
            .addOnSuccessListener { snapshot ->
                // Sort by date descending
                val list = snapshot.documents.mapNotNull { doc -> doc.data }
                    .sortedByDescending { it["date"].toString() }
                    
                if (list.isEmpty()) {
                    Toast.makeText(this, "No hay registros de asistencia.", Toast.LENGTH_SHORT).show()
                }
                binding.rvPadreAsistencia.adapter = AttendanceRecordAdapter(list)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando asistencias", Toast.LENGTH_SHORT).show()
            }
    }
}

