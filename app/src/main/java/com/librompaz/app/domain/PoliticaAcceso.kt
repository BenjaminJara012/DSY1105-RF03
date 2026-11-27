package com.librompaz.app.domain

import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Perfil
import com.librompaz.app.model.Usuario

/**
 * Control de acceso por rol (RBAC, RF-02) y alcance del docente sobre sus cargas (RN-03).
 *
 * Son funciones puras (sin Android): el ViewModel las llama antes de CADA operación que guarda
 * datos, así que la regla se cumple aunque alguien invoque la función sin pasar por la pantalla.
 * Cada función devuelve null si la acción está permitida, o el mensaje de error para el usuario.
 *
 * Matriz de permisos de escritura:
 *  - Asistencia: Docente (solo cursos de sus cargas) e Inspectoría (todos los cursos).
 *  - Clases con OA, evaluaciones, notas y anotaciones: solo Docente, y solo sobre sus cargas.
 *  - UTP y Dirección: solo lectura.
 */
object PoliticaAcceso {

    const val MENSAJE_SIN_SESION = "Inicia sesión para continuar."
    const val MENSAJE_SIN_PERMISO = "Tu perfil no tiene permiso para esta acción."
    const val MENSAJE_SIN_CARGA = "No tienes asignado este curso o asignatura."

    /** RF-03 y RF-05: pasar o corregir la asistencia de un curso. */
    fun verificarAsistencia(usuario: Usuario?, cursoId: String, cargas: List<CargaDocente>): String? =
        when {
            usuario == null -> MENSAJE_SIN_SESION
            usuario.perfil == Perfil.INSPECTORIA -> null
            usuario.perfil == Perfil.DOCENTE -> if (tieneCarga(usuario, cargas, cursoId)) null else MENSAJE_SIN_CARGA
            else -> MENSAJE_SIN_PERMISO
        }

    /** RF-06, RF-08 y notas: solo el docente con carga en ese curso y asignatura. */
    fun verificarCarga(usuario: Usuario?, cursoId: String, asignaturaId: String, cargas: List<CargaDocente>): String? =
        when {
            usuario == null -> MENSAJE_SIN_SESION
            usuario.perfil != Perfil.DOCENTE -> MENSAJE_SIN_PERMISO
            tieneCarga(usuario, cargas, cursoId, asignaturaId) -> null
            else -> MENSAJE_SIN_CARGA
        }

    /** RF-10: solo el docente, y solo estudiantes de un curso de sus cargas. */
    fun verificarAnotacion(usuario: Usuario?, cursoId: String, cargas: List<CargaDocente>): String? =
        when {
            usuario == null -> MENSAJE_SIN_SESION
            usuario.perfil != Perfil.DOCENTE -> MENSAJE_SIN_PERMISO
            tieneCarga(usuario, cargas, cursoId) -> null
            else -> MENSAJE_SIN_CARGA
        }

    /** RN-03: ¿el docente tiene una carga en ese curso (y, si se indica, en esa asignatura)? */
    fun tieneCarga(
        usuario: Usuario,
        cargas: List<CargaDocente>,
        cursoId: String,
        asignaturaId: String? = null
    ): Boolean =
        usuario.perfil == Perfil.DOCENTE && cargas.any {
            it.docenteId == usuario.id && it.cursoId == cursoId &&
                (asignaturaId == null || it.asignaturaId == asignaturaId)
        }
}
