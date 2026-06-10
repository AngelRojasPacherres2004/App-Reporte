package com.example.appreporte.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class AttendanceRecordAdapter(
    private val records: List<Map<String, Any>>
) : RecyclerView.Adapter<AttendanceRecordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvAttendanceDate)
        val tvCourse: TextView = view.findViewById(R.id.tvAttendanceCourse)
        val chipStatus: Chip = view.findViewById(R.id.chipAttendanceStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.tvDate.text = record["date"]?.toString() ?: "Fecha"
        holder.tvCourse.text = record["course_name"]?.toString() ?: "General"
        
        val status = record["status"]?.toString() ?: "Presente"
        holder.chipStatus.text = status

        when (status) {
            "Presente" -> holder.chipStatus.setChipBackgroundColorResource(R.color.stable_green)
            "Tardanza" -> holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_dark)
            "Falta" -> holder.chipStatus.setChipBackgroundColorResource(R.color.priority_red)
            else -> holder.chipStatus.setChipBackgroundColorResource(R.color.primary)
        }
    }

    override fun getItemCount() = records.size
}

