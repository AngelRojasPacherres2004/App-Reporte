package com.example.appreporte.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceStudentAdapter(
    private val studentList: List<Map<String, Any>>
) : RecyclerView.Adapter<AttendanceStudentAdapter.ViewHolder>() {

    val attendanceResults = mutableMapOf<String, String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentNameAttendance)
        val rgStatus: RadioGroup = view.findViewById(R.id.rgAttendanceStatus)
        val rbPresent: RadioButton = view.findViewById(R.id.rbPresent)
        val rbLate: RadioButton = view.findViewById(R.id.rbLate)
        val rbAbsent: RadioButton = view.findViewById(R.id.rbAbsent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        val studentId = student["id"].toString()
        holder.tvName.text = student["name"]?.toString() ?: "Alumno"

        // Por defecto todos presentes
        if (!attendanceResults.containsKey(studentId)) {
            attendanceResults[studentId] = "Presente"
        }

        // Marcar el radio button correcto
        when (attendanceResults[studentId]) {
            "Presente" -> holder.rbPresent.isChecked = true
            "Tardanza" -> holder.rbLate.isChecked = true
            "Falta" -> holder.rbAbsent.isChecked = true
        }

        holder.rgStatus.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPresent -> attendanceResults[studentId] = "Presente"
                R.id.rbLate -> attendanceResults[studentId] = "Tardanza"
                R.id.rbAbsent -> attendanceResults[studentId] = "Falta"
            }
        }
    }

    override fun getItemCount() = studentList.size
}

