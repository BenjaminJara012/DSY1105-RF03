package com.librompaz.app.domain

import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.RegistroAsistencia

/**
 * RN-05: un solo registro de asistencia por estudiante y fecha. El id se arma con el estudiante y
 * la fecha, así que volver a guardar la misma fecha produce el MISMO id y la fila se reemplaza.
 */
fun idAsistencia(estudianteId: String, fecha: String): String = "${estudianteId}_$fecha"

/** RN-08: una sola nota por estudiante y evaluación (mismo criterio que la asistencia). */
fun idCalificacion(evaluacionId: String, estudianteId: String): String = "${evaluacionId}_$estudianteId"

/**
 * Arma los registros de asistencia del curso en la fecha dada. La firma (registradoPor y
 * fechaHoraRegistro) la pone el sistema, nunca el usuario (RNF-04).
 */
fun construirRegistrosAsistencia(
    cursoId: String,
    fecha: String,
    estados: Map<String, EstadoAsistencia>,
    registradoPor: String,
    fechaHoraRegistro: String
): List<RegistroAsistencia> =
    estados.map { (estudianteId, estado) ->
        RegistroAsistencia(
            id = idAsistencia(estudianteId, fecha),
            estudianteId = estudianteId,
            cursoId = cursoId,
            fecha = fecha,
            estado = estado,
            registradoPor = registradoPor,
            fechaHoraRegistro = fechaHoraRegistro
        )
    }

/** Ids de [estados] que no son estudiantes del curso (deben estar vacíos para poder guardar). */
fun estudiantesAjenos(estudiantesDelCurso: Set<String>, estados: Map<String, EstadoAsistencia>): List<String> =
    estados.keys.filter { it !in estudiantesDelCurso }
