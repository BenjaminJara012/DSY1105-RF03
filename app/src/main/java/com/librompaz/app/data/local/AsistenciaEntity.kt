package com.librompaz.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asistencias",
    indices = [Index(value = ["estudianteId", "fecha"], unique = true)]
)
data class AsistenciaEntity(
    @PrimaryKey val id: String,
    val estudianteId: String,
    val cursoId: String,
    val fecha: String,
    val estado: String,
    val registradoPor: String,
    val fechaHoraRegistro: String
)