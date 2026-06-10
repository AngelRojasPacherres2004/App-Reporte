package com.example.appreporte.ui.docente

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
        var query: Query = firestore.collection("complaints").orderBy("timestamp", Query.Direction.DESCENDING)
        if (salonName != null) {
            // Note: Si salonName es necesario, habria que agregar el salonName al crear queja
            // Omitido para mantener simplicidad si no esta en firebase
        }
        query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val list = mutableListOf<Map<String, String>>()
            snapshot?.documents?.forEach { doc ->
                val map = mutableMapOf<String, String>()
                map["id"] = doc.id
                map["post_title"] = doc.getString("postId") ?: ""
                map["parent_email"] = doc.getString("parentEmail") ?: ""
                map["content"] = doc.getString("content") ?: ""
                map["status"] = doc.getString("status") ?: ""
                list.add(map)
            }
            adapter.updateData(list)
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
            holder.tvTitle.text = "Post: ${item["post_title"]}"
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
