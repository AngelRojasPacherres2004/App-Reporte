package com.example.appreporte

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityGestionAlumnosBinding

class GestionAlumnosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionAlumnosBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var studentAdapter: StudentAdapter
    private var selectedClassroomId: Int = -1
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAlumnosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        userEmail = intent.getStringExtra("USER_EMAIL")
        
        setupSpinners()
        setupRecyclerView()

        binding.btnAddStudent.setOnClickListener {
            addStudent()
        }
    }

    private fun setupSpinners() {
        // Setup Salones Spinner - Filter by teacher's assigned classrooms if email is present
        val salones = if (userEmail != null) {
            val assigned = dbHelper.getUserClassroomsWithNames(userEmail!!)
            if (assigned.isEmpty()) dbHelper.getAllClassrooms() else assigned
        } else {
            dbHelper.getAllClassrooms()
        }
        
        val salonesNames = salones.map { it.second }
        val adapterSalones = ArrayAdapter(this, android.R.layout.simple_spinner_item, salonesNames)
        adapterSalones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSalones.adapter = adapterSalones

        binding.spinnerSalones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (salones.isNotEmpty()) {
                    selectedClassroomId = salones[position].first
                    updateStudentList()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Padres Spinner
        val padres = dbHelper.getParents()
        val adapterPadres = ArrayAdapter(this, android.R.layout.simple_spinner_item, padres)
        adapterPadres.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPadres.adapter = adapterPadres
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter(emptyList(), { studentId ->
            if (dbHelper.deleteStudent(studentId)) {
                Toast.makeText(this, "Alumno eliminado", Toast.LENGTH_SHORT).show()
                updateStudentList()
            }
        }, { studentId, studentName, parentEmail ->
            showReportPeriodDialog(studentId, studentName, parentEmail)
        })
    }

    private fun showReportPeriodDialog(studentId: Int, studentName: String, parentEmail: String) {
        val periods = arrayOf("Diario", "Mensual", "Bimestral")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Seleccionar Periodo de Reporte")
            .setItems(periods) { _, which ->
                val selectedPeriod = periods[which]
                generateAndSendReport(studentId, studentName, parentEmail, selectedPeriod)
            }
            .show()
    }

    private fun generateAndSendReport(studentId: Int, studentName: String, parentEmail: String, period: String) {
        val phone = dbHelper.getParentPhone(parentEmail)
        if (phone.isEmpty()) {
            Toast.makeText(this, "El padre no tiene un teléfono registrado", Toast.LENGTH_SHORT).show()
            return
        }

        val reportGenerator = ReportGenerator(this)
        val reportFile = reportGenerator.generateStudentReport(studentId, studentName, period)

        if (reportFile != null) {
            WhatsAppSender.sendPdfToWhatsApp(this, phone, reportFile)
        } else {
            Toast.makeText(this, "Error al generar el reporte", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addStudent() {
        val name = binding.etNombreAlumno.text.toString().trim()
        val parentEmail = binding.spinnerPadres.selectedItem?.toString() ?: ""

        if (name.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del alumno", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedClassroomId == -1) {
            Toast.makeText(this, "Seleccione un salón", Toast.LENGTH_SHORT).show()
            return
        }

        if (parentEmail.isEmpty()) {
            Toast.makeText(this, "No hay padres registrados", Toast.LENGTH_SHORT).show()
            return
        }

        if (dbHelper.addStudent(name, selectedClassroomId, parentEmail)) {
            Toast.makeText(this, "Alumno añadido con éxito", Toast.LENGTH_SHORT).show()
            binding.etNombreAlumno.text?.clear()
            updateStudentList()
        } else {
            Toast.makeText(this, "Error al añadir alumno", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStudentList() {
        if (selectedClassroomId != -1) {
            val students = dbHelper.getStudentsByClassroom(selectedClassroomId)
            studentAdapter.updateList(students)
        }
    }
}
