package com.example.appreporte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityGradingBinding
import com.example.appreporte.databinding.ItemStudentGradeBinding
import java.text.SimpleDateFormat
import java.util.*

class GradingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGradingBinding
    private lateinit var dbHelper: DatabaseHelper
    private var selectedClassroomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setupSalonesSpinner(userEmail)
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
        val students = dbHelper.getStudentsByClassroom(classroomId)
        binding.rvStudentsGrades.layoutManager = LinearLayoutManager(this)
        binding.rvStudentsGrades.adapter = GradeAdapter(students) { studentId, grade ->
            saveGrade(studentId, grade)
        }
    }

    private fun saveGrade(studentId: Int, gradeStr: String) {
        val grade = gradeStr.toDoubleOrNull()
        if (grade == null) {
            Toast.makeText(this, "Ingrese una nota válida", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val success = dbHelper.addGrade(studentId, "General", grade, date, "Diario")
        
        if (success) {
            Toast.makeText(this, "Nota guardada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al guardar nota", Toast.LENGTH_SHORT).show()
        }
    }

    class GradeAdapter(
        private val students: List<Triple<Int, String, String>>,
        private val onSave: (Int, String) -> Unit
    ) : RecyclerView.Adapter<GradeAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemStudentGradeBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentGradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = students[position]
            holder.binding.tvStudentName.text = student.second
            holder.binding.btnSaveGrade.setOnClickListener {
                onSave(student.first, holder.binding.etGrade.text.toString())
            }
        }

        override fun getItemCount() = students.size
    }
}
