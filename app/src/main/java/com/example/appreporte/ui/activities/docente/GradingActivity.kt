package com.example.appreporte.ui.activities.docente

import com.example.appreporte.R
import com.example.appreporte.data.local.DatabaseHelper
import com.example.appreporte.workers.EmailNotificationWorker
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityGradingBinding
import com.example.appreporte.databinding.ItemStudentGradeBinding
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class GradingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGradingBinding
    private lateinit var dbHelper: DatabaseHelper
    private var selectedClassroomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setupSalonesSpinner(userEmail)
    }

    private fun setupSalonesSpinner(email: String) {
        val salones = dbHelper.getUserClassroomsWithNames(email)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, salones.map { it.second })
        binding.spinnerSalones.setAdapter(adapter)

        binding.spinnerSalones.setOnItemClickListener { _, _, position, _ ->
            selectedClassroomId = salones[position].first
            loadStudents(selectedClassroomId)
        }
    }

    private fun loadStudents(classroomId: Int) {
        val students = dbHelper.getStudentsByClassroom(classroomId)
        binding.rvStudentsGrades.layoutManager = LinearLayoutManager(this)
        binding.rvStudentsGrades.adapter = GradeAdapter(students) { studentId, grade ->
            saveGrade(studentId, grade)
        }
    }

    private fun saveGrade(studentId: Int, gradeStr: String) {
        val grade = gradeStr.toDoubleOrNull()
        if (grade == null) {
            Toast.makeText(this, "Ingrese una nota válida", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val success = dbHelper.addGrade(studentId, "General", grade, date, "Diario")
        
        if (success) {
            Toast.makeText(this, "Nota guardada correctamente", Toast.LENGTH_SHORT).show()
            // Disparar notificación por correo
            triggerEmailNotification(studentId, gradeStr)
        } else {
            Toast.makeText(this, "Error al guardar nota", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerEmailNotification(studentId: Int, grade: String) {
        val studentData = dbHelper.getStudentById(studentId)
        val firestoreId = studentData["firestore_id"] ?: ""
        val studentFullName = "${studentData["names"]} ${studentData["lastnames"]}"

        if (firestoreId.isNotEmpty()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("students").document(firestoreId).get().addOnSuccessListener { doc ->
                // El alumno no tiene correo. Buscamos directamente al padre.
                val parentAppEmail = doc.getString("parent_email") ?: studentData["parent_email"] ?: ""
                
                if (parentAppEmail.isNotEmpty()) {
                    db.collection("users").document(parentAppEmail).get().addOnSuccessListener { pDoc ->
                        val recipientGmail = pDoc.getString("correo_reportes") ?: ""
                        if (recipientGmail.isNotEmpty()) {
                            sendToWorker(studentFullName, recipientGmail, grade)
                        } else {
                            Log.w("GradingActivity", "El padre $parentAppEmail no tiene configurado correo_reportes")
                        }
                    }
                } else {
                    Log.w("GradingActivity", "El estudiante no tiene un padre asociado")
                }
            }
        }
    }

    private fun sendToWorker(studentName: String, email: String, grade: String) {
        val data = androidx.work.Data.Builder()
            .putString("student_name", studentName)
            .putString("subject", "Notificación de Nota - $studentName")
            .putString("message", "Estimado padre de familia, acabamos de subir la nota (diaria) de su hijo $studentName y ya está cargado en su sección de reportes.\n\nCalificación: $grade\n\nSaludos,\nEduConnect")
            .putString("recipient_email", email)
            .build()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<EmailNotificationWorker>()
            .setInputData(data)
            .build()

        androidx.work.WorkManager.getInstance(this).enqueue(workRequest)

        // Sincronizar con la App (Bandeja de Notificaciones Gmail)
        val notifData = hashMapOf(
            "recipient_email" to email,
            "subject" to "Notificación de Nota - $studentName",
            "message" to "Estimado padre de familia, acabamos de subir la nota (diaria) de su hijo $studentName y ya está cargado en su sección de reportes.\n\nCalificación: $grade",
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to "nota"
        )
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("notifications").add(notifData)
    }

    class GradeAdapter(
        private val students: List<Map<String, String>>,
        private val onSave: (Int, String) -> Unit
    ) : RecyclerView.Adapter<GradeAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemStudentGradeBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentGradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = students[position]
            val studentId = student["id"]?.toInt() ?: -1
            holder.binding.tvStudentName.text = "${student["names"]} ${student["lastnames"]}"
            holder.binding.btnSaveGrade.setOnClickListener {
                if (studentId != -1) onSave(studentId, holder.binding.etGrade.text.toString())
            }
        }

        override fun getItemCount() = students.size
    }
}
