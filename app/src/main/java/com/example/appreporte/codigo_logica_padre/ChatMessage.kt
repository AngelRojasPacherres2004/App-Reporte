package com.example.appreporte.codigo_logica_padre


import com.example.appreporte.R
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
