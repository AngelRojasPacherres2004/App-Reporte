package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SchoolAdapter(
    private var schools: List<Map<String, Any>>,
    private val onSchoolAction: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {

    class SchoolViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSchoolName: TextView = view.findViewById(R.id.tvSchoolName)
        val tvSchoolLevels: TextView = view.findViewById(R.id.tvSchoolLevels)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_school, parent, false)
        return SchoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val school = schools[position]
        holder.tvSchoolName.text = school["name"]?.toString() ?: "Sin Nombre"
        
        val levels = school["levels"] as? List<String> ?: emptyList()
        val levelsText = if (levels.isNotEmpty()) {
            "Niveles: " + levels.joinToString(", ")
        } else {
            "Sin niveles asignados"
        }
        holder.tvSchoolLevels.text = levelsText
        
        holder.itemView.setOnClickListener {
            onSchoolAction(school)
        }
    }

    override fun getItemCount() = schools.size

    fun updateSchools(newSchools: List<Map<String, Any>>) {
        schools = newSchools
        notifyDataSetChanged()
    }
}
