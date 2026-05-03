package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ItemAlumnoBinding

class AlumnosAdapter(
    private var alumnos: List<Map<String, String>>,
    private val onEdit: (Map<String, String>) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onItemClick: ((Int, String) -> Unit)? = null
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

        holder.binding.btnEditAlumno.setOnClickListener { onEdit(alumno) }
        holder.binding.btnDeleteAlumno.setOnClickListener { 
            onDelete(alumno["id"]?.toInt() ?: -1)
        }

        holder.itemView.setOnClickListener {
            val id = alumno["id"]?.toInt() ?: -1
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