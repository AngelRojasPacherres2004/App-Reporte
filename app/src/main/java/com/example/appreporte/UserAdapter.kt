package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private var users: List<Triple<String, String, String>>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvRole: TextView = view.findViewById(R.id.tvUserRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvEmail.text = user.first
        holder.tvRole.text = "Rol: ${user.third}"
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<Triple<String, String, String>>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
