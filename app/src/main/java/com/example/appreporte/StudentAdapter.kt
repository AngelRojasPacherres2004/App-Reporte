package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private var students: List<Triple<Int, String, String>>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val tvParent: TextView = view.findViewById(R.id.tvParentEmail)
        val btnDelete: View = view.findViewById(R.id.btnDeleteStudent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.tvName.text = student.second
        holder.tvParent.text = "Padre: ${student.third}"
        holder.btnDelete.setOnClickListener {
            onDeleteClick(student.first)
        }
    }

    override fun getItemCount() = students.size

    fun updateList(newList: List<Triple<Int, String, String>>) {
        students = newList
        notifyDataSetChanged()
    }
}
