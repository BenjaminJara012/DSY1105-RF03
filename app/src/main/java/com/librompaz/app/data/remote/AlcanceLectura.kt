package com.librompaz.app.data.remote

import com.librompaz.app.model.Perfil

/** Colecciones que la app descarga del servidor. */
enum class Coleccion { ESTUDIANTES, CARGAS, ASISTENCIAS, SESIONES, EVALUACIONES, CALIFICACIONES, ANOTACIONES, AUDITORIA }

/**
 * Qué colecciones puede leer cada perfil (RF-02). Es la misma matriz que aplica el servidor
 * (backend/docs/API.md): la app solo pide lo que le corresponde, así no recibe 403 en la
 * sincronización. Cursos, asignaturas y objetivos son de referencia y los leen todos.
 */
object AlcanceLectura {

    private val porColeccion: Map<Coleccion, Set<Perfil>> = mapOf(
        Coleccion.ESTUDIANTES to Perfil.entries.toSet(),
        Coleccion.CARGAS to Perfil.entries.toSet(),
        Coleccion.ASISTENCIAS to setOf(Perfil.DOCENTE, Perfil.INSPECTORIA, Perfil.DIRECCION),
        Coleccion.SESIONES to setOf(Perfil.DOCENTE, Perfil.UTP, Perfil.DIRECCION),
        Coleccion.EVALUACIONES to setOf(Perfil.DOCENTE, Perfil.UTP, Perfil.DIRECCION),
        Coleccion.CALIFICACIONES to setOf(Perfil.DOCENTE, Perfil.UTP, Perfil.DIRECCION),
        Coleccion.ANOTACIONES to setOf(Perfil.DOCENTE, Perfil.INSPECTORIA, Perfil.DIRECCION),
        Coleccion.AUDITORIA to setOf(Perfil.DIRECCION)
    )

    fun puedeLeer(perfil: Perfil, coleccion: Coleccion): Boolean = perfil in porColeccion.getValue(coleccion)
}
