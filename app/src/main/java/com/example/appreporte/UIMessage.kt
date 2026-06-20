package com.example.appreporte

/**
 * Modelo para la interfaz de usuario del chat.
 * @param sender Nombre del emisor (ej: "Tú", "EduConnect IA")
 * @param content Contenido del mensaje, soporta formato Markdown básico (**negrita**)
 * @param isMe Indica si el mensaje fue enviado por el usuario actual para el alineamiento en el RecyclerView
 */
data class UIMessage(
    val sender: String,
    val content: String,
    val isMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
