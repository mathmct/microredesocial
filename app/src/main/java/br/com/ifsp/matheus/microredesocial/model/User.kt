package br.com.ifsp.matheus.microredesocial.model

import java.io.Serializable

data class User(
    val email: String = "",
    val nomeCompleto: String = "",
    val fotoPerfil: String = ""
) : Serializable