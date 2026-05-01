package com.example.appreporte

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AppReporte.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_ROL = "rol"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ROL + " TEXT" + ")")
        db.execSQL(createTable)

        // Insertar usuarios iniciales
        insertUser(db, "admin@reporte.com", "admin123", "admin")
        insertUser(db, "user@reporte.com", "user123", "usuario")
        insertUser(db, "docente@reporte.com", "docente123", "docente")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
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
}
