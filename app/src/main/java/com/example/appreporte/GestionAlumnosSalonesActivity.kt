package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityGestionAlumnosSalonesBinding

class GestionAlumnosSalonesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionAlumnosSalonesBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAlumnosSalonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val salones = db.getAllClassrooms()
        val adapter = ClassroomsAdapter(salones, 
            onDelete = { id -> 
                // No habilitado borrar aquí para evitar conflictos, solo navegación
            },
            onEdit = { id, name -> 
                // Solo navegación
            },
            onItemClick = { id, name ->
                val intent = Intent(this, AlumnosListaActivity::class.java)
                intent.putExtra("CLASSROOM_ID", id)
                intent.putExtra("CLASSROOM_NAME", name)
                startActivity(intent)
            }
        )
        binding.rvSalonesAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvSalonesAlumnos.adapter = adapter
    }
}
