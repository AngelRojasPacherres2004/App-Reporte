package com.example.appreporte.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassroomAdapter(
    private var classrooms: List<Pair<String, String>>,
    private val onDeleteClick: (String) -> Unit,
    private val onItemClick: ((String, String) -> Unit)? = null
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
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(classroom.first, classroom.second)
        }
    }

    override fun getItemCount() = classrooms.size

    fun updateClassrooms(newClassrooms: List<Pair<String, String>>) {
        classrooms = newClassrooms
        notifyDataSetChanged()
    }
}

