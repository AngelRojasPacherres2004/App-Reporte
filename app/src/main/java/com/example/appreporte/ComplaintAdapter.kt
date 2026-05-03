package com.example.appreporte

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComplaintAdapter(
    private var list: List<Complaint>,
    private val onItemClick: (Complaint) -> Unit
) : RecyclerView.Adapter<ComplaintAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val email: TextView = view.findViewById(R.id.tvUserEmail)
        val clasificacion: TextView = view.findViewById(R.id.tvClasificacion)
        val mensaje: TextView = view.findViewById(R.id.tvMensaje)
        val estado: TextView = view.findViewById(R.id.tvEstado)
        // HEMOS ELIMINADO LA REFERENCIA AL BOTÓN AQUÍ
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.email.text = item.userEmail
        holder.clasificacion.text = item.clasificacion
        holder.mensaje.text = item.mensaje
        holder.estado.text = "Estado: ${item.estado}"

        // Lógica de colores según el estado
        when (item.estado) {
            "Resuelto" -> {
                holder.estado.setTextColor(Color.parseColor("#81C784")) // Verde
            }
            "Rechazado" -> {
                holder.estado.setTextColor(Color.parseColor("#E57373")) // Rojo
            }
            else -> { // "En Proceso" o "Pendiente"
                holder.estado.setTextColor(Color.parseColor("#FFB74D")) // Naranja
            }
        }

        // Ahora toda la tarjeta abre el menú de "Asignar Estado"
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Complaint>) {
        list = newList
        notifyDataSetChanged()
    }
}