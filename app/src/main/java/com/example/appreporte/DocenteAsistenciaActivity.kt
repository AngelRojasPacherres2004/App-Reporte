package com.example.appreporte

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityDocenteAsistenciaBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View

class DocenteAsistenciaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocenteAsistenciaBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AttendanceStudentAdapter
    private var schoolId: String = ""
    private val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private var selectedClassroom: String = ""
    private var selectedClassroomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocenteAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        schoolId = intent.getStringExtra("SCHOOL_ID") ?: ""
        binding.tvAttendanceDateInfo.text = "Fecha: $currentDateStr"
        
        binding.rvAlumnosAsistencia.layoutManager = LinearLayoutManager(this)
        
        loadClassrooms()
        
        binding.btnSaveAttendance.setOnClickListener {
            saveAttendance()
        }
    }

    private fun loadClassrooms() {
        if (schoolId.isEmpty()) return

        db.collection("classrooms")
            .whereEqualTo("school_id", schoolId)
            .get()
            .addOnSuccessListener { snapshot ->
                val classroomsList = snapshot.documents.map { doc ->
                    Pair(doc.id, doc.getString("name") ?: "Sin nombre")
                }.toMutableList()
                
                if (classroomsList.isEmpty()) {
                    classroomsList.add(Pair("general", "General"))
                }
                
                val names = classroomsList.map { it.second }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
                binding.spinnerClassrooms.adapter = spinnerAdapter
                
                binding.spinnerClassrooms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedPair = classroomsList[position]
                        selectedClassroom = selectedPair.second
                        selectedClassroomId = selectedPair.first
                        loadStudents(selectedClassroomId)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando salones", Toast.LENGTH_SHORT).show()
                loadStudents("general") // Fallback
            }
    }

    private fun loadStudents(classroomId: String) {
        if (schoolId.isEmpty()) return
        
        val query = if (classroomId == "general") {
            db.collection("students").whereEqualTo("school_id", schoolId)
        } else {
            db.collection("students").whereEqualTo("classroom_id", classroomId)
        }
        
        query.get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data?.toMutableMap()
                    data?.put("id", doc.id)
                    data
                }
                adapter = AttendanceStudentAdapter(list)
                binding.rvAlumnosAsistencia.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando alumnos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAttendance() {
        if (!::adapter.isInitialized) return
        
        val batch = db.batch()
        val results = adapter.attendanceResults
        
        for ((studentId, status) in results) {
            val docRef = db.collection("attendance").document()
            val data = hashMapOf(
                "student_id" to studentId,
                "date" to currentDateStr,
                "status" to status,
                "course_name" to selectedClassroom
            )
            batch.set(docRef, data)
        }
        
        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Asistencia guardada exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar asistencia", Toast.LENGTH_SHORT).show()
        }
    }
}
