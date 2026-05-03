package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var users: List<Map<String, String>>,
    private val onUserClick: (String) -> Unit,
    private val onUserLongClick: (Map<String, String>) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

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
        val email = user["email"] ?: ""
        val rol = user["rol"] ?: ""
        
        holder.tvEmail.text = email
        holder.tvRole.text = "Rol: $rol"
        
        holder.itemView.setOnClickListener {
            onUserClick(email)
        }

        holder.itemView.setOnLongClickListener {
            onUserLongClick(user)
            true
        }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<Map<String, String>>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
