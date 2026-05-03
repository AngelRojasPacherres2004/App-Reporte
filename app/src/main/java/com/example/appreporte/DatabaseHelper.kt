package com.example.appreporte

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AppReporte.db"
        private const val DATABASE_VERSION = 5 // Incrementado para añadir Quejas

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_ROL = "rol"

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

        // Quejas Tables (RF-06)
        private const val TABLE_QUEJAS = "quejas"
        private const val COLUMN_QUEJA_ID = "id"
        private const val COLUMN_QUEJA_EMAIL = "user_email"
        private const val COLUMN_QUEJA_MENSAJE = "mensaje"
        private const val COLUMN_QUEJA_CLASIFICACION = "clasificacion"
        private const val COLUMN_QUEJA_ESTADO = "estado"
        private const val COLUMN_QUEJA_FECHA = "fecha"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ROL + " TEXT" + ")")
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

        // Crear Tabla de Quejas
        val createQuejasTable = ("CREATE TABLE " + TABLE_QUEJAS + "("
                + COLUMN_QUEJA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_QUEJA_EMAIL + " TEXT,"
                + COLUMN_QUEJA_MENSAJE + " TEXT,"
                + COLUMN_QUEJA_CLASIFICACION + " TEXT,"
                + COLUMN_QUEJA_ESTADO + " TEXT,"
                + COLUMN_QUEJA_FECHA + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")")
        db.execSQL(createQuejasTable)

        // Insertar usuarios iniciales
        insertUser(db, "admin@reporte.com", "admin123", "admin")
        insertUser(db, "user@reporte.com", "user123", "usuario")
        insertUser(db, "docente@reporte.com", "docente123", "docente")

        // Insertar salones iniciales
        insertClassroom(db, "Sala de 3 años")
        insertClassroom(db, "Sala de 4 años")
        insertClassroom(db, "Sala de 5 años")

        // Insertar una queja de prueba para que veas el diseño al instante
        insertQuejaPrueba(db, "user@reporte.com", "Falta de ventilación en el aula 3.", "Infraestructura", "Pendiente")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUEJAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_CLASSROOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSROOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // --- Métodos originales de Users y Classrooms ---
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

    private fun insertUser(db: SQLiteDatabase, email: String, pass: String, rol: String) {
        val values = ContentValues()
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
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

    fun getAllUsers(): List<Triple<String, String, String>> {
        val userList = mutableListOf<Triple<String, String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_EMAIL, $COLUMN_PASSWORD, $COLUMN_ROL FROM $TABLE_USERS", null)
        if (cursor.moveToFirst()) {
            do {
                userList.add(Triple(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }

    fun addUser(email: String, pass: String, rol: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_ROL, rol)
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

    // --- NUEVOS MÉTODOS PARA QUEJAS (RF-06) ---
    private fun insertQuejaPrueba(db: SQLiteDatabase, email: String, mensaje: String, clasificacion: String, estado: String) {
        val values = ContentValues()
        values.put(COLUMN_QUEJA_EMAIL, email)
        values.put(COLUMN_QUEJA_MENSAJE, mensaje)
        values.put(COLUMN_QUEJA_CLASIFICACION, clasificacion)
        values.put(COLUMN_QUEJA_ESTADO, estado)
        db.insert(TABLE_QUEJAS, null, values)
    }

    fun getAllQuejas(): List<Complaint> {
        val list = mutableListOf<Complaint>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_QUEJAS ORDER BY $COLUMN_QUEJA_FECHA DESC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Complaint(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5) ?: "N/A"
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateQuejaStatus(id: Int, nuevoEstado: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_QUEJA_ESTADO, nuevoEstado)

        // TOQUE PRO: Actualizamos la fecha a la hora exacta en la que el docente resolvió la queja
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val fechaActual = dateFormat.format(java.util.Date())
        values.put(COLUMN_QUEJA_FECHA, fechaActual)

        val result = db.update(TABLE_QUEJAS, values, "$COLUMN_QUEJA_ID = ?", arrayOf(id.toString()))
        return result > 0
    }
}