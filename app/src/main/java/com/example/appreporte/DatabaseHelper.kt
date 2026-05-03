package com.example.appreporte

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AppReporte.db"
        private const val DATABASE_VERSION = 8 // Incremented for complaints fix
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

        // Students Table
        private const val TABLE_STUDENTS = "students"
        private const val COLUMN_STUDENT_ID = "id"
        private const val COLUMN_STUDENT_NAMES = "names"
        private const val COLUMN_STUDENT_LASTNAMES = "lastnames"
        private const val COLUMN_STUDENT_DNI = "dni"
        private const val COLUMN_STUDENT_CLASSROOM_ID = "classroom_id"
        private const val COLUMN_STUDENT_PARENT_EMAIL = "parent_email"

        // Grades Table
        private const val TABLE_GRADES = "grades"
        private const val COLUMN_GRADE_ID = "id"
        private const val COLUMN_GRADE_STUDENT_ID = "student_id"
        private const val COLUMN_GRADE_TYPE = "type" // diaria, mensual, bimestral
        private const val COLUMN_GRADE_VALUE = "value"
        private const val COLUMN_GRADE_SUBJECT = "subject"
        private const val COLUMN_GRADE_DATE = "date"

        // Complaints Table
        private const val TABLE_COMPLAINTS = "complaints"
        private const val COLUMN_COMPLAINT_ID = "id"
        private const val COLUMN_COMPLAINT_POST_ID = "post_id"
        private const val COLUMN_COMPLAINT_PARENT_EMAIL = "parent_email"
        private const val COLUMN_COMPLAINT_CONTENT = "content"
        private const val COLUMN_COMPLAINT_STATUS = "status" // en proceso, rechazado, atendido
        private const val COLUMN_COMPLAINT_DATE = "date"
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
                + COLUMN_STUDENT_NAMES + " TEXT,"
                + COLUMN_STUDENT_LASTNAMES + " TEXT,"
                + COLUMN_STUDENT_DNI + " TEXT UNIQUE,"
                + COLUMN_STUDENT_CLASSROOM_ID + " INTEGER,"
                + COLUMN_STUDENT_PARENT_EMAIL + " TEXT,"
                + "FOREIGN KEY($COLUMN_STUDENT_CLASSROOM_ID) REFERENCES $TABLE_CLASSROOMS($COLUMN_CLASSROOM_ID),"
                + "FOREIGN KEY($COLUMN_STUDENT_PARENT_EMAIL) REFERENCES $TABLE_USERS($COLUMN_EMAIL))")
        db.execSQL(createStudentsTable)

        val createGradesTable = ("CREATE TABLE " + TABLE_GRADES + "("
                + COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GRADE_STUDENT_ID + " INTEGER,"
                + COLUMN_GRADE_TYPE + " TEXT,"
                + COLUMN_GRADE_VALUE + " TEXT,"
                + COLUMN_GRADE_SUBJECT + " TEXT,"
                + COLUMN_GRADE_DATE + " TEXT,"
                + "FOREIGN KEY($COLUMN_GRADE_STUDENT_ID) REFERENCES $TABLE_STUDENTS($COLUMN_STUDENT_ID))")
        db.execSQL(createGradesTable)

        val createComplaintsTable = ("CREATE TABLE " + TABLE_COMPLAINTS + "("
                + COLUMN_COMPLAINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_COMPLAINT_POST_ID + " INTEGER,"
                + COLUMN_COMPLAINT_PARENT_EMAIL + " TEXT,"
                + COLUMN_COMPLAINT_CONTENT + " TEXT,"
                + COLUMN_COMPLAINT_STATUS + " TEXT,"
                + COLUMN_COMPLAINT_DATE + " TEXT,"
                + "FOREIGN KEY($COLUMN_COMPLAINT_POST_ID) REFERENCES $TABLE_POSTS($COLUMN_POST_ID),"
                + "FOREIGN KEY($COLUMN_COMPLAINT_PARENT_EMAIL) REFERENCES $TABLE_USERS($COLUMN_EMAIL))")
        db.execSQL(createComplaintsTable)

        // Insertar usuarios iniciales
        insertUser(db, "admin@reporte.com", "admin123", "admin", "")
        insertUser(db, "user@reporte.com", "user123", "usuario", "912345678")
        insertUser(db, "docente@reporte.com", "docente123", "docente", "")

        // Insertar salones iniciales
        insertClassroom(db, "Sala de 3 años")
        insertClassroom(db, "Sala de 4 años")
        insertClassroom(db, "Sala de 5 años")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPLAINTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GRADES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_CLASSROOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSROOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
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

    fun getAllUsers(): List<Map<String, String>> {
        val userList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_EMAIL, $COLUMN_PASSWORD, $COLUMN_ROL, $COLUMN_PHONE FROM $TABLE_USERS", null)
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["email"] = cursor.getString(0)
                map["password"] = cursor.getString(1)
                map["rol"] = cursor.getString(2)
                map["phone"] = cursor.getString(3) ?: ""
                userList.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }

    fun getUserData(email: String): Map<String, String>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?", arrayOf(email))
        var user: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            user = mapOf(
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                "rol" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROL)),
                "phone" to (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: "No registrado")
            )
        }
        cursor.close()
        return user
    }

    fun addUser(email: String, pass: String, rol: String, phone: String = ""): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun updateUser(email: String, pass: String, rol: String, phone: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        return result > 0
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

    fun addStudent(names: String, lastnames: String, dni: String, classroomId: Int, parentEmail: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_STUDENT_NAMES, names)
        values.put(COLUMN_STUDENT_LASTNAMES, lastnames)
        values.put(COLUMN_STUDENT_DNI, dni)
        values.put(COLUMN_STUDENT_CLASSROOM_ID, classroomId)
        values.put(COLUMN_STUDENT_PARENT_EMAIL, parentEmail)
        val result = db.insert(TABLE_STUDENTS, null, values)

        // Vincular automáticamente al padre con el salón para que pueda verlo en el foro
        if (result != -1L && parentEmail.isNotEmpty()) {
            assignUserToClassroom(parentEmail, classroomId)
        }

        return result != -1L
    }

    fun getStudentsByClassroom(classroomId: Int): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS WHERE $COLUMN_STUDENT_CLASSROOM_ID = ?", arrayOf(classroomId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["id"] = cursor.getInt(0).toString()
                map["names"] = cursor.getString(1)
                map["lastnames"] = cursor.getString(2)
                map["dni"] = cursor.getString(3)
                map["parent_email"] = cursor.getString(5)
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateStudent(id: Int, names: String, lastnames: String, dni: String, parentEmail: String, classroomId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_STUDENT_NAMES, names)
        values.put(COLUMN_STUDENT_LASTNAMES, lastnames)
        values.put(COLUMN_STUDENT_DNI, dni)
        values.put(COLUMN_STUDENT_PARENT_EMAIL, parentEmail)
        val result = db.update(TABLE_STUDENTS, values, "$COLUMN_STUDENT_ID = ?", arrayOf(id.toString()))

        // Asegurar que el padre esté vinculado al salón al actualizar los datos
        if (result > 0 && parentEmail.isNotEmpty()) {
            assignUserToClassroom(parentEmail, classroomId)
        }

        return result > 0
    }

    fun deleteStudent(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_STUDENTS, "$COLUMN_STUDENT_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun getParents(): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_EMAIL FROM $TABLE_USERS WHERE $COLUMN_ROL = 'usuario' OR $COLUMN_ROL = 'padre'", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getStudentsByParent(parentEmail: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS WHERE $COLUMN_STUDENT_PARENT_EMAIL = ?", arrayOf(parentEmail))
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["id"] = cursor.getInt(0).toString()
                map["names"] = cursor.getString(1)
                map["lastnames"] = cursor.getString(2)
                map["dni"] = cursor.getString(3)
                map["classroom_id"] = cursor.getInt(4).toString()
                map["parent_email"] = cursor.getString(5)
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateClassroom(id: Int, newName: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CLASSROOM_NAME, newName)
        val result = db.update(TABLE_CLASSROOMS, values, "$COLUMN_CLASSROOM_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    // --- Grades Methods ---

    fun addGrade(studentId: Int, type: String, value: String, subject: String, date: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_GRADE_STUDENT_ID, studentId)
        values.put(COLUMN_GRADE_TYPE, type)
        values.put(COLUMN_GRADE_VALUE, value)
        values.put(COLUMN_GRADE_SUBJECT, subject)
        values.put(COLUMN_GRADE_DATE, date)
        val result = db.insert(TABLE_GRADES, null, values)
        return result != -1L
    }

    fun getGradesByStudent(studentId: Int): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GRADES WHERE $COLUMN_GRADE_STUDENT_ID = ?", arrayOf(studentId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["id"] = cursor.getInt(0).toString()
                map["type"] = cursor.getString(2)
                map["value"] = cursor.getString(3)
                map["subject"] = cursor.getString(4)
                map["date"] = cursor.getString(5)
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getParentPhone(parentEmail: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_PHONE FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?", arrayOf(parentEmail))
        var phone: String? = null
        if (cursor.moveToFirst()) {
            phone = cursor.getString(0)
        }
        cursor.close()
        return phone
    }

    // --- COMPLAINTS METHODS ---

    fun addComplaint(postId: Int, parentEmail: String, content: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_COMPLAINT_POST_ID, postId)
        values.put(COLUMN_COMPLAINT_PARENT_EMAIL, parentEmail)
        values.put(COLUMN_COMPLAINT_CONTENT, content)
        values.put(COLUMN_COMPLAINT_STATUS, "en proceso")
        values.put(COLUMN_COMPLAINT_DATE, SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))
        val result = db.insert(TABLE_COMPLAINTS, null, values)
        return result != -1L
    }

    fun getAllComplaints(salonName: String? = null): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        
        var query = "SELECT c.*, p.$COLUMN_POST_TITLE FROM $TABLE_COMPLAINTS c " +
                "INNER JOIN $TABLE_POSTS p ON c.$COLUMN_COMPLAINT_POST_ID = p.$COLUMN_POST_ID "
        
        val selectionArgs = if (salonName != null) {
            query += "WHERE p.$COLUMN_POST_SALON = ? "
            arrayOf(salonName)
        } else {
            null
        }
        
        query += "ORDER BY c.$COLUMN_COMPLAINT_ID DESC"
        
        val cursor = db.rawQuery(query, selectionArgs)
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["id"] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_ID)).toString()
                map["post_id"] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_POST_ID)).toString()
                map["post_title"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TITLE))
                map["parent_email"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_PARENT_EMAIL))
                map["content"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_CONTENT))
                map["status"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_STATUS))
                map["date"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_DATE))
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateComplaintStatus(complaintId: Int, newStatus: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_COMPLAINT_STATUS, newStatus)
        val result = db.update(TABLE_COMPLAINTS, values, "$COLUMN_COMPLAINT_ID = ?", arrayOf(complaintId.toString()))
        return result > 0
    }
}
