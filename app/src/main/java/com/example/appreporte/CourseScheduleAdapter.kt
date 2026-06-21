package com.example.appreporte

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseScheduleAdapter(
    private val dataList: List<Map<String, Any>>,
    private val isPadre: Boolean
) : RecyclerView.Adapter<CourseScheduleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvCourseTime: TextView = view.findViewById(R.id.tvCourseTime)
        val tvCourseTeacher: TextView = view.findViewById(R.id.tvCourseTeacher)
        val btnWhatsApp: ImageButton = view.findViewById(R.id.btnWhatsAppTeacher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvCourseName.text = item["course_name"]?.toString() ?: "Curso Desconocido"
        holder.tvCourseTime.text = item["schedule"]?.toString() ?: "Horario no asignado"
        holder.tvCourseTeacher.text = "Prof: ${item["teacher_name"]?.toString() ?: "No asignado"}"

        if (isPadre) {
            holder.btnWhatsApp.visibility = View.VISIBLE
            holder.btnWhatsApp.setOnClickListener {
                val phone = item["teacher_phone"]?.toString() ?: ""
                if (phone.isNotEmpty()) {
                    val url = "https://api.whatsapp.com/send?phone=$phone"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    holder.itemView.context.startActivity(i)
                }
            }
        } else {
            holder.btnWhatsApp.visibility = View.GONE
        }
    }

    override fun getItemCount() = dataList.size
}
