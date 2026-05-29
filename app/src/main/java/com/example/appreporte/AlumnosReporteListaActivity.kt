package com.example.appreporte

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
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        binding.fabAddAlumno.hide()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = AlumnosAdapter(emptyList(),
            onEdit = { _ -> /* No editar */ },
            onDelete = { _ -> /* No borrar */ },
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
        val options = arrayOf(getString(R.string.add_grade), getString(R.string.generate_pdf_report))
        AlertDialog.Builder(this)
            .setTitle(studentName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showGradeTypeDialog(studentId, studentName)
                    1 -> generateAndSendReport(studentId, studentName)
                }
            }
            .show()
    }

    private fun showGradeTypeDialog(studentId: String, studentName: String) {
        val dialogBinding = DialogAddGradeBinding.inflate(layoutInflater)
        val types = arrayOf("Diaria", "Mensual", "Bimestral")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                    val gradeData = hashMapOf(
                        "student_id" to studentId,
                        "type" to type,
                        "value" to value,
                        "subject" to subject,
                        "date" to date
                    )
                    FirebaseFirestore.getInstance().collection("grades").add(gradeData)
                        .addOnSuccessListener {
                            Toast.makeText(this, R.string.grade_assigned_success, Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, R.string.grade_assigned_error, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun generateAndSendReport(studentId: String, studentName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch student
                val studentSnap = FirebaseFirestore.getInstance().collection("students").document(studentId).get().await()
                val parentEmail = studentSnap.getString("parent_email") ?: return@launch
                
                // Fetch parent to get phone
                val userSnap = FirebaseFirestore.getInstance().collection("users").whereEqualTo("email", parentEmail).get().await()
                val phone = userSnap.documents.firstOrNull()?.getString("phone") ?: ""

                if (phone.isEmpty()) {
                    withContext(Dispatchers.Main) { Toast.makeText(this@AlumnosReporteListaActivity, R.string.error_no_phone, Toast.LENGTH_LONG).show() }
                    return@launch
                }

                // Fetch grades
                val gradesSnap = FirebaseFirestore.getInstance().collection("grades").whereEqualTo("student_id", studentId).get().await()
                val grades = gradesSnap.documents.mapNotNull { doc ->
                    doc.data?.mapValues { it.value.toString() }
                }

                val pdfFile = File(cacheDir, "Reporte_${studentName.replace(" ", "_")}.pdf")
                
                val writer = PdfWriter(FileOutputStream(pdfFile))
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                document.add(Paragraph("Reporte Académico").setBold().setFontSize(18f))
                document.add(Paragraph("Estudiante: $studentName"))
                document.add(Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"))
                document.add(Paragraph("\n"))

                val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 3f, 2f))).useAllAvailableWidth()
                table.addHeaderCell("Materia")
                table.addHeaderCell("Tipo")
                table.addHeaderCell("Fecha")
                table.addHeaderCell("Nota")

                for (grade in grades) {
                    table.addCell(grade["subject"] ?: "")
                    table.addCell(grade["type"] ?: "")
                    table.addCell(grade["date"] ?: "")
                    table.addCell(grade["value"] ?: "")
                }

                document.add(table)
                document.close()

                saveToDownloads(pdfFile)
                withContext(Dispatchers.Main) { sendToWhatsApp(pdfFile, phone, studentName) }
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { Toast.makeText(this@AlumnosReporteListaActivity, R.string.error_pdf, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun sendToWhatsApp(file: File, phone: String, studentName: String) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        
        // Limpiar número: Solo dígitos y prefijo 51
        val digitsOnly = phone.replace("\\D".toRegex(), "")
        val finalPhone = if (digitsOnly.startsWith("51")) digitsOnly else "51$digitsOnly"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, "Hola, adjunto el reporte académico de $studentName.")
        // JID específico para abrir la conversación directamente
        intent.putExtra("jid", "$finalPhone@s.whatsapp.net")
        intent.setPackage("com.whatsapp")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // Intento con WhatsApp Business
                intent.setPackage("com.whatsapp.w4b")
                startActivity(intent)
            } catch (e2: Exception) {
                // Selector normal si nada falla
                val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }, getString(R.string.share_report))
                startActivity(chooser)
            }
        }
    }

    private fun saveToDownloads(file: File) {
        val fileName = file.name
        val resolver = contentResolver
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                runOnUiThread {
                    Toast.makeText(this, "Reporte guardado en Descargas", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val targetFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            try {
                FileInputStream(file).use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                runOnUiThread {
                    Toast.makeText(this, "Reporte guardado en Descargas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
