package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GradesAdapter(private var grades: List<Map<String, String>>) :
    RecyclerView.Adapter<GradesAdapter.GradeViewHolder>() {

    class GradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvGradeSubject)
        val tvTypeDate: TextView = view.findViewById(R.id.tvGradeTypeDate)
        val tvValue: TextView = view.findViewById(R.id.tvGradeValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grade_history, parent, false)
        return GradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val grade = grades[position]
        holder.tvSubject.text = grade["subject"]
        holder.tvTypeDate.text = "${grade["type"]?.uppercase()} - ${grade["date"]}"
        holder.tvValue.text = grade["value"]
    }

    override fun getItemCount() = grades.size

    fun updateData(newGrades: List<Map<String, String>>) {
        grades = newGrades
        notifyDataSetChanged()
    }
}