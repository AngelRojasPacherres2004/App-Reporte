package com.example.appreporte

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportGenerator(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun generateStudentReport(studentId: Int, studentName: String, period: String): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Calculate start date based on period
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val endDate = sdf.format(calendar.time)
        
        when (period) {
            "Diario" -> { /* Today only, keep as is or filter by today */ }
            "Mensual" -> calendar.add(Calendar.MONTH, -1)
            "Bimestral" -> calendar.add(Calendar.MONTH, -2)
        }
        val startDate = if (period == "Diario") endDate else sdf.format(calendar.time)

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Reporte Académico $period", 50f, 50f, paint)

        // Student Info
        paint.textSize = 16f
        paint.isFakeBoldText = false
        canvas.drawText("Alumno: $studentName", 50f, 90f, paint)
        canvas.drawText("Rango: $startDate al $endDate", 50f, 115f, paint)

        // Real Data from Database with date filtering
        val grades = dbHelper.getGradesByStudent(studentId, startDate)
        
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Calificaciones:", 50f, 160f, paint)
        
        paint.textSize = 14f
        paint.isFakeBoldText = false
        var currentY = 190f
        
        if (grades.isEmpty()) {
            canvas.drawText("No hay calificaciones en este periodo.", 70f, currentY, paint)
            currentY += 25f
        } else {
            for (grade in grades) {
                canvas.drawText("${grade.first}: ${grade.second} (${grade.third})", 70f, currentY, paint)
                currentY += 25f
            }
        }

        currentY += 30f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Asistencia:", 50f, currentY, paint)
        
        currentY += 30f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        
        val attendanceStats = dbHelper.getAttendanceStats(studentId, startDate)
        val present = attendanceStats["present"] ?: 0
        val absent = attendanceStats["absent"] ?: 0
        val late = attendanceStats["late"] ?: 0
        val total = present + absent + late

        if (total == 0) {
            canvas.drawText("No hay registros de asistencia en este periodo.", 70f, currentY, paint)
        } else {
            canvas.drawText("Días asistidos: $present / $total", 70f, currentY, paint)
            currentY += 25f
            canvas.drawText("Inasistencias: $absent", 70f, currentY, paint)
            currentY += 25f
            canvas.drawText("Tardanzas: $late", 70f, currentY, paint)
        }

        pdfDocument.finishPage(page)

        val directory = File(context.filesDir, "reports")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val fileName = "Reporte_${studentName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(directory, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }

        return file
    }
}
