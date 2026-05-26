package br.com.ifsp.matheus.microredesocial.model

import java.io.Serializable

data class Post(
    val autor: String = "",
    val fotoAutor: String = "",
    val cidade: String = "",
    val imagem: String = "",
    val texto: String = "",
    val timestamp: Long = 0
) : Serializable