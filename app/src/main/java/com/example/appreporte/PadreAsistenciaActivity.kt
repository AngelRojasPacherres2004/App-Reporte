package com.example.appreporte

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityPadreAsistenciaBinding
import com.google.firebase.firestore.FirebaseFirestore

class PadreAsistenciaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPadreAsistenciaBinding
    private lateinit var dbHelper: DatabaseHelper
    private val db = FirebaseFirestore.getInstance()
    private var studentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPadreAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
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
                val list = snapshot.documents.mapNotNull { doc -> 
                    val data = doc.data
                    if (data != null) {
                        // Respaldo en SQLite para uso offline
                        dbHelper.saveAttendance(
                            studentId,
                            data["date"]?.toString() ?: "",
                            data["status"]?.toString() ?: "",
                            data["course_name"]?.toString() ?: ""
                        )
                    }
                    data
                }.sortedByDescending { it["date"].toString() }
                    
                if (list.isEmpty()) {
                    Toast.makeText(this, "No hay registros de asistencia.", Toast.LENGTH_SHORT).show()
                }
                binding.rvPadreAsistencia.adapter = AttendanceRecordAdapter(list)
            }
            .addOnFailureListener {
                // Fallback a SQLite (Offline)
                val offlineAttendance = dbHelper.getOfflineAttendance(studentId)
                if (offlineAttendance.isNotEmpty()) {
                    binding.rvPadreAsistencia.adapter = AttendanceRecordAdapter(offlineAttendance)
                    Toast.makeText(this, "Mostrando historial local (Sin conexión)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error cargando asistencias y no hay datos locales", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
