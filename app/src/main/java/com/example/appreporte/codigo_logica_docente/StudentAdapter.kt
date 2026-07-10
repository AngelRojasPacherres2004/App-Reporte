package com.example.appreporte.codigo_logica_docente


import com.example.appreporte.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private var students: List<Map<String, String>>,
    private val onDeleteClick: (Int) -> Unit,
    private val onSendReportClick: (Int, String, String) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val tvParent: TextView = view.findViewById(R.id.tvParentEmail)
        val btnDelete: View = view.findViewById(R.id.btnDeleteStudent)
        val btnSendReport: View = view.findViewById(R.id.btnSendReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        val studentId = student["id"]?.toInt() ?: -1
        val studentName = "${student["names"]} ${student["lastnames"]}"
        val parentEmail = student["parent_email"] ?: ""
        
        holder.tvName.text = studentName
        holder.tvParent.text = "Padre: $parentEmail"
        
        holder.btnDelete.setOnClickListener {
            if (studentId != -1) onDeleteClick(studentId)
        }

        holder.btnSendReport.setOnClickListener {
            if (studentId != -1) onSendReportClick(studentId, studentName, parentEmail)
        }
    }

    override fun getItemCount() = students.size

    fun updateList(newList: List<Map<String, String>>) {
        students = newList
        notifyDataSetChanged()
    }
}

