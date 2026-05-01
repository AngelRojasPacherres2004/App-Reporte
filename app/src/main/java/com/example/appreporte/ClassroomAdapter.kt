package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassroomAdapter(
    private var classrooms: List<Pair<Int, String>>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ClassroomAdapter.ClassroomViewHolder>() {

    class ClassroomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvClassroomName)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteClassroom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassroomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
        return ClassroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassroomViewHolder, position: Int) {
        val classroom = classrooms[position]
        holder.tvName.text = classroom.second
        holder.btnDelete.setOnClickListener {
            onDeleteClick(classroom.first)
        }
    }

    override fun getItemCount() = classrooms.size

    fun updateClassrooms(newClassrooms: List<Pair<Int, String>>) {
        classrooms = newClassrooms
        notifyDataSetChanged()
    }
}
