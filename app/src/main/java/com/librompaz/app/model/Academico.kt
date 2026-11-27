package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Curso o nivel. Tabla "cursos". */
@Entity(tableName = "cursos")
data class Curso(
    @PrimaryKey val id: String,
    val nombre: String
)

/** Tabla "asignaturas". */
@Entity(tableName = "asignaturas")
data class Asignatura(
    @PrimaryKey val id: String,
    val nombre: String
)

/**
 * Estudiante con identificación interna y nombre FICTICIO (prohibido usar datos reales o RUT).
 * Tabla "estudiantes": cada estudiante pertenece a un curso (clave foránea cursoId).
 */
@Entity(
    tableName = "estudiantes",
    foreignKeys = [ForeignKey(entity = Curso::class, parentColumns = ["id"], childColumns = ["cursoId"])],
    indices = [Index("cursoId")]
)
data class Estudiante(
    @PrimaryKey val id: String,
    val nombreFicticio: String,
    val cursoId: String
)

/**
 * Carga docente: vínculo docente–curso–asignatura. Tabla "cargas_docentes".
 * RF-01 / RN-03: el docente solo ve y registra en los cursos y asignaturas de sus cargas.
 * Supuesto vigente: un docente puede tener más de una asignatura y más de un curso.
 */
@Entity(
    tableName = "cargas_docentes",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["docenteId"]),
        ForeignKey(entity = Curso::class, parentColumns = ["id"], childColumns = ["cursoId"]),
        ForeignKey(entity = Asignatura::class, parentColumns = ["id"], childColumns = ["asignaturaId"])
    ],
    indices = [Index("docenteId"), Index("cursoId"), Index("asignaturaId")]
)
data class CargaDocente(
    @PrimaryKey val id: String,
    val docenteId: String,
    val cursoId: String,
    val asignaturaId: String
)
