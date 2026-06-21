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
        private const val DATABASE_VERSION = 11
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_ROL = "rol"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_USER_GMAIL = "correo_reportes"

        private const val TABLE_CLASSROOMS = "classrooms"
        private const val COLUMN_CLASSROOM_ID = "id"
        private const val COLUMN_CLASSROOM_NAME = "name"
        private const val COLUMN_CLASSROOM_FIRESTORE_ID = "firestore_id"

        private const val TABLE_USER_CLASSROOMS = "user_classrooms"
        private const val COLUMN_UC_USER_EMAIL = "user_email"
        private const val COLUMN_UC_CLASSROOM_ID = "classroom_id"

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

        private const val TABLE_STUDENTS = "students"
        private const val COLUMN_STUDENT_ID = "id"
        private const val COLUMN_STUDENT_FIRESTORE_ID = "firestore_id"
        private const val COLUMN_STUDENT_NAMES = "names"
        private const val COLUMN_STUDENT_LASTNAMES = "lastnames"
        private const val COLUMN_STUDENT_DNI = "dni"
        private const val COLUMN_STUDENT_CLASSROOM_ID = "classroom_id"
        private const val COLUMN_STUDENT_PARENT_EMAIL = "parent_email"
        private const val COLUMN_STUDENT_GMAIL = "correo"

        private const val TABLE_GRADES = "grades"
        private const val COLUMN_GRADE_ID = "id"
        private const val COLUMN_GRADE_STUDENT_ID = "student_id" // Will store Firestore ID
        private const val COLUMN_GRADE_TYPE = "type"
        private const val COLUMN_GRADE_VALUE = "value"
        private const val COLUMN_GRADE_SUBJECT = "subject"
        private const val COLUMN_GRADE_DATE = "date"
        private const val COLUMN_GRADE_PERIOD = "period"

        private const val TABLE_COMPLAINTS = "complaints"
        private const val COLUMN_COMPLAINT_ID = "id"
        private const val COLUMN_COMPLAINT_POST_ID = "post_id"
        private const val COLUMN_COMPLAINT_PARENT_EMAIL = "parent_email"
        private const val COLUMN_COMPLAINT_CONTENT = "content"
        private const val COLUMN_COMPLAINT_STATUS = "status"
        private const val COLUMN_COMPLAINT_DATE = "date"

        private const val TABLE_ATTENDANCE = "attendance"
        private const val COLUMN_ATTENDANCE_ID = "id"
        private const val COLUMN_ATTENDANCE_STUDENT_ID = "student_id" // Will store Firestore ID
        private const val COLUMN_ATTENDANCE_STATUS = "status"
        private const val COLUMN_ATTENDANCE_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ROL + " TEXT,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_ADDRESS + " TEXT,"
                + COLUMN_USER_GMAIL + " TEXT" + ")")
        db.execSQL(createUsersTable)

        val createClassroomsTable = ("CREATE TABLE " + TABLE_CLASSROOMS + "("
                + COLUMN_CLASSROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CLASSROOM_NAME + " TEXT UNIQUE,"
                + COLUMN_CLASSROOM_FIRESTORE_ID + " TEXT" + ")")
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
                + COLUMN_STUDENT_FIRESTORE_ID + " TEXT UNIQUE,"
                + COLUMN_STUDENT_NAMES + " TEXT,"
                + COLUMN_STUDENT_LASTNAMES + " TEXT,"
                + COLUMN_STUDENT_DNI + " TEXT,"
                + COLUMN_STUDENT_CLASSROOM_ID + " TEXT,"
                + COLUMN_STUDENT_PARENT_EMAIL + " TEXT,"
                + COLUMN_STUDENT_GMAIL + " TEXT,"
                + "FOREIGN KEY($COLUMN_STUDENT_PARENT_EMAIL) REFERENCES $TABLE_USERS($COLUMN_EMAIL))")
        db.execSQL(createStudentsTable)

        val createGradesTable = ("CREATE TABLE " + TABLE_GRADES + "("
                + COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GRADE_STUDENT_ID + " TEXT,"
                + COLUMN_GRADE_TYPE + " TEXT,"
                + COLUMN_GRADE_VALUE + " TEXT,"
                + COLUMN_GRADE_SUBJECT + " TEXT,"
                + COLUMN_GRADE_DATE + " TEXT,"
                + COLUMN_GRADE_PERIOD + " TEXT" + ")")
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

        val createAttendanceTable = ("CREATE TABLE " + TABLE_ATTENDANCE + "("
                + COLUMN_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ATTENDANCE_STUDENT_ID + " TEXT,"
                + COLUMN_ATTENDANCE_STATUS + " TEXT,"
                + COLUMN_ATTENDANCE_DATE + " TEXT" + ")")
        db.execSQL(createAttendanceTable)

        insertUser(db, "admin@reporte.com", "admin123", "admin", "", "")
        insertUser(db, "user@reporte.com", "user123", "usuario", "912345678", "")
        insertUser(db, "docente@reporte.com", "docente123", "docente", "", "")

        insertClassroom(db, "Sala de 3 años")
        insertClassroom(db, "Sala de 4 años")
        insertClassroom(db, "Sala de 5 años")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
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

    fun getAllClassrooms(): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_CLASSROOM_FIRESTORE_ID, $COLUMN_CLASSROOM_NAME FROM $TABLE_CLASSROOMS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Pair(cursor.getString(0) ?: "", cursor.getString(1)))
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

    fun updateClassroom(id: Int, newName: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CLASSROOM_NAME, newName)
        val result = db.update(TABLE_CLASSROOMS, values, "$COLUMN_CLASSROOM_ID = ?", arrayOf(id.toString()))
        return result > 0
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

    fun getUserClassroomsWithNames(email: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        val query = """
            SELECT c.$COLUMN_CLASSROOM_FIRESTORE_ID, c.$COLUMN_CLASSROOM_NAME 
            FROM $TABLE_CLASSROOMS c
            JOIN $TABLE_USER_CLASSROOMS uc ON c.$COLUMN_CLASSROOM_FIRESTORE_ID = uc.$COLUMN_UC_CLASSROOM_ID
            WHERE uc.$COLUMN_UC_USER_EMAIL = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(email))
        if (cursor.moveToFirst()) {
            do {
                list.add(Pair(cursor.getString(0) ?: "", cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    private fun insertUser(db: SQLiteDatabase, email: String, pass: String, rol: String, phone: String, address: String) {
        val values = ContentValues()
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        values.put(COLUMN_ADDRESS, address)
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
        val cursor = db.rawQuery("SELECT $COLUMN_EMAIL, $COLUMN_PASSWORD, $COLUMN_ROL, $COLUMN_PHONE, $COLUMN_ADDRESS, $COLUMN_USER_GMAIL FROM $TABLE_USERS", null)
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["email"] = cursor.getString(0)
                map["password"] = cursor.getString(1)
                map["rol"] = cursor.getString(2)
                map["phone"] = cursor.getString(3) ?: ""
                map["address"] = cursor.getString(4) ?: ""
                map["correo_reportes"] = cursor.getString(5) ?: ""
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
                "phone" to (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: "No registrado"),
                "address" to (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)) ?: "No registrado"),
                "correo_reportes" to (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_GMAIL)) ?: "")
            )
        }
        cursor.close()
        return user
    }

    fun addUser(email: String, pass: String, rol: String, phone: String = "", address: String = ""): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        values.put(COLUMN_ADDRESS, address)
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun updateUser(email: String, pass: String, rol: String, phone: String, address: String = ""): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        values.put(COLUMN_ADDRESS, address)
        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        return result > 0
    }

    fun syncUserProfile(email: String, rol: String, phone: String, address: String, gmail: String = ""): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ROL, rol)
        values.put(COLUMN_PHONE, phone)
        values.put(COLUMN_ADDRESS, address)
        values.put(COLUMN_USER_GMAIL, gmail)
        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        return result > 0
    }

    fun updateUserAddress(email: String, address: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ADDRESS, address)
        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        return result > 0
    }

    fun updateUserGmail(email: String, gmail: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_GMAIL, gmail)
        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        return result > 0
    }

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

    fun addStudent(firestoreId: String, names: String, lastnames: String, dni: String, classroomId: String, parentEmail: String, gmail: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_STUDENT_FIRESTORE_ID, firestoreId)
        values.put(COLUMN_STUDENT_NAMES, names)
        values.put(COLUMN_STUDENT_LASTNAMES, lastnames)
        values.put(COLUMN_STUDENT_DNI, dni)
        values.put(COLUMN_STUDENT_CLASSROOM_ID, classroomId)
        values.put(COLUMN_STUDENT_PARENT_EMAIL, parentEmail)
        values.put(COLUMN_STUDENT_GMAIL, gmail)
        val result = db.insertWithOnConflict(TABLE_STUDENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }

    fun getStudentsByClassroom(classroomId: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_STUDENT_FIRESTORE_ID, $COLUMN_STUDENT_NAMES, $COLUMN_STUDENT_LASTNAMES, $COLUMN_STUDENT_DNI, $COLUMN_STUDENT_PARENT_EMAIL, $COLUMN_STUDENT_GMAIL FROM $TABLE_STUDENTS WHERE $COLUMN_STUDENT_CLASSROOM_ID = ?", arrayOf(classroomId))
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["id"] = cursor.getString(0)
                map["names"] = cursor.getString(1)
                map["lastnames"] = cursor.getString(2)
                map["dni"] = cursor.getString(3)
                map["parent_email"] = cursor.getString(4) ?: ""
                map["correo"] = cursor.getString(5) ?: ""
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateStudent(firestoreId: String, names: String, lastnames: String, dni: String, parentEmail: String, gmail: String, classroomId: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_STUDENT_NAMES, names)
        values.put(COLUMN_STUDENT_LASTNAMES, lastnames)
        values.put(COLUMN_STUDENT_DNI, dni)
        values.put(COLUMN_STUDENT_PARENT_EMAIL, parentEmail)
        values.put(COLUMN_STUDENT_GMAIL, gmail)
        values.put(COLUMN_STUDENT_CLASSROOM_ID, classroomId)
        val result = db.update(TABLE_STUDENTS, values, "$COLUMN_STUDENT_FIRESTORE_ID = ?", arrayOf(firestoreId))
        return result > 0
    }

    fun deleteStudent(firestoreId: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_STUDENTS, "$COLUMN_STUDENT_FIRESTORE_ID = ?", arrayOf(firestoreId))
        return result > 0
    }

    fun getStudentById(firestoreId: String): Map<String, String> {
        val db = this.readableDatabase
        val map = mutableMapOf<String, String>()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS WHERE $COLUMN_STUDENT_FIRESTORE_ID = ?", arrayOf(firestoreId))
        if (cursor.moveToFirst()) {
            map["id"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_FIRESTORE_ID))
            map["names"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_NAMES))
            map["lastnames"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_LASTNAMES))
            map["dni"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_DNI))
            map["classroom_id"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_CLASSROOM_ID))
            map["parent_email"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_PARENT_EMAIL))
            map["correo"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_GMAIL)) ?: ""
            map["firestore_id"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_FIRESTORE_ID))
        }
        cursor.close()
        return map
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
                map["id"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_FIRESTORE_ID))
                map["names"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_NAMES))
                map["lastnames"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_LASTNAMES))
                map["dni"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_DNI))
                map["classroom_id"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_CLASSROOM_ID))
                map["parent_email"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_PARENT_EMAIL))
                map["correo"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_GMAIL)) ?: ""
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addGrade(studentId: String, subject: String, value: String, date: String, type: String, period: String = ""): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_GRADE_STUDENT_ID, studentId)
        values.put(COLUMN_GRADE_TYPE, type)
        values.put(COLUMN_GRADE_VALUE, value)
        values.put(COLUMN_GRADE_SUBJECT, subject)
        values.put(COLUMN_GRADE_DATE, date)
        values.put(COLUMN_GRADE_PERIOD, period)
        val result = db.insert(TABLE_GRADES, null, values)
        return result != -1L
    }

    fun getGradesByStudent(studentId: String, startDate: String? = null): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        val db = this.readableDatabase
        val query = if (startDate != null) {
            "SELECT $COLUMN_GRADE_SUBJECT, $COLUMN_GRADE_VALUE, $COLUMN_GRADE_DATE FROM $TABLE_GRADES WHERE $COLUMN_GRADE_STUDENT_ID = ? AND $COLUMN_GRADE_DATE >= ?"
        } else {
            "SELECT $COLUMN_GRADE_SUBJECT, $COLUMN_GRADE_VALUE, $COLUMN_GRADE_DATE FROM $TABLE_GRADES WHERE $COLUMN_GRADE_STUDENT_ID = ?"
        }
        val args = if (startDate != null) arrayOf(studentId, startDate) else arrayOf(studentId)
        
        val cursor = db.rawQuery(query, args)
        if (cursor.moveToFirst()) {
            do {
                list.add(Triple(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
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

    fun addAttendance(studentId: String, status: String, date: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ATTENDANCE_STUDENT_ID, studentId)
        values.put(COLUMN_ATTENDANCE_STATUS, status)
        values.put(COLUMN_ATTENDANCE_DATE, date)
        val result = db.insert(TABLE_ATTENDANCE, null, values)
        return result != -1L
    }

    fun getAttendanceStats(studentId: String, startDate: String): Map<String, Int> {
        val stats = mutableMapOf("present" to 0, "absent" to 0, "late" to 0)
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_ATTENDANCE_STATUS, COUNT(*) FROM $TABLE_ATTENDANCE WHERE $COLUMN_ATTENDANCE_STUDENT_ID = ? AND $COLUMN_ATTENDANCE_DATE >= ? GROUP BY $COLUMN_ATTENDANCE_STATUS"
        val cursor = db.rawQuery(query, arrayOf(studentId, startDate))
        if (cursor.moveToFirst()) {
            do {
                val status = cursor.getString(0)
                val count = cursor.getInt(1)
                stats[status] = count
            } while (cursor.moveToNext())
        }
        cursor.close()
        return stats
    }

    // --- Offline Wrapper Methods ---

    fun getOfflineStudents(parentEmail: String): List<Map<String, String>> {
        return getStudentsByParent(parentEmail)
    }

    fun getOfflineGrades(studentId: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_GRADE_SUBJECT, $COLUMN_GRADE_VALUE, $COLUMN_GRADE_DATE FROM $TABLE_GRADES WHERE $COLUMN_GRADE_STUDENT_ID = ?", arrayOf(studentId))
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["subject"] = cursor.getString(0)
                map["value"] = cursor.getString(1)
                map["date"] = cursor.getString(2)
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getOfflineAttendance(studentId: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ATTENDANCE_STATUS, $COLUMN_ATTENDANCE_DATE FROM $TABLE_ATTENDANCE WHERE $COLUMN_ATTENDANCE_STUDENT_ID = ?", arrayOf(studentId))
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["status"] = cursor.getString(0)
                map["date"] = cursor.getString(1)
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun saveStudent(firestoreId: String, name: String, classroomId: String, parentEmail: String): Boolean {
        // Simple version for quick sync
        return addStudent(firestoreId, name, "", "", classroomId, parentEmail, "")
    }

    fun saveGrade(studentId: String, subject: String, value: String, type: String, date: String, period: String = ""): Boolean {
        return addGrade(studentId, subject, value, date, type, period)
    }

    fun saveAttendance(studentId: String, status: String, date: String): Boolean {
        return addAttendance(studentId, status, date)
    }

    fun saveUser(email: String, pass: String, rol: String, phone: String = "", address: String = ""): Boolean {
        return addUser(email, pass, rol, phone, address)
    }
}
