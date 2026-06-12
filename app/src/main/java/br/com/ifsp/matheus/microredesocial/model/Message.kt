package br.com.ifsp.matheus.microredesocial.model

data class Message(
    val remetenteId: String = "",
    val destinatarioId: String = "",
    val texto: String = "",
    val timestamp: Long = 0,
    val lida: Boolean = false
)