package com.example.appreporte.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.appreporte.data.local.DatabaseHelper
import com.example.appreporte.utils.ReportGenerator

class ReportWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val dbHelper = DatabaseHelper(applicationContext)
        val reportGenerator = ReportGenerator(applicationContext)
        val period = inputData.getString("PERIOD") ?: "Diario"

        val classrooms = dbHelper.getAllClassrooms()
        for (classroom in classrooms) {
            val students = dbHelper.getStudentsByClassroom(classroom.first)
            for (student in students) {
                val studentId = student["id"]?.toInt() ?: -1
                val studentName = "${student["names"]} ${student["lastnames"]}"
                val parentEmail = student["parent_email"] ?: ""
                val phone = dbHelper.getParentPhone(parentEmail)

                if (studentId != -1 && !phone.isNullOrEmpty()) {
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
