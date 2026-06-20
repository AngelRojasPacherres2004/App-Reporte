package com.example.appreporte

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AppReporteBackup.db"
        private const val DATABASE_VERSION = 1

        // Tabla Usuarios
        const val TABLE_USERS = "users"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_ROL = "rol"
        const val COL_USER_SCHOOL = "school_id"

        // Tabla Alumnos
        const val TABLE_STUDENTS = "students"
        const val COL_STUDENT_ID = "id"
        const val COL_STUDENT_NAME = "name"
        const val COL_STUDENT_CLASSROOM = "classroom_id"
        const val COL_STUDENT_PARENT_EMAIL = "parent_email"

        // Tabla Notas
        const val TABLE_GRADES = "grades"
        const val COL_GRADE_ID = "id"
        const val COL_GRADE_STUDENT_ID = "student_id"
        const val COL_GRADE_SUBJECT = "subject"
        const val COL_GRADE_VALUE = "value"
        const val COL_GRADE_TYPE = "type"
        const val COL_GRADE_DATE = "date"
        const val COL_GRADE_PERIOD = "period"

        // Tabla Asistencia
        const val TABLE_ATTENDANCE = "attendance"
        const val COL_ATTENDANCE_ID = "id"
        const val COL_ATTENDANCE_STUDENT_ID = "student_id"
        const val COL_ATTENDANCE_DATE = "date"
        const val COL_ATTENDANCE_STATUS = "status"
        const val COL_ATTENDANCE_COURSE = "course_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsers = ("CREATE TABLE $TABLE_USERS (" +
                "$COL_USER_EMAIL TEXT PRIMARY KEY, " +
                "$COL_USER_ROL TEXT, " +
                "$COL_USER_SCHOOL TEXT)")
        db.execSQL(createUsers)

        val createStudents = ("CREATE TABLE $TABLE_STUDENTS (" +
                "$COL_STUDENT_ID TEXT PRIMARY KEY, " +
                "$COL_STUDENT_NAME TEXT, " +
                "$COL_STUDENT_CLASSROOM TEXT, " +
                "$COL_STUDENT_PARENT_EMAIL TEXT)")
        db.execSQL(createStudents)

        val createGrades = ("CREATE TABLE $TABLE_GRADES (" +
                "$COL_GRADE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_GRADE_STUDENT_ID TEXT, " +
                "$COL_GRADE_SUBJECT TEXT, " +
                "$COL_GRADE_VALUE TEXT, " +
                "$COL_GRADE_TYPE TEXT, " +
                "$COL_GRADE_PERIOD TEXT, " +
                "$COL_GRADE_DATE TEXT)")
        db.execSQL(createGrades)

        val createAttendance = ("CREATE TABLE $TABLE_ATTENDANCE (" +
                "$COL_ATTENDANCE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_ATTENDANCE_STUDENT_ID TEXT, " +
                "$COL_ATTENDANCE_DATE TEXT, " +
                "$COL_ATTENDANCE_STATUS TEXT, " +
                "$COL_ATTENDANCE_COURSE TEXT)")
        db.execSQL(createAttendance)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GRADES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
        onCreate(db)
    }

    // --- Métodos de Ayuda ---

    fun saveUser(email: String, rol: String, schoolId: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_EMAIL, email)
            put(COL_USER_ROL, rol)
            put(COL_USER_SCHOOL, schoolId)
        }
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun saveGrade(studentId: String, subject: String, value: String, type: String, date: String, period: String = "") {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_GRADE_STUDENT_ID, studentId)
            put(COL_GRADE_SUBJECT, subject)
            put(COL_GRADE_VALUE, value)
            put(COL_GRADE_TYPE, type)
            put(COL_GRADE_DATE, date)
            put(COL_GRADE_PERIOD, period)
        }
        db.insert(TABLE_GRADES, null, values)
        db.close()
    }

    fun saveAttendance(studentId: String, date: String, status: String, course: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ATTENDANCE_STUDENT_ID, studentId)
            put(COL_ATTENDANCE_DATE, date)
            put(COL_ATTENDANCE_STATUS, status)
            put(COL_ATTENDANCE_COURSE, course)
        }
        db.insert(TABLE_ATTENDANCE, null, values)
        db.close()
    }

    fun saveStudent(id: String, name: String, classroomId: String, parentEmail: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_STUDENT_ID, id)
            put(COL_STUDENT_NAME, name)
            put(COL_STUDENT_CLASSROOM, classroomId)
            put(COL_STUDENT_PARENT_EMAIL, parentEmail)
        }
        db.insertWithOnConflict(TABLE_STUDENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getOfflineStudents(parentEmail: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS WHERE $COL_STUDENT_PARENT_EMAIL = ?", arrayOf(parentEmail))
        if (cursor.moveToFirst()) {
            do {
                val item = mapOf(
                    "id" to cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_ID)),
                    "names" to cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_NAME)), // Normalizado a 'names' para el adapter
                    "classroom_id" to cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_CLASSROOM)),
                    "parent_email" to cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_PARENT_EMAIL))
                )
                list.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getOfflineGrades(studentId: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GRADES WHERE $COL_GRADE_STUDENT_ID = ?", arrayOf(studentId))
        if (cursor.moveToFirst()) {
            do {
                val item = mapOf(
                    "subject" to cursor.getString(cursor.getColumnIndexOrThrow(COL_GRADE_SUBJECT)),
                    "value" to cursor.getString(cursor.getColumnIndexOrThrow(COL_GRADE_VALUE)),
                    "type" to cursor.getString(cursor.getColumnIndexOrThrow(COL_GRADE_TYPE)),
                    "date" to cursor.getString(cursor.getColumnIndexOrThrow(COL_GRADE_DATE)),
                    "period" to cursor.getString(cursor.getColumnIndexOrThrow(COL_GRADE_PERIOD))
                )
                list.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getOfflineAttendance(studentId: String): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ATTENDANCE WHERE $COL_ATTENDANCE_STUDENT_ID = ? ORDER BY $COL_ATTENDANCE_DATE DESC", arrayOf(studentId))
        if (cursor.moveToFirst()) {
            do {
                val item = mapOf(
                    "student_id" to cursor.getString(cursor.getColumnIndexOrThrow(COL_ATTENDANCE_STUDENT_ID)),
                    "date" to cursor.getString(cursor.getColumnIndexOrThrow(COL_ATTENDANCE_DATE)),
                    "status" to cursor.getString(cursor.getColumnIndexOrThrow(COL_ATTENDANCE_STATUS)),
                    "course_name" to cursor.getString(cursor.getColumnIndexOrThrow(COL_ATTENDANCE_COURSE))
                )
                list.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
