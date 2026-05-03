package com.example.appreporte

data class Complaint(
    val id: Int,
    val userEmail: String,
    val mensaje: String,
    val clasificacion: String,
    val estado: String,
    val fecha: String
)