package com.example.appreporte

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityAlumnosListaBinding
import com.example.appreporte.databinding.DialogAddAlumnoBinding

class AlumnosListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnosListaBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: AlumnosAdapter
    private var classroomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnosListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        classroomId = intent.getIntExtra("CLASSROOM_ID", -1)
        val classroomName = intent.getStringExtra("CLASSROOM_NAME") ?: "Salón"

        binding.tvTituloSalon.text = "Alumnos: $classroomName"

        setupRecyclerView()

        binding.fabAddAlumno.setOnClickListener {
            showAlumnoDialog(null)
        }
    }

    private fun setupRecyclerView() {
        val alumnos = db.getStudentsByClassroom(classroomId)
        adapter = AlumnosAdapter(alumnos, 
            onEdit = { alumno -> showAlumnoDialog(alumno) },
            onDelete = { id -> confirmDelete(id) },
            onItemClick = null
        )
        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = adapter
    }

    private fun showAlumnoDialog(alumno: Map<String, String>?) {
        val dialogBinding = DialogAddAlumnoBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogBinding.root)

        val padres = db.getParents()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, padres)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerPadres.adapter = spinnerAdapter

        if (alumno != null) {
            dialogBinding.etNombresAlumno.setText(alumno["names"])
            dialogBinding.etApellidosAlumno.setText(alumno["lastnames"])
            dialogBinding.etDniAlumno.setText(alumno["dni"])
            val parentPos = padres.indexOf(alumno["parent_email"])
            if (parentPos != -1) dialogBinding.spinnerPadres.setSelection(parentPos)
        }

        builder.setTitle(if (alumno == null) "Añadir Alumno" else "Editar Alumno")
        builder.setPositiveButton("Guardar") { _, _ ->
            val names = dialogBinding.etNombresAlumno.text.toString()
            val lastnames = dialogBinding.etApellidosAlumno.text.toString()
            val dni = dialogBinding.etDniAlumno.text.toString()
            val parentEmail = dialogBinding.spinnerPadres.selectedItem?.toString() ?: ""

            if (names.isNotEmpty() && lastnames.isNotEmpty() && dni.isNotEmpty()) {
                val success = if (alumno == null) {
                    db.addStudent(names, lastnames, dni, classroomId, parentEmail)
                } else {
                    db.updateStudent(alumno["id"]!!.toInt(), names, lastnames, dni, parentEmail, classroomId)
                }

                if (success) {
                    refreshList()
                    Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al guardar (DNI duplicado?)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun confirmDelete(id: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Alumno")
            .setMessage("¿Estás seguro de eliminar este alumno?")
            .setPositiveButton("Eliminar") { _, _ ->
                if (db.deleteStudent(id)) {
                    refreshList()
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun refreshList() {
        adapter.updateData(db.getStudentsByClassroom(classroomId))
    }
}