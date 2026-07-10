package com.example.appreporte.codigo_vista_pantalla_padre


import com.example.appreporte.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityDocenteQuejasBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ComplaintsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocenteQuejasBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: ComplaintsAdapter

    private var salonName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocenteQuejasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        salonName = intent.getStringExtra("SALON_NAME")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.toolbar.title = if (salonName != null) "Quejas - $salonName" else "Todas las Quejas"

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = ComplaintsAdapter(emptyList()) { complaintId ->
            showStatusDialog(complaintId)
        }
        binding.rvComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvComplaints.adapter = adapter
        refreshList()
    }

    private fun showStatusDialog(complaintId: String) {
        val options = arrayOf("no atendido", "en proceso", "atendido")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar nuevo estado")
        builder.setItems(options) { _, which ->
            val newStatus = options[which]
            firestore.collection("complaints").document(complaintId).update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }

    private fun refreshList() {
        // Se ha detectado que el orderBy con campos inexistentes o nulos puede filtrar resultados.
        // Consultamos la colección completa y ordenamos en memoria para asegurar que se vean todas las quejas.
        firestore.collection("complaints").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(this, "Error al cargar quejas", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            
            val list = mutableListOf<Map<String, String>>()
            snapshot?.documents?.forEach { doc ->
                val map = mutableMapOf<String, String>()
                map["id"] = doc.id
                
                // Intentar obtener un origen descriptivo según los campos disponibles
                val postId = doc.getString("postId")
                val studentId = doc.getString("student_id")
                map["post_title"] = when {
                    !postId.isNullOrEmpty() -> "Post: $postId"
                    !studentId.isNullOrEmpty() -> "Chatbot (Estudiante: $studentId)"
                    else -> "General / Otros"
                }
                
                map["parent_email"] = doc.getString("parentEmail") ?: "Anónimo"
                map["content"] = doc.getString("content") ?: ""
                map["status"] = doc.getString("status") ?: "en proceso"
                
                // Guardar el timestamp para ordenar localmente (soporta tanto Long como Timestamp)
                val ts = doc.get("timestamp") ?: doc.get("date") ?: 0L
                map["ts_raw"] = ts.toString()
                
                list.add(map)
            }
            
            // Ordenar localmente para evitar problemas de índices en Firestore
            val sortedList = list.sortedByDescending { it["ts_raw"] }
            adapter.updateData(sortedList)
        }
    }

    class ComplaintsAdapter(
        private var items: List<Map<String, String>>,
        private val onStatusClick: (String) -> Unit
    ) : RecyclerView.Adapter<ComplaintsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvComplaintTitle)
            val tvParent: TextView = view.findViewById(R.id.tvComplaintParent)
            val tvContent: TextView = view.findViewById(R.id.tvComplaintContent)
            val tvStatus: TextView = view.findViewById(R.id.tvComplaintStatus)
            val btnStatus: MaterialButton = view.findViewById(R.id.btnChangeStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvTitle.text = item["post_title"]
            holder.tvParent.text = "De: ${item["parent_email"]}"
            holder.tvContent.text = item["content"]
            holder.tvStatus.text = "Estado: ${item["status"]}"
            
            holder.btnStatus.setOnClickListener {
                onStatusClick(item["id"] ?: "")
            }
        }

        override fun getItemCount() = items.size

        fun updateData(newItems: List<Map<String, String>>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
}
