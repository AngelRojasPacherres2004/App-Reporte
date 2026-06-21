package com.example.appreporte

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color

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
        setupBottomNavigation()
        if (studentId.isNotEmpty()) {
            loadStudentData()
        } else {
            binding.tvStudentName.text = "Error: No se proporcionó el ID del estudiante."
            binding.btnDownloadReport.isEnabled = false
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_reportes

        // Ajuste de icono de asistente
        val menuView = binding.bottomNavigation.getChildAt(0) as? ViewGroup
        val assistantItem = menuView?.findViewById<View>(R.id.nav_asistente)
        val iconView = assistantItem?.findViewById<android.widget.ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)
        iconView?.post {
            val params = iconView.layoutParams
            val density = resources.displayMetrics.density
            val sizeInPx = (40 * density).toInt()
            params.width = sizeInPx
            params.height = sizeInPx
            iconView.layoutParams = params
            iconView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = android.content.Intent(this, PadreDashboardActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", "usuario")
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    true
                }
                R.id.nav_foro -> {
                    FirebaseFirestore.getInstance().collection("students").document(studentId).get()
                        .addOnSuccessListener { doc ->
                            val classroomId = doc.getString("classroom_id") ?: ""
                            // También necesitamos el nombre del salón para que el Foro cargue los posts
                            FirebaseFirestore.getInstance().collection("classrooms").document(classroomId).get()
                                .addOnSuccessListener { classDoc ->
                                    val classroomName = classDoc.getString("name") ?: "Foro"
                                    val intent = android.content.Intent(this, ForoDetalleActivity::class.java)
                                    intent.putExtra("CLASSROOM_ID", classroomId)
                                    intent.putExtra("SALON_NAME", classroomName)
                                    intent.putExtra("USER_EMAIL", userEmail)
                                    intent.putExtra("USER_ROL", "usuario")
                                    intent.putExtra("STUDENT_ID", studentId)
                                    startActivity(intent)
                                }
                        }
                    true
                }
                R.id.nav_asistente -> {
                    val intent = android.content.Intent(this, ChatbotPadreActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("STUDENT_ID", studentId)
                    startActivity(intent)
                    true
                }
                R.id.nav_reportes -> true
                R.id.nav_perfil -> {
                    val intent = android.content.Intent(this, PerfilActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", "usuario")
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        binding.rvGradesHistory.layoutManager = LinearLayoutManager(this)
        gradesAdapter = GradesAdapter(emptyList())
        binding.rvGradesHistory.adapter = gradesAdapter

        setupEvolutionChart()
        setupBarChart()

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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar notas: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    gradesList = snapshot.documents.mapNotNull { doc ->
                        doc.data?.mapValues { it.value.toString() }
                    }
                    gradesAdapter.updateData(gradesList)
                    updateCharts(gradesList)
                }
            }
    }

    private fun setupEvolutionChart() {
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK
        val gridColor = if (isNightMode) Color.parseColor("#40FFFFFF") else Color.parseColor("#E0E0E0")

        binding.lineChartEvolution.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            legend.apply {
                this.textColor = textColor
                this.textSize = 13f
                this.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                this.textColor = textColor
                this.textSize = 12f
                this.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            axisRight.isEnabled = false
            
            axisLeft.apply {
                setDrawGridLines(true)
                this.textColor = textColor
                this.textSize = 12f
                this.typeface = android.graphics.Typeface.DEFAULT_BOLD
                this.gridColor = gridColor
            }
            
            animateX(1000)
        }
    }

    private fun setupBarChart() {
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK
        val gridColor = if (isNightMode) Color.parseColor("#40FFFFFF") else Color.parseColor("#E0E0E0")

        binding.barChartAverages.apply {
            description.isEnabled = false
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(60)
            setPinchZoom(false)
            setDrawGridBackground(false)
            
            legend.apply {
                this.textColor = textColor
                this.textSize = 13f
                this.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                this.textColor = textColor
                this.textSize = 12f
                this.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            axisRight.isEnabled = false
            
            axisLeft.apply {
                this.textColor = textColor
                this.textSize = 12f
                this.typeface = android.graphics.Typeface.DEFAULT_BOLD
                this.gridColor = gridColor
            }
            
            animateY(1000)
        }
    }

    private fun updateCharts(grades: List<Map<String, String>>) {
        if (grades.isEmpty()) {
            binding.lineChartEvolution.clear()
            binding.barChartAverages.clear()
            return
        }

        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK

        // 1. Evolution Chart (Line Chart)
        val evolutionEntries = ArrayList<Entry>()
        val sortedGrades = grades.filter { it["value"]?.toDoubleOrNull() != null }
            .sortedBy { it["date"] ?: "" }
        
        val dateLabels = ArrayList<String>()
        sortedGrades.forEachIndexed { index, grade ->
            val value = grade["value"]?.toFloat() ?: 0f
            evolutionEntries.add(Entry(index.toFloat(), value))
            val fullDate = grade["date"] ?: ""
            val displayDate = if (fullDate.length >= 10) fullDate.substring(5) else fullDate
            dateLabels.add(displayDate)
        }

        val lineDataSet = LineDataSet(evolutionEntries, "Evolución de Notas").apply {
            color = Color.parseColor("#64B5F6") // Brighter blue
            setCircleColor(Color.parseColor("#64B5F6"))
            lineWidth = 3.5f // Thicker line
            circleRadius = 7f // Larger circles
            setDrawCircleHole(true)
            circleHoleColor = if (isNightMode) Color.parseColor("#121212") else Color.WHITE
            valueTextSize = 14f // Larger value text
            valueTextColor = textColor
            valueTypeface = android.graphics.Typeface.DEFAULT_BOLD
            setDrawFilled(true)
            fillColor = Color.parseColor("#64B5F6")
            fillAlpha = 80 // More visible fill
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        binding.lineChartEvolution.apply {
            data = LineData(lineDataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
            xAxis.labelCount = if (dateLabels.size > 5) 5 else dateLabels.size
            xAxis.granularity = 1f
            invalidate()
        }

        // 2. Averages Chart (Bar Chart)
        val subjectGrades = grades.groupBy { it["subject"] ?: "Desconocido" }
        val barEntries = ArrayList<BarEntry>()
        val subjects = ArrayList<String>()

        subjectGrades.entries.forEachIndexed { index, entry ->
            val avg = entry.value.mapNotNull { it["value"]?.toDoubleOrNull() }.average()
            if (!avg.isNaN()) {
                barEntries.add(BarEntry(index.toFloat(), avg.toFloat()))
                subjects.add(entry.key)
            }
        }

        val barDataSet = BarDataSet(barEntries, "Promedio por Materia").apply {
            colors = if (isNightMode) {
                listOf(
                    Color.parseColor("#BBDEFB"), // Very Light Blue
                    Color.parseColor("#C8E6C9"), // Very Light Green
                    Color.parseColor("#E1BEE7"), // Very Light Purple
                    Color.parseColor("#FFE0B2"), // Very Light Orange
                    Color.parseColor("#F8BBD0")  // Very Light Pink
                )
            } else {
                listOf(
                    Color.parseColor("#4CAF50"),
                    Color.parseColor("#2196F3"),
                    Color.parseColor("#9C27B0"),
                    Color.parseColor("#FF9800"),
                    Color.parseColor("#E91E63")
                )
            }
            valueTextSize = 14f // Larger value text
            valueTextColor = textColor
            valueTypeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        binding.barChartAverages.apply {
            data = BarData(barDataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(subjects)
            xAxis.labelCount = subjects.size
            xAxis.granularity = 1f
            invalidate()
        }
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