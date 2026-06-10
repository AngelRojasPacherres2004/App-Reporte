package com.example.appreporte.ui.docente

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityAlumnosListaBinding
import com.google.firebase.firestore.FirebaseFirestore

class AlumnosListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnosListaBinding
    private lateinit var adapter: AlumnosAdapter
    private var classroomId: String = ""
    private var parentEmails: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnosListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classroomId = intent.getStringExtra("CLASSROOM_ID") ?: ""
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
            onDelete = { id -> confirmDelete(id) },
            onItemClick = null
        )
        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = adapter
    }

    private fun loadParents() {
        FirebaseFirestore.getInstance().collection("users")
            .whereIn("rol", listOf("padre", "usuario"))
            .get()
            .addOnSuccessListener { snapshot ->
                parentEmails = snapshot.documents.mapNotNull { it.getString("email") }
            }
    }

    private fun loadStudents() {
        FirebaseFirestore.getInstance().collection("students")
            .whereEqualTo("classroom_id", classroomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data?.mapValues { it.value.toString() }?.toMutableMap()
                        data?.put("id", doc.id)
                        data
                    }
                    adapter.updateData(list)
                }
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
                val data = hashMapOf(
                    "names" to names,
                    "lastnames" to lastnames,
                    "dni" to dni,
                    "parent_email" to parentEmail,
                    "classroom_id" to classroomId,
                    "classroom_name" to (intent.getStringExtra("CLASSROOM_NAME") ?: "Salón"),
                    "school_id" to (intent.getStringExtra("SCHOOL_ID") ?: "Colegio San José"),
                    "created_at" to com.google.firebase.Timestamp.now()
                )

                if (alumno == null) {
                    FirebaseFirestore.getInstance().collection("students")
                        .add(data)
                        .addOnSuccessListener {
                            loadStudents()
                            Toast.makeText(this, "Alumno asignado al padre $parentEmail", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    FirebaseFirestore.getInstance().collection("students")
                        .document(alumno["id"]!!)
                        .update(data as Map<String, Any>)
                        .addOnSuccessListener {
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

    private fun confirmDelete(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Alumno")
            .setMessage("¿Estás seguro de eliminar este alumno?")
            .setPositiveButton("Eliminar") { _, _ ->
                FirebaseFirestore.getInstance().collection("students").document(id)
                    .delete()
                    .addOnSuccessListener {
                        loadStudents()
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
