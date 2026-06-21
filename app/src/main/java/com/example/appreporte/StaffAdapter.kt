package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StaffAdapter(
    private var staffList: List<Map<String, String>>
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    class StaffViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvStaffInitials)
        val tvEmail: TextView = view.findViewById(R.id.tvStaffEmail)
        val tvRole: TextView = view.findViewById(R.id.tvStaffRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]
        val email = staff["email"] ?: ""
        val rol = staff["rol"] ?: ""
        
        holder.tvEmail.text = email
        holder.tvRole.text = rol.replaceFirstChar { it.uppercase() }
        holder.tvInitials.text = email.take(1).uppercase()
    }

    override fun getItemCount() = staffList.size

    fun updateStaff(newList: List<Map<String, String>>) {
        staffList = newList
        notifyDataSetChanged()
    }
}
