package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Objetivo de Aprendizaje (OA). Tabla "objetivos".
 * Supuesto vigente: catálogo cerrado de OA de ejemplo cargado en la base; el docente solo elige.
 */
@Entity(
    tableName = "objetivos",
    foreignKeys = [ForeignKey(entity = Asignatura::class, parentColumns = ["id"], childColumns = ["asignaturaId"])],
    indices = [Index("asignaturaId")]
)
data class ObjetivoAprendizaje(
    @PrimaryKey val id: String,
    val asignaturaId: String,
    val codigo: String,
    val descripcion: String
)

/**
 * Sesión o bloque lectivo con los OA y contenidos trabajados. Tabla "sesiones".
 *
 * oaIds es una lista: SQLite no tiene columnas de tipo lista, así que data/local/Converters.kt la
 * guarda como texto separado por comas ("OA-MAT-1,OA-MAT-2") y la reconstruye al leer.
 * El índice único impide repetir curso + asignatura + fecha + bloque.
 */
@Entity(
    tableName = "sesiones",
    foreignKeys = [
        ForeignKey(entity = Curso::class, parentColumns = ["id"], childColumns = ["cursoId"]),
        ForeignKey(entity = Asignatura::class, parentColumns = ["id"], childColumns = ["asignaturaId"])
    ],
    indices = [
        Index(value = ["cursoId", "asignaturaId", "fecha", "bloque"], unique = true),
        Index("asignaturaId")
    ]
)
data class SesionClase(
    @PrimaryKey val id: String,
    val cursoId: String,
    val asignaturaId: String,
    val fecha: String,
    val bloque: String,
    val oaIds: List<String>,
    val contenido: String,
    val registradoPor: String,
    val fechaHoraRegistro: String
)
