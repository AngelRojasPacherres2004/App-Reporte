package com.example.appreporte.codigo_vista_pantalla_docente

import com.example.appreporte.codigo_logica_docente.AlumnosAdapter
import com.example.appreporte.codigo_logica_docente.EmailNotificationWorker
import com.example.appreporte.R
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityAlumnosListaBinding
import com.example.appreporte.databinding.DialogAddGradeBinding
import com.itextpdf.kernel.pdf.CompressionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class AlumnosReporteListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnosListaBinding
    private var classroomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnosListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classroomId = intent.getStringExtra("CLASSROOM_ID") ?: ""
        val classroomName = intent.getStringExtra("CLASSROOM_NAME") ?: "Salón"

        binding.tvTituloSalon.text = getString(R.string.students_of, classroomName)
        binding.ivBackAlumnos.setOnClickListener { finish() }
        binding.fabAddAlumno.hide()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = AlumnosAdapter(emptyList(),
            onEdit = { _ -> },
            onDelete = { _ -> },
            onItemClick = { id, name ->
                showStudentOptionsDialog(id, name)
            },
            hideActions = true
        )
        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = adapter

        FirebaseFirestore.getInstance().collection("students")
            .whereEqualTo("classroom_id", classroomId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data?.mapValues { it.value.toString() }?.toMutableMap()
                    data?.put("id", doc.id)
                    data
                }
                adapter.updateData(list)
            }
    }

    private fun showStudentOptionsDialog(studentId: String, studentName: String) {
        val options = arrayOf(
            getString(R.string.add_grade),
            getString(R.string.edit_grade),
            getString(R.string.generate_pdf_report)
        )
        AlertDialog.Builder(this)
            .setTitle(studentName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showGradeTypeDialog(studentId, studentName)
                    1 -> showEditGradesSelectionDialog(studentId, studentName)
                    2 -> generateAndSendReport(studentId, studentName)
                }
            }
            .show()
    }

    private fun showEditGradesSelectionDialog(studentId: String, studentName: String) {
        FirebaseFirestore.getInstance().collection("grades")
            .whereEqualTo("student_id", studentId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No hay notas registradas", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val gradesList = documents.map { it to it.data }
                val options = gradesList.map { (_, data) ->
                    "${data["subject"]} - ${data["type"]?.toString()?.uppercase()}: ${data["value"]} (${data["date"]})"
                }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Seleccione nota para editar")
                    .setItems(options) { _, which ->
                        val (doc, data) = gradesList[which]
                        showEditGradeDialog(doc.id, data, studentName)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
    }

    private fun showEditGradeDialog(gradeId: String, currentData: Map<String, Any?>, studentName: String) {
        val dialogBinding = DialogAddGradeBinding.inflate(layoutInflater)
        val types = arrayOf("Diaria", "Mensual", "Bimestral")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        dialogBinding.spinnerGradeType.adapter = adapter

        val currentType = currentData["type"]?.toString()?.lowercase() ?: "diaria"
        val typeIndex = types.indexOfFirst { it.lowercase() == currentType }.let { if (it == -1) 0 else it }
        dialogBinding.spinnerGradeType.setSelection(typeIndex)
        dialogBinding.etSubject.setText(currentData["subject"]?.toString() ?: "")
        dialogBinding.etGradeValue.setText(currentData["value"]?.toString() ?: "")

        AlertDialog.Builder(this)
            .setTitle("Editar Nota - $studentName")
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val type = dialogBinding.spinnerGradeType.selectedItem.toString().lowercase()
                val value = dialogBinding.etGradeValue.text.toString()
                val subject = dialogBinding.etSubject.text.toString()

                if (value.isNotEmpty() && subject.isNotEmpty()) {
                    val updateData = hashMapOf("type" to type, "value" to value, "subject" to subject)
                    FirebaseFirestore.getInstance().collection("grades").document(gradeId).update(updateData as Map<String, Any>)
                        .addOnSuccessListener { Toast.makeText(this, "Nota actualizada", Toast.LENGTH_SHORT).show() }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showGradeTypeDialog(studentId: String, studentName: String) {
        val dialogBinding = DialogAddGradeBinding.inflate(layoutInflater)
        val types = arrayOf("Diaria", "Mensual", "Bimestral")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        dialogBinding.spinnerGradeType.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.assign_grade_to, studentName))
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val type = dialogBinding.spinnerGradeType.selectedItem.toString().lowercase()
                val value = dialogBinding.etGradeValue.text.toString()
                val subject = dialogBinding.etSubject.text.toString()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (value.isNotEmpty() && subject.isNotEmpty()) {
                    val gradeData = hashMapOf("student_id" to studentId, "type" to type, "value" to value, "subject" to subject, "date" to date)
                    FirebaseFirestore.getInstance().collection("grades").add(gradeData)
                        .addOnSuccessListener {
                            Toast.makeText(this, R.string.grade_assigned_success, Toast.LENGTH_SHORT).show()
                            simulateNotificationTrigger(studentId, studentName, subject, value, type)
                        }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun generateAndSendReport(studentId: String, studentName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val studentSnap = FirebaseFirestore.getInstance().collection("students").document(studentId).get().await()
                val parentAppEmail = studentSnap.getString("parent_email") ?: ""
                
                var targetGmail = ""
                if (parentAppEmail.isNotEmpty()) {
                    val parentSnap = FirebaseFirestore.getInstance().collection("users").document(parentAppEmail).get().await()
                    if (parentSnap.exists()) {
                        targetGmail = parentSnap.getString("correo_reportes") ?: ""
                    }
                }

                val gradesSnap = FirebaseFirestore.getInstance().collection("grades").whereEqualTo("student_id", studentId).get().await()
                val grades = gradesSnap.documents.mapNotNull { doc -> doc.data?.mapValues { it.value.toString() } }

                val pdfFile = File(cacheDir, "Reporte_${studentName.replace(" ", "_")}.pdf")
                val writerProperties = WriterProperties()
                    .setCompressionLevel(CompressionConstants.BEST_COMPRESSION)
                    .setFullCompressionMode(true)
                
                val writer = PdfWriter(FileOutputStream(pdfFile), writerProperties)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                document.add(Paragraph("Reporte Académico").setBold().setFontSize(18f))
                document.add(Paragraph("Estudiante: $studentName"))
                document.add(Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"))
                document.add(Paragraph("\n"))

                val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 3f, 2f))).useAllAvailableWidth()
                table.addHeaderCell("Materia"); table.addHeaderCell("Tipo"); table.addHeaderCell("Fecha"); table.addHeaderCell("Nota")

                for (grade in grades) {
                    table.addCell(grade["subject"] ?: ""); table.addCell(grade["type"] ?: ""); table.addCell(grade["date"] ?: ""); table.addCell(grade["value"] ?: "")
                }
                document.add(table)
                document.close()

                saveToDownloads(pdfFile)

                if (targetGmail.isNotEmpty()) {
                    val data = Data.Builder()
                        .putString("student_name", studentName)
                        .putString("subject", "Reporte Académico PDF - $studentName")
                        .putString("message", "Estimado Padre de Familia, adjunto encontrará el reporte académico detallado de $studentName.\n\nSaludos,\nEquipo EduConnect")
                        .putString("recipient_email", targetGmail)
                        .putString("attachment_path", pdfFile.absolutePath)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<EmailNotificationWorker>().setInputData(data).build()
                    WorkManager.getInstance(applicationContext).enqueue(workRequest)
                    Log.d("AlumnosReporte", "Reporte PDF encolado para $targetGmail")

                    // Registro para la App (Simulación de "Recibido en Gmail")
                    val notifData = hashMapOf(
                        "recipient_email" to targetGmail,
                        "subject" to "Reporte Académico PDF - $studentName",
                        "message" to "Estimado Padre de Familia, adjunto encontrará el reporte académico detallado de $studentName.",
                        "timestamp" to com.google.firebase.Timestamp.now(),
                        "type" to "pdf_report"
                    )
                    FirebaseFirestore.getInstance().collection("notifications").add(notifData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { Toast.makeText(this@AlumnosReporteListaActivity, "Error al generar reporte", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun simulateNotificationTrigger(studentId: String, studentName: String, subject: String, value: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val studentSnap = FirebaseFirestore.getInstance().collection("students").document(studentId).get().await()
                val parentAppEmail = studentSnap.getString("parent_email") ?: ""
                var targetGmail = ""

                if (parentAppEmail.isNotEmpty()) {
                    val parentSnap = FirebaseFirestore.getInstance().collection("users").document(parentAppEmail).get().await()
                    if (parentSnap.exists()) {
                        targetGmail = parentSnap.getString("correo_reportes") ?: ""
                    }
                }

                if (targetGmail.isNotEmpty()) {
                    val data = Data.Builder()
                        .putString("student_name", studentName)
                        .putString("subject", "Nueva Nota Registrada - $studentName")
                        .putString("message", "Estimado padre de familia, acabamos de subir una nueva calificación para su hijo $studentName.\n\nMateria: $subject\nTipo: ${type.uppercase()}\nCalificación: $value\n\nEl reporte actualizado ya está disponible.\n\nSaludos,\nEduConnect")
                        .putString("recipient_email", targetGmail)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<EmailNotificationWorker>().setInputData(data).build()
                    WorkManager.getInstance(applicationContext).enqueue(workRequest)
                    Log.d("AlumnosReporte", "Notificación de nota encolada para $targetGmail")

                    // Registro para la App (Simulación de "Recibido en Gmail")
                    val notifData = hashMapOf(
                        "recipient_email" to targetGmail,
                        "subject" to "Nueva Nota Registrada - $studentName",
                        "message" to "Materia: $subject\nTipo: ${type.uppercase()}\nCalificación: $value\n\nEl reporte actualizado ya está disponible.",
                        "timestamp" to com.google.firebase.Timestamp.now(),
                        "type" to "nota"
                    )
                    FirebaseFirestore.getInstance().collection("notifications").add(notifData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendToWhatsApp(file: File, phone: String, studentName: String) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val digitsOnly = phone.replace("\\D".toRegex(), "")
        val finalPhone = if (digitsOnly.startsWith("51")) digitsOnly else "51$digitsOnly"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Hola, adjunto el reporte académico de $studentName.")
            putExtra("jid", "$finalPhone@s.whatsapp.net")
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try { startActivity(intent) } catch (e: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
            }, "Compartir reporte")
            startActivity(chooser)
        }
    }

    private fun saveToDownloads(file: File) {
        val resolver = contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { out -> FileInputStream(file).use { it.copyTo(out) } }
            }
        }
    }
}

