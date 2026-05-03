package com.example.appreporte

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReportWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val dbHelper = DatabaseHelper(applicationContext)
        val reportGenerator = ReportGenerator(applicationContext)
        val period = inputData.getString("PERIOD") ?: "Diario"

        val classrooms = dbHelper.getAllClassrooms()
        for (classroom in classrooms) {
            val students = dbHelper.getStudentsByClassroom(classroom.first)
            for (student in students) {
                val studentId = student.first
                val studentName = student.second
                val parentEmail = student.third
                val phone = dbHelper.getParentPhone(parentEmail)

                if (phone.isNotEmpty()) {
                    val reportFile = reportGenerator.generateStudentReport(studentId, studentName, period)
                    // In a production environment with WhatsApp Business API, the sending would happen here.
                    // For this project, the report is generated and saved in the internal storage.
                    println("Automated Report ($period) generated for $studentName to be sent to $phone. File: ${reportFile?.absolutePath}")
                }
            }
        }

        return Result.success()
    }
}
