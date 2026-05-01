package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityForoSalonesBinding

class ForoSalonesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForoSalonesBinding
    private lateinit var dbHelper: DatabaseHelper
    private var userRole: String = "usuario"
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForoSalonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setupRecyclerView()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        val salones = if (userRole == "admin") {
            dbHelper.getAllClassrooms().map { it.second }
        } else {
            dbHelper.getUserClassroomsWithNames(userEmail).map { it.second }
        }

        binding.rvSalones.layoutManager = LinearLayoutManager(this)
        binding.rvSalones.adapter = SalonesAdapter(salones) { salon ->
            val intent = Intent(this, ForoDetalleActivity::class.java)
            intent.putExtra("SALON_NAME", salon)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_foro
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    finish()
                    true
                }
                R.id.nav_foro -> true
                else -> false
            }
        }
    }

    class SalonesAdapter(private val salones: List<String>, private val onClick: (String) -> Unit) :
        RecyclerView.Adapter<SalonesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvName.text = salones[position]
            holder.itemView.setOnClickListener { onClick(salones[position]) }
        }

        override fun getItemCount() = salones.size
    }
}
