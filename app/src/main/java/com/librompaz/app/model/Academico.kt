package com.librompaz.app.model

/** Curso o nivel (entidad "Estudiante y curso" del caso). */
data class Curso(
    val id: String,
    val nombre: String
)

data class Asignatura(
    val id: String,
    val nombre: String
)

/** Estudiante con identificación interna y nombre FICTICIO (prohibido usar datos reales o RUT). */
data class Estudiante(
    val id: String,
    val nombreFicticio: String,
    val cursoId: String
)

/**
 * Asignación de carga docente (entidad del caso): vínculo docente–curso–asignatura.
 * RF: el docente solo ve los cursos y asignaturas bajo su asignación formal.
 */
data class CargaDocente(
    val id: String,
    val docenteId: String,
    val cursoId: String,
    val asignaturaId: String
)
