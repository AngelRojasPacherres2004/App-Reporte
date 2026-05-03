package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityGestionAlumnosSalonesBinding

class GestionReportesSalonesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionAlumnosSalonesBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAlumnosSalonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        
        // Reutilizamos el layout de selección de salones
        binding.tvTituloSeleccionSalon.text = getString(R.string.select_classroom_notes)
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val salones = db.getAllClassrooms()
        val adapter = ClassroomsAdapter(salones, 
            onDelete = {},
            onEdit = { _, _ -> },
            onItemClick = { id, name ->
                val intent = Intent(this, AlumnosReporteListaActivity::class.java)
                intent.putExtra("CLASSROOM_ID", id)
                intent.putExtra("CLASSROOM_NAME", name)
                startActivity(intent)
            }
        )
        binding.rvSalonesAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvSalonesAlumnos.adapter = adapter
    }
}
