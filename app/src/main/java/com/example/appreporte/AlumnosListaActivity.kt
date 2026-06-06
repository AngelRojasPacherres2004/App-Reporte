package com.example.appreporte

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityAlumnosListaBinding

class AlumnosListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnosListaBinding
    private lateinit var adapter: AlumnosAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var classroomId: Int = -1
    private var parentEmails: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnosListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        classroomId = intent.getIntExtra("CLASSROOM_ID", -1)
        val classroomName = intent.getStringExtra("CLASSROOM_NAME") ?: "Salón"

        binding.tvTituloSalon.text = "Alumnos: $classroomName"

        setupRecyclerView()
        loadParents()
        loadStudents()

        binding.fabAddAlumno.setOnClickListener {
            showAlumnoDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = AlumnosAdapter(emptyList(), 
            onEdit = { alumno -> showAlumnoDialog(alumno) },
            onDelete = { id -> confirmDelete(id.toInt()) },
            onItemClick = null
        )
        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = adapter
    }

    private fun loadParents() {
        parentEmails = dbHelper.getParents()
    }

    private fun loadStudents() {
        if (classroomId != -1) {
            val students = dbHelper.getStudentsByClassroom(classroomId)
            // Need to convert Triple to Map for AlumnosAdapter
            val mappedStudents = students.map {
                val namesParts = it.second.split(" ")
                val firstName = namesParts.getOrElse(0) { "" }
                val lastName = if (namesParts.size > 1) namesParts.subList(1, namesParts.size).joinToString(" ") else ""
                
                mapOf(
                    "id" to it.first.toString(),
                    "names" to firstName,
                    "lastnames" to lastName,
                    "dni" to it.third,
                    "parent_email" to ""
                )
            }
            adapter.updateData(mappedStudents)
        }
    }

    private fun showAlumnoDialog(alumno: Map<String, String>?) {
        val dialogBinding = com.example.appreporte.databinding.DialogAddAlumnoBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogBinding.root)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, parentEmails)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerPadres.adapter = spinnerAdapter

        if (alumno != null) {
            dialogBinding.etNombresAlumno.setText(alumno["names"])
            dialogBinding.etApellidosAlumno.setText(alumno["lastnames"])
            dialogBinding.etDniAlumno.setText(alumno["dni"])
            val parentPos = parentEmails.indexOf(alumno["parent_email"])
            if (parentPos != -1) dialogBinding.spinnerPadres.setSelection(parentPos)
        }

        builder.setTitle(if (alumno == null) "Añadir Alumno" else "Editar Alumno")
        builder.setPositiveButton("Guardar") { _, _ ->
            val names = dialogBinding.etNombresAlumno.text.toString()
            val lastnames = dialogBinding.etApellidosAlumno.text.toString()
            val dni = dialogBinding.etDniAlumno.text.toString()
            val parentEmail = dialogBinding.spinnerPadres.selectedItem?.toString() ?: ""

            if (names.isNotEmpty() && lastnames.isNotEmpty() && dni.isNotEmpty()) {
                if (alumno == null) {
                    if (dbHelper.addStudent(names, lastnames, dni, classroomId, parentEmail)) {
                        loadStudents()
                        Toast.makeText(this, "Alumno guardado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (dbHelper.updateStudent(alumno["id"]!!.toInt(), names, lastnames, dni, parentEmail, classroomId)) {
                        loadStudents()
                        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    }
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
                if (dbHelper.deleteStudent(id)) {
                    loadStudents()
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
