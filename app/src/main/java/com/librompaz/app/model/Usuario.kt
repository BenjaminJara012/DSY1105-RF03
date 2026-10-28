package com.librompaz.app.model

/** Usuario del sistema. Solo datos ficticios (ver MockData.usuarios). */
data class Usuario(
    val id: String,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val perfil: Perfil
)
