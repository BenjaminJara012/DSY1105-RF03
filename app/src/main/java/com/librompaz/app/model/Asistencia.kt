package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EstadoAsistencia(val etiqueta: String, val abreviatura: String) {
    PRESENTE("Presente", "P"),
    AUSENTE("Ausente", "A"),
    ATRASADO("Atrasado", "T"),
    JUSTIFICADO("Justificado", "J")
}

/**
 * Registro de asistencia diaria. registradoPor y fechaHoraRegistro son la firma automática.
 *
 * Tabla "asistencias". RN-05: un solo registro por estudiante y fecha, garantizado de dos formas:
 * el índice único (estudianteId, fecha) y un id determinista "<estudianteId>_<fecha>" (ver
 * domain/RegistrosAsistencia.kt), de modo que guardar de nuevo la misma fecha REEMPLAZA la fila.
 */
@Entity(
    tableName = "asistencias",
    foreignKeys = [ForeignKey(entity = Estudiante::class, parentColumns = ["id"], childColumns = ["estudianteId"])],
    indices = [
        Index(value = ["estudianteId", "fecha"], unique = true),
        Index(value = ["cursoId", "fecha"])
    ]
)
data class RegistroAsistencia(
    @PrimaryKey val id: String,
    val estudianteId: String,
    val cursoId: String,
    val fecha: String,
    val estado: EstadoAsistencia,
    val registradoPor: String,
    val fechaHoraRegistro: String
)
