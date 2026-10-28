package com.librompaz.app.model

/** Instrumento de evaluación (parte de la "Planilla de calificaciones" del caso). */
data class Evaluacion(
    val id: String,
    val cursoId: String,
    val asignaturaId: String,
    val nombreInstrumento: String,
    val fecha: String
)

/**
 * Calificación en escala numérica chilena. Pendiente con el CITT: confirmar si se valida el
 * rango 1,0 a 7,0 dentro de la app.
 */
data class Calificacion(
    val id: String,
    val evaluacionId: String,
    val estudianteId: String,
    val nota: Double,
    val registradoPor: String
)
