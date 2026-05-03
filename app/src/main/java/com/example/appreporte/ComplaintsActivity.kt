package com.example.appreporte

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

class ComplaintsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocenteQuejasBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ComplaintsAdapter

    private var salonName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocenteQuejasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        salonName = intent.getStringExtra("SALON_NAME")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.toolbar.title = if (salonName != null) "Quejas - $salonName" else "Todas las Quejas"

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val complaints = db.getAllComplaints(salonName)
        adapter = ComplaintsAdapter(complaints) { complaintId ->
            showStatusDialog(complaintId)
        }
        binding.rvComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvComplaints.adapter = adapter
    }

    private fun showStatusDialog(complaintId: Int) {
        val options = arrayOf("no atendido", "en proceso", "atendido")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar nuevo estado")
        builder.setItems(options) { _, which ->
            val newStatus = options[which]
            if (db.updateComplaintStatus(complaintId, newStatus)) {
                Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                refreshList()
            }
        }
        builder.show()
    }

    private fun refreshList() {
        val complaints = db.getAllComplaints(salonName)
        adapter.updateData(complaints)
    }

    class ComplaintsAdapter(
        private var items: List<Map<String, String>>,
        private val onStatusClick: (Int) -> Unit
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
                onStatusClick(item["id"]?.toInt() ?: -1)
            }
        }

        override fun getItemCount() = items.size

        fun updateData(newItems: List<Map<String, String>>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
}