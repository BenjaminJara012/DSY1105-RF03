package com.librompaz.app.domain

import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.SesionClase

/**
 * Reglas de negocio de las ESCRITURAS que combinan varias validaciones. Son funciones puras (sin
 * Android ni base de datos): las usan por igual el ViewModel (para mostrar los errores junto a cada
 * campo) y el repositorio del modo local (que vuelve a validar antes de guardar, como lo hace el
 * servidor). Así hay una sola definición de cada regla.
 */

const val MENSAJE_OA_AJENOS = "Hay OA que no pertenecen a esta asignatura"
const val MENSAJE_SESION_DUPLICADA = "Ya hay una clase registrada en ese bloque y fecha"
const val MENSAJE_EVALUACION_DUPLICADA = "Ya existe una evaluación con ese nombre en esa fecha"
const val MENSAJE_LISTA_ASISTENCIA = "La lista de asistencia no corresponde al curso."
const val MENSAJE_SIN_NOTAS = "Escribe al menos una nota."

/**
 * RF-06: validaciones de una clase nueva. Además de [validarSesion], los OA deben pertenecer a la
 * asignatura ([objetivosDeLaAsignatura]) y no puede repetirse curso + asignatura + fecha + bloque.
 */
fun erroresDeSesionNueva(
    fecha: String,
    bloque: String,
    oaIds: List<String>,
    contenido: String,
    hoy: String,
    objetivosDeLaAsignatura: List<ObjetivoAprendizaje>,
    existentes: List<SesionClase>,
    cursoId: String,
    asignaturaId: String
): ErroresSesion {
    var errores = validarSesion(fecha, bloque, oaIds, contenido, hoy)
    if (errores.objetivos == null && !objetivosDeLaAsignatura.map { it.id }.containsAll(oaIds)) {
        errores = errores.copy(objetivos = MENSAJE_OA_AJENOS)
    }
    if (errores.fecha == null && errores.bloque == null &&
        sesionDuplicada(existentes, cursoId, asignaturaId, fecha, bloque)
    ) {
        errores = errores.copy(bloque = MENSAJE_SESION_DUPLICADA)
    }
    return errores
}

/** RF-08: validaciones de una evaluación nueva (nombre, fecha y que no repita nombre en la misma fecha). */
fun erroresDeEvaluacionNueva(
    nombre: String,
    fecha: String,
    hoy: String,
    existentes: List<Evaluacion>,
    cursoId: String,
    asignaturaId: String
): ErroresEvaluacion {
    var errores = validarEvaluacion(nombre, fecha, hoy)
    if (errores.nombre == null && errores.fecha == null &&
        evaluacionDuplicada(existentes, cursoId, asignaturaId, nombre, fecha)
    ) {
        errores = errores.copy(nombre = MENSAJE_EVALUACION_DUPLICADA)
    }
    return errores
}

/** RF-03: la lista no puede estar vacía ni traer estudiantes que no son del curso. Devuelve el error o null. */
fun errorListaAsistencia(estudiantesDelCurso: Set<String>, estados: Map<String, EstadoAsistencia>): String? =
    if (estados.isEmpty() || estudiantesAjenos(estudiantesDelCurso, estados).isNotEmpty()) MENSAJE_LISTA_ASISTENCIA else null

/**
 * RN-04 para una nota ya convertida a número: entre 1,0 y 7,0 y con un decimal como máximo.
 * Devuelve el error o null. (La versión para texto escrito es [validarNota].)
 */
fun validarNotaNumerica(nota: Double): String? = when {
    nota.isNaN() || nota.isInfinite() -> MENSAJE_NOTA_INVALIDA
    nota < NOTA_MINIMA || nota > NOTA_MAXIMA -> MENSAJE_NOTA_FUERA_DE_RANGO
    Math.abs(nota * 10 - Math.round(nota * 10).toDouble()) > 1e-9 -> MENSAJE_NOTA_DECIMALES
    else -> null
}

/** Primer error de la clase en el orden en que se piden los campos (el repositorio informa uno solo). */
fun ErroresSesion.primerMensaje(): String? = listOfNotNull(fecha, bloque, objetivos, contenido).firstOrNull()

fun ErroresEvaluacion.primerMensaje(): String? = listOfNotNull(nombre, fecha).firstOrNull()

fun ErroresAnotacion.primerMensaje(): String? = listOfNotNull(estudiante, tipo, descripcion).firstOrNull()
