package com.example.appreporte

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AppReporte.db"
        private const val DATABASE_VERSION = 7 // Incremented version to force migration
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_ROL = "rol"
        private const val COLUMN_PHONE = "phone"

        private const val TABLE_CLASSROOMS = "classrooms"
        private const val COLUMN_CLASSROOM_ID = "id"
        private const val COLUMN_CLASSROOM_NAME = "name"

        private const val TABLE_USER_CLASSROOMS = "user_classrooms"
        private const val COLUMN_UC_USER_EMAIL = "user_email"
        private const val COLUMN_UC_CLASSROOM_ID = "classroom_id"

        private const val TABLE_STUDENTS = "students"
        private const val COLUMN_STUDENT_ID = "id"
        private const val COLUMN_STUDENT_NAME = "name"
        private const val COLUMN_STUDENT_CLASSROOM_ID = "classroom_id"
        private const val COLUMN_STUDENT_PARENT_EMAIL = "parent_email"

        // Academic Tables
        private const val TABLE_GRADES = "grades"
        private const val COLUMN_GRADE_ID = "id"
        private const val COLUMN_GRADE_STUDENT_ID = "student_id"
        private const val COLUMN_GRADE_SUBJECT = "subject"
        private const val COLUMN_GRADE_VALUE = "grade"
        private const val COLUMN_GRADE_DATE = "date"
        private const val COLUMN_GRADE_PERIOD = "period" // daily, monthly, bimonthly

        private const val TABLE_ATTENDANCE = "attendance"
        private const val COLUMN_ATT_ID = "id"
        private const val COLUMN_ATT_STUDENT_ID = "student_id"
        private const val COLUMN_ATT_STATUS = "status" // present, absent, late
        private const val COLUMN_ATT_DATE = "date"

        // Forum Tables
        private const val TABLE_POSTS = "posts"
        private const val COLUMN_POST_ID = "id"
        private const val COLUMN_POST_SALON = "salon_name"
        private const val COLUMN_POST_AUTHOR = "author"
        private const val COLUMN_POST_TITLE = "title"
        private const val COLUMN_POST_CONTENT = "content"
        private const val COLUMN_POST_TIME = "time"

        private const val TABLE_COMMENTS = "comments"
        private const val COLUMN_COMMENT_ID = "id"
        private const val COLUMN_COMMENT_POST_ID = "post_id"
        private const val COLUMN_COMMENT_AUTHOR = "author"
        private const val COLUMN_COMMENT_CONTENT = "content"
        private const val COLUMN_COMMENT_TIME = "time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ROL + " TEXT,"
                + COLUMN_PHONE + " TEXT" + ")")
        db.execSQL(createUsersTable)

        val createClassroomsTable = ("CREATE TABLE " + TABLE_CLASSROOMS + "("
                + COLUMN_CLASSROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CLASSROOM_NAME + " TEXT UNIQUE" + ")")
        db.execSQL(createClassroomsTable)

        val createUserClassroomsTable = ("CREATE TABLE " + TABLE_USER_CLASSROOMS + "("
                + COLUMN_UC_USER_EMAIL + " TEXT,"
                + COLUMN_UC_CLASSROOM_ID + " INTEGER,"
                + "PRIMARY KEY ($COLUMN_UC_USER_EMAIL, $COLUMN_UC_CLASSROOM_ID),"
                + "FOREIGN KEY($COLUMN_UC_USER_EMAIL) REFERENCES $TABLE_USERS($COLUMN_EMAIL),"
                + "FOREIGN KEY($COLUMN_UC_CLASSROOM_ID) REFERENCES $TABLE_CLASSROOMS($COLUMN_CLASSROOM_ID))")
        db.execSQL(createUserClassroomsTable)

        val createPostsTable = ("CREATE TABLE " + TABLE_POSTS + "("
                + COLUMN_POST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_POST_SALON + " TEXT,"
                + COLUMN_POST_AUTHOR + " TEXT,"
                + COLUMN_POST_TITLE + " TEXT,"
                + COLUMN_POST_CONTENT + " TEXT,"
                + COLUMN_POST_TIME + " TEXT" + ")")
        db.execSQL(createPostsTable)

        val createCommentsTable = ("CREATE TABLE " + TABLE_COMMENTS + "("
                + COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_COMMENT_POST_ID + " INTEGER,"
                + COLUMN_COMMENT_AUTHOR + " TEXT,"
                + COLUMN_COMMENT_CONTENT + " TEXT,"
                + COLUMN_COMMENT_TIME + " TEXT,"
                + "FOREIGN KEY($COLUMN_COMMENT_POST_ID) REFERENCES $TABLE_POSTS($COLUMN_POST_ID))")
        db.execSQL(createCommentsTable)

        val createStudentsTable = ("CREATE TABLE " + TABLE_STUDENTS + "("
                + COLUMN_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STUDENT_NAME + " TEXT,"
                + COLUMN_STUDENT_CLASSROOM_ID + " INTEGER,"
                + COLUMN_STUDENT_PARENT_EMAIL + " TEXT,"
                + "FOREIGN KEY($COLUMN_STUDENT_CLASSROOM_ID) REFERENCES $TABLE_CLASSROOMS($COLUMN_CLASSROOM_ID),"
                + "FOREIGN KEY($COLUMN_STUDENT_PARENT_EMAIL) REFERENCES $TABLE_USERS($COLUMN_EMAIL))")
        db.execSQL(createStudentsTable)

        val createGradesTable = ("CREATE TABLE " + TABLE_GRADES + "("
                + COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GRADE_STUDENT_ID + " INTEGER,"
                + COLUMN_GRADE_SUBJECT + " TEXT,"
                + COLUMN_GRADE_VALUE + " REAL,"
                + COLUMN_GRADE_DATE + " TEXT,"
                + COLUMN_GRADE_PERIOD + " TEXT,"
                + "FOREIGN KEY($COLUMN_GRADE_STUDENT_ID) REFERENCES $TABLE_STUDENTS($COLUMN_STUDENT_ID))")
        db.execSQL(createGradesTable)

        val createAttendanceTable = ("CREATE TABLE " + TABLE_ATTENDANCE + "("
                + COLUMN_ATT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ATT_STUDENT_ID + " INTEGER,"
                + COLUMN_ATT_STATUS + " TEXT,"
                + COLUMN_ATT_DATE + " TEXT,"
                + "FOREIGN KEY($COLUMN_ATT_STUDENT_ID) REFERENCES $TABLE_STUDENTS($COLUMN_STUDENT_ID))")
        db.execSQL(createAttendanceTable)

        // Insertar usuarios iniciales
        insertUser(db, "admin@reporte.com", "admin123", "admin", "+51987654321")
        insertUser(db, "user@reporte.com", "user123", "usuario", "+51999888777")
        insertUser(db, "docente@reporte.com", "docente123", "docente", "+51911222333")

        // Insertar salones iniciales
        insertClassroom(db, "Sala de 3 años")
        insertClassroom(db, "Sala de 4 años")
        insertClassroom(db, "Sala de 5 años")

        // Asignar salones al docente por defecto para pruebas
        assignUserToClassroom(db, "docente@reporte.com", 1)
        assignUserToClassroom(db, "docente@reporte.com", 2)
        assignUserToClassroom(db, "docente@reporte.com", 3)
    }

    private fun assignUserToClassroom(db: SQLiteDatabase, email: String, classroomId: Int) {
        val values = ContentValues()
        values.put(COLUMN_UC_USER_EMAIL, email)
        values.put(COLUMN_UC_CLASSROOM_ID, classroomId)
        db.insertWithOnConflict(TABLE_USER_CLASSROOMS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Disable foreign keys to avoid issues during drop
        db.execSQL("PRAGMA foreign_keys = OFF;")
        
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GRADES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_CLASSROOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSROOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        
        db.execSQL("PRAGMA foreign_keys = ON;")
        onCreate(db)
    }

    private fun insertClassroom(db: SQLiteDatabase, name: String) {
        val values = ContentValues()
        values.put(COLUMN_CLASSROOM_NAME, name)
        db.insert(TABLE_CLASSROOMS, null, values)
    }

    fun getAllClassrooms(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_CLASSROOM_ID, $COLUMN_CLASSROOM_NAME FROM $TABLE_CLASSROOMS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Pair(cursor.getInt(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addClassroom(name: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CLASSROOM_NAME, name)
        val result = db.insert(TABLE_CLASSROOMS, null, values)
        return result != -1L
    }

    fun deleteClassroom(id: Int): Boolean {
        val db = this.writableDatabase
        db.delete(TABLE_USER_CLASSROOMS, "$COLUMN_UC_CLASSROOM_ID = ?", arrayOf(id.toString()))
        val result = db.delete(TABLE_CLASSROOMS, "$COLUMN_CLASSROOM_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun assignUserToClassroom(email: String, classroomId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_UC_USER_EMAIL, email)
        values.put(COLUMN_UC_CLASSROOM_ID, classroomId)
        val result = db.insertWithOnConflict(TABLE_USER_CLASSROOMS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        return result != -1L
    }

    fun removeUserFromClassroom(email: String, classroomId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_USER_CLASSROOMS, "$COLUMN_UC_USER_EMAIL = ? AND $COLUMN_UC_CLASSROOM_ID = ?", arrayOf(email, classroomId.toString()))
        return result > 0
    }

    fun getUserClassrooms(email: String): List<Int> {
        val list = mutableListOf<Int>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_UC_CLASSROOM_ID FROM $TABLE_USER_CLASSROOMS WHERE $COLUMN_UC_USER_EMAIL = ?", arrayOf(email))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getInt(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUserClassroomsWithNames(email: String): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val db = this.readableDatabase
        val query = """
            SELECT c.$COLUMN_CLASSROOM_ID, c.$COLUMN_CLASSROOM_NAME 
            FROM $TABLE_CLASSROOMS c
            JOIN $TABLE_USER_CLASSROOMS uc ON c.$COLUMN_CLASSROOM_ID = uc.$COLUMN_UC_CLASSROOM_ID
            WHERE uc.$COLUMN_UC_USER_EMAIL = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(email))
        if (cursor.moveToFirst()) {
            do {
                list.add(Pair(cursor.getInt(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // User data class
    data class User(val email: String, val pass: String, val rol: String, val phone: String)

    private fun insertUser(db: SQLiteDatabase, email: String, pass: String, rol: String, phone: String) {
        val values = ContentValues()
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        db.insert(TABLE_USERS, null, values)
    }

    fun checkUser(email: String, pass: String): String? {
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_ROL)
        val selection = "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(email, pass)
        val cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)
        var role: String? = null
        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }
        cursor.close()
        return role
    }

    fun getAllUsers(): List<User> {
        val userList = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_EMAIL, $COLUMN_PASSWORD, $COLUMN_ROL, $COLUMN_PHONE FROM $TABLE_USERS", null)
        if (cursor.moveToFirst()) {
            do {
                userList.add(User(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3) ?: ""))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }

    fun addUser(email: String, pass: String, rol: String, phone: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    // --- Forum Methods ---

    fun addPost(salon: String, author: String, title: String, content: String, time: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_POST_SALON, salon)
        values.put(COLUMN_POST_AUTHOR, author)
        values.put(COLUMN_POST_TITLE, title)
        values.put(COLUMN_POST_CONTENT, content)
        values.put(COLUMN_POST_TIME, time)
        return db.insert(TABLE_POSTS, null, values)
    }

    fun getPostsBySalon(salon: String): List<Triple<Int, Triple<String, String, String>, String>> {
        val list = mutableListOf<Triple<Int, Triple<String, String, String>, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_POSTS WHERE $COLUMN_POST_SALON = ? ORDER BY $COLUMN_POST_ID DESC", arrayOf(salon))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val author = cursor.getString(2)
                val title = cursor.getString(3)
                val content = cursor.getString(4)
                val time = cursor.getString(5)
                list.add(Triple(id, Triple(author, title, content), time))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addComment(postId: Int, author: String, content: String, time: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_COMMENT_POST_ID, postId)
        values.put(COLUMN_COMMENT_AUTHOR, author)
        values.put(COLUMN_COMMENT_CONTENT, content)
        values.put(COLUMN_COMMENT_TIME, time)
        val result = db.insert(TABLE_COMMENTS, null, values)
        return result != -1L
    }

    fun getCommentsByPost(postId: Int): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_COMMENT_AUTHOR, $COLUMN_COMMENT_CONTENT, $COLUMN_COMMENT_TIME FROM $TABLE_COMMENTS WHERE $COLUMN_COMMENT_POST_ID = ?", arrayOf(postId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(Triple(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- Student Methods ---

    fun addStudent(name: String, classroomId: Int, parentEmail: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_STUDENT_NAME, name)
        values.put(COLUMN_STUDENT_CLASSROOM_ID, classroomId)
        values.put(COLUMN_STUDENT_PARENT_EMAIL, parentEmail)
        val result = db.insert(TABLE_STUDENTS, null, values)
        return result != -1L
    }

    fun getStudentsByClassroom(classroomId: Int): List<Triple<Int, String, String>> {
        val list = mutableListOf<Triple<Int, String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_STUDENT_ID, $COLUMN_STUDENT_NAME, $COLUMN_STUDENT_PARENT_EMAIL FROM $TABLE_STUDENTS WHERE $COLUMN_STUDENT_CLASSROOM_ID = ?", arrayOf(classroomId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(Triple(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteStudent(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_STUDENTS, "$COLUMN_STUDENT_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun getParents(): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_EMAIL FROM $TABLE_USERS WHERE $COLUMN_ROL = 'usuario'", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getParentPhone(email: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_PHONE FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?", arrayOf(email))
        var phone = ""
        if (cursor.moveToFirst()) {
            phone = cursor.getString(0) ?: ""
        }
        cursor.close()
        return phone
    }

    // --- Grades & Attendance Methods ---

    fun addGrade(studentId: Int, subject: String, grade: Double, date: String, period: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_GRADE_STUDENT_ID, studentId)
        values.put(COLUMN_GRADE_SUBJECT, subject)
        values.put(COLUMN_GRADE_VALUE, grade)
        values.put(COLUMN_GRADE_DATE, date)
        values.put(COLUMN_GRADE_PERIOD, period)
        val result = db.insert(TABLE_GRADES, null, values)
        return result != -1L
    }

    fun getGradesByStudent(studentId: Int, startDate: String? = null): List<Triple<String, Double, String>> {
        val list = mutableListOf<Triple<String, Double, String>>()
        val db = this.readableDatabase
        val query = if (startDate != null) {
            "SELECT $COLUMN_GRADE_SUBJECT, $COLUMN_GRADE_VALUE, $COLUMN_GRADE_DATE FROM $TABLE_GRADES WHERE $COLUMN_GRADE_STUDENT_ID = ? AND $COLUMN_GRADE_DATE >= ?"
        } else {
            "SELECT $COLUMN_GRADE_SUBJECT, $COLUMN_GRADE_VALUE, $COLUMN_GRADE_DATE FROM $TABLE_GRADES WHERE $COLUMN_GRADE_STUDENT_ID = ?"
        }
        val args = if (startDate != null) arrayOf(studentId.toString(), startDate) else arrayOf(studentId.toString())
        
        val cursor = db.rawQuery(query, args)
        if (cursor.moveToFirst()) {
            do {
                list.add(Triple(cursor.getString(0), cursor.getDouble(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addAttendance(studentId: Int, status: String, date: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ATT_STUDENT_ID, studentId)
        values.put(COLUMN_ATT_STATUS, status)
        values.put(COLUMN_ATT_DATE, date)
        val result = db.insert(TABLE_ATTENDANCE, null, values)
        return result != -1L
    }

    fun getAttendanceStats(studentId: Int, startDate: String? = null): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        val db = this.readableDatabase
        val query = if (startDate != null) {
            "SELECT $COLUMN_ATT_STATUS, COUNT(*) FROM $TABLE_ATTENDANCE WHERE $COLUMN_ATT_STUDENT_ID = ? AND $COLUMN_ATT_DATE >= ? GROUP BY $COLUMN_ATT_STATUS"
        } else {
            "SELECT $COLUMN_ATT_STATUS, COUNT(*) FROM $TABLE_ATTENDANCE WHERE $COLUMN_ATT_STUDENT_ID = ? GROUP BY $COLUMN_ATT_STATUS"
        }
        val args = if (startDate != null) arrayOf(studentId.toString(), startDate) else arrayOf(studentId.toString())

        val cursor = db.rawQuery(query, args)
        if (cursor.moveToFirst()) {
            do {
                stats[cursor.getString(0)] = cursor.getInt(1)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return stats
    }
}
