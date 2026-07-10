package com.example.appreporte

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityPadreReporteBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

class PadreReporteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPadreReporteBinding
    private lateinit var gradesAdapter: GradesAdapter
    private var userEmail: String = ""
    private var studentId: String = ""
    private var studentName: String = ""
    private var gradesList: List<Map<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPadreReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""

        setupUI()
        if (studentId.isNotEmpty()) {
            loadStudentData()
        } else {
            binding.tvStudentName.text = "Error: No se proporcionó el ID del estudiante."
            binding.btnDownloadReport.isEnabled = false
        }
    }

    private fun setupUI() {
        binding.rvGradesHistory.layoutManager = LinearLayoutManager(this)
        gradesAdapter = GradesAdapter(emptyList())
        binding.rvGradesHistory.adapter = gradesAdapter

        binding.btnDownloadReport.setOnClickListener {
            if (studentId.isNotEmpty()) {
                generateAndSavePDF()
            } else {
                Toast.makeText(this, "No se encontró información del alumno", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStudentData() {
        FirebaseFirestore.getInstance().collection("students").document(studentId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    studentName = "${doc.getString("names")} ${doc.getString("lastnames")}"
                    binding.tvStudentName.text = "Estudiante: $studentName"
                    loadGrades()
                } else {
                    binding.tvStudentName.text = "Alumno no encontrado."
                    binding.btnDownloadReport.isEnabled = false
                }
            }
            .addOnFailureListener {
                binding.tvStudentName.text = "Error de conexión."
                binding.btnDownloadReport.isEnabled = false
            }
    }

    private fun loadGrades() {
        FirebaseFirestore.getInstance().collection("grades")
            .whereEqualTo("student_id", studentId)
            .get()
            .addOnSuccessListener { snapshot ->
                gradesList = snapshot.documents.mapNotNull { doc ->
                    doc.data?.mapValues { it.value.toString() }
                }
                gradesAdapter.updateData(gradesList)
                
                setupChart()
                
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = android.view.View.GONE
                binding.rvGradesHistory.visibility = android.view.View.VISIBLE
            }
    }

    private fun setupChart() {
        val chart = findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChartGrades)
        if (gradesList.isEmpty()) {
            chart.clear()
            return
        }

        val entries = ArrayList<com.github.mikephil.charting.data.Entry>()
        val labels = ArrayList<String>()

        // Sort by date or just use the index
        var index = 0f
        for (grade in gradesList) {
            val valueStr = grade["value"] ?: "0"
            val value = valueStr.toFloatOrNull() ?: 0f
            entries.add(com.github.mikephil.charting.data.Entry(index, value))
            labels.add(grade["subject"] ?: "Nota")
            index += 1f
        }

        val textColor = androidx.core.content.ContextCompat.getColor(this, R.color.on_surface)
        
        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Evolución de Notas")
        dataSet.color = androidx.core.content.ContextCompat.getColor(this, R.color.primary)
        dataSet.valueTextColor = textColor
        dataSet.valueTextSize = 10f
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(androidx.core.content.ContextCompat.getColor(this, R.color.support_green))
        dataSet.mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER

        val lineData = com.github.mikephil.charting.data.LineData(dataSet)
        chart.data = lineData

        val xAxis = chart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f
        xAxis.textColor = textColor
        
        chart.axisLeft.textColor = textColor
        chart.legend.textColor = textColor
        
        // Espacio extra para que no se corten las etiquetas rotadas
        chart.extraBottomOffset = 40f

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.animateX(1000)
        chart.invalidate()
    }

    private fun generateAndSavePDF() {
        if (gradesList.isEmpty()) {
            Toast.makeText(this, "No hay notas para generar el reporte", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val pdfFile = File(cacheDir, "Reporte_${studentName.replace(" ", "_")}.pdf")
            try {
                val writer = PdfWriter(FileOutputStream(pdfFile))
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                document.add(Paragraph("Reporte Académico").setBold().setFontSize(18f))
                document.add(Paragraph("Estudiante: $studentName"))
                document.add(Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"))
                document.add(Paragraph("\n"))

                val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 3f, 2f))).useAllAvailableWidth()
                table.addHeaderCell(Paragraph("Materia").setBold())
                table.addHeaderCell(Paragraph("Tipo").setBold())
                table.addHeaderCell(Paragraph("Fecha").setBold())
                table.addHeaderCell(Paragraph("Nota").setBold())

                for (grade in gradesList) {
                    table.addCell(grade["subject"] ?: "")
                    table.addCell(grade["type"] ?: "")
                    table.addCell(grade["date"] ?: "")
                    table.addCell(grade["value"] ?: "")
                }

                document.add(table)
                document.close()

                saveToDownloads(pdfFile)

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PadreReporteActivity, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                }
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