package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Instrumento de evaluación. Tabla "evaluaciones".
 * El índice único impide repetir el mismo nombre en la misma fecha, curso y asignatura.
 */
@Entity(
    tableName = "evaluaciones",
    foreignKeys = [
        ForeignKey(entity = Curso::class, parentColumns = ["id"], childColumns = ["cursoId"]),
        ForeignKey(entity = Asignatura::class, parentColumns = ["id"], childColumns = ["asignaturaId"])
    ],
    indices = [
        Index(value = ["cursoId", "asignaturaId", "fecha", "nombreInstrumento"], unique = true),
        Index("asignaturaId")
    ]
)
data class Evaluacion(
    @PrimaryKey val id: String,
    val cursoId: String,
    val asignaturaId: String,
    val nombreInstrumento: String,
    val fecha: String
)

/**
 * Calificación en escala chilena (1,0 a 7,0; la regla está en domain/Validaciones.kt).
 *
 * Tabla "calificaciones". RN-08: una nota por estudiante y evaluación (índice único
 * evaluacionId + estudianteId e id "<evaluacionId>_<estudianteId>"). Si se elimina la evaluación,
 * SQLite borra sus notas (ForeignKey.CASCADE).
 */
@Entity(
    tableName = "calificaciones",
    foreignKeys = [
        ForeignKey(
            entity = Evaluacion::class,
            parentColumns = ["id"],
            childColumns = ["evaluacionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(entity = Estudiante::class, parentColumns = ["id"], childColumns = ["estudianteId"])
    ],
    indices = [
        Index(value = ["evaluacionId", "estudianteId"], unique = true),
        Index("estudianteId")
    ]
)
data class Calificacion(
    @PrimaryKey val id: String,
    val evaluacionId: String,
    val estudianteId: String,
    val nota: Double,
    val registradoPor: String
)
