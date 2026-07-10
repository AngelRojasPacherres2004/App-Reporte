package com.example.appreporte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.R
import com.example.appreporte.databinding.ActivityCalendarioBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarioBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val events = mutableListOf<Map<String, Any>>()
    private lateinit var adapter: EventsAdapter
    
    private var schoolId: String = ""
    private var userRole: String = ""
    private var selectedDateString: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        schoolId = intent.getStringExtra("SCHOOL_ID") ?: ""
        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        
        binding.toolbarCalendario.setNavigationOnClickListener { finish() }
        
        // Formato inicial (hoy)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        selectedDateString = sdf.format(Date(binding.calendarView.date))
        binding.tvSelectedDate.text = "Eventos para el $selectedDateString"
        
        // Configurar RecyclerView
        adapter = EventsAdapter(events, userRole) { eventId ->
            deleteEvent(eventId)
        }
        binding.rvEventos.layoutManager = LinearLayoutManager(this)
        binding.rvEventos.adapter = adapter
        
        // Si es admin o docente, puede agregar
        if (userRole == "admin" || userRole == "superadmin" || userRole == "docente") {
            binding.fabAddEvento.visibility = View.VISIBLE
            binding.fabAddEvento.setOnClickListener {
                showAddEventDialog()
            }
        }
        
        // Escuchar cambios de fecha
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDateString = sdf.format(calendar.time)
            binding.tvSelectedDate.text = "Eventos para el $selectedDateString"
            filterEvents()
        }
        
        loadEvents()
    }
    
    private fun loadEvents() {
        if (schoolId.isEmpty()) return
        firestore.collection("events")
            .whereEqualTo("school_id", schoolId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                events.clear()
                snapshot?.documents?.forEach { doc ->
                    val map = doc.data?.toMutableMap() ?: mutableMapOf()
                    map["id"] = doc.id
                    events.add(map)
                }
                filterEvents()
            }
    }
    
    private fun filterEvents() {
        val filtered = events.filter { it["date"] == selectedDateString }
        adapter.updateData(filtered)
        if (filtered.isEmpty()) {
            binding.rvEventos.visibility = View.GONE
            binding.tvEmptyEventos.visibility = View.VISIBLE
        } else {
            binding.rvEventos.visibility = View.VISIBLE
            binding.tvEmptyEventos.visibility = View.GONE
        }
    }
    
    private fun showAddEventDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Añadir evento el $selectedDateString")
        
        val layout = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        // Para simplificar, haremos un layout programático:
        
        val linearLayout = android.widget.LinearLayout(this)
        linearLayout.orientation = android.widget.LinearLayout.VERTICAL
        val padding = 48
        linearLayout.setPadding(padding, padding, padding, padding)
        
        val etTitle = EditText(this)
        etTitle.hint = "Título del evento (Ej. Examen de Historia)"
        linearLayout.addView(etTitle)
        
        val etDesc = EditText(this)
        etDesc.hint = "Descripción"
        linearLayout.addView(etDesc)
        
        builder.setView(linearLayout)
        
        builder.setPositiveButton("Guardar") { _, _ ->
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            if (title.isNotEmpty()) {
                val data = hashMapOf(
                    "school_id" to schoolId,
                    "date" to selectedDateString,
                    "title" to title,
                    "description" to desc,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("events").add(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Evento guardado", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
    
    private fun deleteEvent(eventId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar evento")
            .setMessage("¿Estás seguro que deseas eliminar este evento?")
            .setPositiveButton("Eliminar") { _, _ ->
                firestore.collection("events").document(eventId).delete()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    class EventsAdapter(
        private var eventsList: List<Map<String, Any>>,
        private val userRole: String,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
        
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDay: TextView = view.findViewById(R.id.tvEventDay)
            val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
            val tvDesc: TextView = view.findViewById(R.id.tvEventDesc)
            val ivDelete: ImageView = view.findViewById(R.id.ivDeleteEvent)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val event = eventsList[position]
            val dateStr = event["date"] as? String ?: ""
            // extraer dia: 15/10/2026 -> 15
            holder.tvDay.text = dateStr.substringBefore("/")
            holder.tvTitle.text = event["title"] as? String ?: ""
            holder.tvDesc.text = event["description"] as? String ?: ""
            
            if (userRole == "admin" || userRole == "superadmin" || userRole == "docente") {
                holder.ivDelete.visibility = View.VISIBLE
                holder.ivDelete.setOnClickListener {
                    onDeleteClick(event["id"] as String)
                }
            } else {
                holder.ivDelete.visibility = View.GONE
            }
        }
        
        override fun getItemCount() = eventsList.size
        
        fun updateData(newList: List<Map<String, Any>>) {
            eventsList = newList
            notifyDataSetChanged()
        }
    }
}
