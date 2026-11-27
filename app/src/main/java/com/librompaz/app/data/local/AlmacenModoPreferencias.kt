package com.librompaz.app.data.local

import android.content.Context
import com.librompaz.app.data.repository.AlmacenModo

/** [AlmacenModo] sobre SharedPreferences (solo en este equipo; el mismo mecanismo que usa la dirección del servidor). */
class AlmacenModoPreferencias(contexto: Context) : AlmacenModo {

    private val preferencias = contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    override fun leer(): Boolean? =
        if (preferencias.contains(CLAVE_MODO_LOCAL)) preferencias.getBoolean(CLAVE_MODO_LOCAL, false) else null

    override fun guardar(valor: Boolean) {
        preferencias.edit().putBoolean(CLAVE_MODO_LOCAL, valor).apply()
    }

    private companion object {
        const val ARCHIVO = "modo_datos"
        const val CLAVE_MODO_LOCAL = "modo_local"
    }
}
