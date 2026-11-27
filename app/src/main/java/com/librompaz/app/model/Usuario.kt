package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Usuario del sistema (solo datos ficticios). Tabla "usuarios".
 *
 * - Solo existen los cuatro perfiles internos (RN-01): el perfil se guarda con el nombre del enum.
 * - La clave nunca se guarda en texto plano: se guarda su hash SHA-256 con sal (ver
 *   domain/ClaveSegura.kt).
 * - El correo es único.
 */
@Entity(tableName = "usuarios", indices = [Index(value = ["usuario"], unique = true)])
data class Usuario(
    @PrimaryKey val id: String,
    val nombre: String,
    val usuario: String,
    val claveHash: String,
    val perfil: Perfil
)
