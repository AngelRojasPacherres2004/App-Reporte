package com.example.appreporte.codigo_vista_pantalla_docente

import com.example.appreporte.codigo_logica_docente.AlumnosAdapter
import com.example.appreporte.codigo_logica_login.DatabaseHelper
import com.example.appreporte.R
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
    private lateinit var dbHelper: DatabaseHelper
    private var classroomId: String = ""
    private var parentEmails: List<String> = emptyList()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnosListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        classroomId = intent.getStringExtra("CLASSROOM_ID") ?: ""
        val classroomName = intent.getStringExtra("CLASSROOM_NAME") ?: "Salón"

        binding.tvTituloSalon.text = "Alumnos: $classroomName"
        binding.ivBackAlumnos.setOnClickListener { finish() }

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
        firestore.collection("users")
            .whereIn("rol", listOf("usuario", "padre"))
            .get()
            .addOnSuccessListener { snapshot ->
                parentEmails = snapshot.documents.mapNotNull { it.getString("email") }
            }
            .addOnFailureListener {
                // Fallback to SQLite if Firestore fails, though we prefer Firestore
                parentEmails = dbHelper.getParents()
            }
    }

    private fun loadStudents() {
        if (classroomId.isNotEmpty()) {
            firestore.collection("students")
                .whereEqualTo("classroom_id", classroomId)
                .get()
                .addOnSuccessListener { result ->
                    val students = result.map { doc ->
                        mapOf(
                            "id" to doc.id,
                            "names" to (doc.getString("names") ?: ""),
                            "lastnames" to (doc.getString("lastnames") ?: ""),
                            "dni" to (doc.getString("dni") ?: ""),
                            "correo" to (doc.getString("correo") ?: ""),
                            "parent_email" to (doc.getString("parent_email") ?: "")
                        )
                    }
                    adapter.updateData(students)
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
            dialogBinding.etGmailAlumno.setText(alumno["correo"])
            val parentPos = parentEmails.indexOf(alumno["parent_email"])
            if (parentPos != -1) dialogBinding.spinnerPadres.setSelection(parentPos)
        }

        builder.setTitle(if (alumno == null) "Añadir Alumno" else "Editar Alumno")
        builder.setPositiveButton("Guardar") { _, _ ->
            val names = dialogBinding.etNombresAlumno.text.toString()
            val lastnames = dialogBinding.etApellidosAlumno.text.toString()
            val dni = dialogBinding.etDniAlumno.text.toString()
            val gmail = dialogBinding.etGmailAlumno.text.toString()
            val parentEmail = dialogBinding.spinnerPadres.selectedItem?.toString() ?: ""

            if (names.isNotEmpty() && lastnames.isNotEmpty() && dni.isNotEmpty()) {
                val data = hashMapOf(
                    "names" to names,
                    "lastnames" to lastnames,
                    "dni" to dni,
                    "classroom_id" to classroomId,
                    "parent_email" to parentEmail,
                    "correo" to gmail
                )

                if (alumno == null) {
                    firestore.collection("students").add(data)
                        .addOnSuccessListener {
                            loadStudents()
                            Toast.makeText(this, "Alumno guardado", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    firestore.collection("students").document(alumno["id"]!!).set(data)
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
                firestore.collection("students").document(id).delete()
                    .addOnSuccessListener {
                        loadStudents()
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

