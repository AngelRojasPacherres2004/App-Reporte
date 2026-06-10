package com.example.appreporte.ui.general

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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ForoSalonesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForoSalonesBinding
    private var userRole: String = "usuario"
    private var userEmail: String = ""
    private var schoolId: String = ""
    private val firestore = FirebaseFirestore.getInstance()
    private val salones = mutableListOf<Map<String, String>>()
    private lateinit var adapter: SalonesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForoSalonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        schoolId = intent.getStringExtra("SCHOOL_ID") ?: ""

        setupRecyclerView()
        setupBottomNavigation()
        loadForums()
    }

    private fun setupRecyclerView() {
        adapter = SalonesAdapter(salones, { salon ->
            val intent = Intent(this, ForoDetalleActivity::class.java)
            intent.putExtra("SALON_NAME", salon["name"])
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }, { salon ->
            if (userRole == "docente" || userRole == "admin") {
                showForumActionDialog(salon)
            }
        })
        binding.rvSalones.layoutManager = LinearLayoutManager(this)
        binding.rvSalones.adapter = adapter
    }

    private fun loadForums() {
        // Load classrooms as forums
        val query = if (schoolId.isNotEmpty())
            firestore.collection("classrooms").whereEqualTo("school_id", schoolId)
        else
            firestore.collection("classrooms")

        query.get().addOnSuccessListener { classSnap ->
            salones.clear()
            classSnap.documents.forEach { doc ->
                val name = doc.getString("name") ?: ""
                salones.add(mapOf("id" to doc.id, "name" to name, "type" to "classroom"))
            }
            // Also load forum posts
            firestore.collection("forums")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener { forumSnap ->
                    forumSnap.documents.forEach { doc ->
                        val name = doc.getString("name") ?: ""
                        if (!salones.any { it["name"] == name }) {
                            salones.add(mapOf("id" to doc.id, "name" to name, "type" to "forum"))
                        }
                    }
                    if (salones.isEmpty()) {
                        salones.add(mapOf("id" to "general", "name" to "General", "type" to "forum"))
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    if (salones.isEmpty()) {
                        salones.add(mapOf("id" to "general", "name" to "General", "type" to "forum"))
                    }
                    adapter.notifyDataSetChanged()
                }
        }.addOnFailureListener {
            salones.add(mapOf("id" to "general", "name" to "General", "type" to "forum"))
            adapter.notifyDataSetChanged()
        }
    }

    private fun showForumActionDialog(salon: Map<String, String>) {
        val forumId = salon["id"] ?: return
        if (forumId == "general") return // Don't edit default
        val currentName = salon["name"] ?: ""

        val options = arrayOf("Modificar", "Eliminar")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(currentName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditForumDialog(forumId, currentName)
                    1 -> confirmDeleteForum(forumId, currentName)
                }
            }
            .show()
    }

    private fun showEditForumDialog(forumId: String, currentName: String) {
        val input = com.google.android.material.textfield.TextInputEditText(this)
        input.setText(currentName)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Modificar Foro")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = input.text?.toString() ?: ""
                if (newName.isNotEmpty()) {
                    firestore.collection("forums").document(forumId).update("name", newName)
                        .addOnSuccessListener {
                            android.widget.Toast.makeText(this, "Foro actualizado", android.widget.Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDeleteForum(forumId: String, name: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Foro")
            .setMessage("¿Seguro que deseas eliminar el foro '$name'?")
            .setPositiveButton("Eliminar") { _, _ ->
                firestore.collection("forums").document(forumId).delete()
                    .addOnSuccessListener {
                        android.widget.Toast.makeText(this, "Foro eliminado", android.widget.Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    private fun setupBottomNavigation() {
        val menuRes = when (userRole) {
            "admin" -> R.menu.bottom_nav_menu_admin
            "docente" -> R.menu.bottom_nav_menu_docente
            else -> R.menu.bottom_nav_menu
        }
        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(menuRes)
        
        binding.bottomNavigation.selectedItemId = R.id.nav_foro
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    finish()
                    true
                }
                R.id.nav_gestion -> {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_reportes -> {
                    val intent = Intent(this, GestionReportesSalonesActivity::class.java)
                    intent.putExtra("SCHOOL_ID", intent.getStringExtra("SCHOOL_ID"))
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    startActivity(Intent(this, AsistenteActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    val perfilIntent = Intent(this, PerfilActivity::class.java)
                    perfilIntent.putExtra("USER_EMAIL", userEmail)
                    perfilIntent.putExtra("USER_ROL", userRole)
                    perfilIntent.putExtra("SCHOOL_ID", schoolId)
                    startActivity(perfilIntent)
                    finish()
                    true
                }
                R.id.nav_foro -> true
                else -> false
            }
        }
    }

    class SalonesAdapter(
        private val salones: List<Map<String, String>>, 
        private val onClick: (Map<String, String>) -> Unit,
        private val onLongClick: (Map<String, String>) -> Unit
    ) : RecyclerView.Adapter<SalonesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvName.text = salones[position]["name"]
            holder.itemView.setOnClickListener { onClick(salones[position]) }
            holder.itemView.setOnLongClickListener { 
                onLongClick(salones[position])
                true
            }
        }

        override fun getItemCount() = salones.size
    }
}

