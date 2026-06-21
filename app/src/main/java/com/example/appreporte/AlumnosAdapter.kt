package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ItemAlumnoBinding

class AlumnosAdapter(
    private var alumnos: List<Map<String, String>>,
    private val onEdit: (Map<String, String>) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onItemClick: ((String, String) -> Unit)? = null,
    private val hideActions: Boolean = false
) : RecyclerView.Adapter<AlumnosAdapter.AlumnoViewHolder>() {

    class AlumnoViewHolder(val binding: ItemAlumnoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val binding = ItemAlumnoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlumnoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        val alumno = alumnos[position]
        holder.binding.tvNombreAlumno.text = "${alumno["names"]} ${alumno["lastnames"]}"
        holder.binding.tvDniAlumno.text = "DNI: ${alumno["dni"]}"
        holder.binding.tvPadreAlumno.text = "Padre: ${alumno["parent_email"]}"

        if (hideActions) {
            holder.binding.llAlumnoActions.visibility = View.GONE
        } else {
            holder.binding.llAlumnoActions.visibility = View.VISIBLE
            holder.binding.btnEditAlumno.setOnClickListener { onEdit(alumno) }
            holder.binding.btnDeleteAlumno.setOnClickListener { 
                onDelete(alumno["id"] ?: "")
            }
        }

        holder.itemView.setOnClickListener {
            val id = alumno["id"] ?: ""
            val name = "${alumno["names"]} ${alumno["lastnames"]}"
            onItemClick?.invoke(id, name)
        }
    }

    override fun getItemCount() = alumnos.size

    fun updateData(newList: List<Map<String, String>>) {
        alumnos = newList
        notifyDataSetChanged()
    }
}