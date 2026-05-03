package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassroomsAdapter(
    private var classrooms: List<Pair<Int, String>>,
    private val onDelete: (Int) -> Unit = {},
    private val onEdit: (Int, String) -> Unit = { _, _ -> },
    private val onItemClick: (Int, String) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ClassroomsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvClassroomName)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteClassroom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, name) = classrooms[position]
        holder.tvName.text = name
        
        holder.itemView.setOnClickListener { onItemClick(id, name) }
        
        holder.btnDelete.setOnClickListener { onDelete(id) }
        
        // El botón de editar se podría añadir al item_classroom si se desea, 
        // por ahora usamos el clic largo o el clic normal para navegar.
        holder.itemView.setOnLongClickListener {
            onEdit(id, name)
            true
        }
    }

    override fun getItemCount() = classrooms.size

    fun updateData(newList: List<Pair<Int, String>>) {
        classrooms = newList
        notifyDataSetChanged()
    }
}