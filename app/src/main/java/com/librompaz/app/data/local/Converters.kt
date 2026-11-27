package com.librompaz.app.data.local

import androidx.room.TypeConverter

/**
 * TypeConverter: le enseña a Room a guardar tipos que SQLite no conoce.
 * Aquí, la lista de OA de una sesión (List<String>) se guarda como un texto separado por comas.
 *
 * Los enums (Perfil, EstadoAsistencia, TipoAnotacion) no necesitan converter: Room los guarda
 * solos, con su nombre (por ejemplo "PRESENTE").
 */
class Converters {

    @TypeConverter
    fun listaATexto(lista: List<String>): String = lista.joinToString(",")

    @TypeConverter
    fun textoALista(texto: String): List<String> = texto.split(",").filter { it.isNotBlank() }
}
