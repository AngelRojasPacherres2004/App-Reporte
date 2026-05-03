package com.example.appreporte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityAttendanceBinding
import com.example.appreporte.databinding.ItemStudentAttendanceBinding
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var dbHelper: DatabaseHelper
    private var selectedClassroomId: Int = -1
    private var studentList: List<Triple<Int, String, String>> = emptyList()
    private val attendanceMap = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setupSalonesSpinner(userEmail)

        binding.btnSaveAllAttendance.setOnClickListener {
            saveAllAttendance()
        }
    }

    private fun setupSalonesSpinner(email: String) {
        val salones = dbHelper.getUserClassroomsWithNames(email)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, salones.map { it.second })
        binding.spinnerSalones.setAdapter(adapter)

        binding.spinnerSalones.setOnItemClickListener { _, _, position, _ ->
            selectedClassroomId = salones[position].first
            loadStudents(selectedClassroomId)
        }
    }

    private fun loadStudents(classroomId: Int) {
        studentList = dbHelper.getStudentsByClassroom(classroomId)
        attendanceMap.clear()
        // Default to present
        studentList.forEach { attendanceMap[it.first] = "present" }

        binding.rvStudentsAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvStudentsAttendance.adapter = AttendanceAdapter(studentList) { studentId, status ->
            attendanceMap[studentId] = status
        }
    }

    private fun saveAllAttendance() {
        if (selectedClassroomId == -1) {
            Toast.makeText(this, "Seleccione un salón primero", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var allSuccess = true

        attendanceMap.forEach { (studentId, status) ->
            if (!dbHelper.addAttendance(studentId, status, date)) {
                allSuccess = false
            }
        }

        if (allSuccess) {
            Toast.makeText(this, "Asistencia guardada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Hubo errores al guardar algunas asistencias", Toast.LENGTH_SHORT).show()
        }
    }

    class AttendanceAdapter(
        private val students: List<Triple<Int, String, String>>,
        private val onStatusChanged: (Int, String) -> Unit
    ) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemStudentAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = students[position]
            holder.binding.tvStudentName.text = student.second
            
            // Set initial state
            holder.binding.rbPresent.isChecked = true

            holder.binding.rgAttendance.setOnCheckedChangeListener { _, checkedId ->
                val status = when (checkedId) {
                    R.id.rbPresent -> "present"
                    R.id.rbLate -> "late"
                    R.id.rbAbsent -> "absent"
                    else -> "present"
                }
                onStatusChanged(student.first, status)
            }
        }

        override fun getItemCount() = students.size
    }
}
