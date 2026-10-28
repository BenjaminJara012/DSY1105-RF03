package com.librompaz.app.model

/**
 * Registro de auditoría (entidad "Control de seguridad y trazabilidad" del caso). Regla de
 * negocio: toda creación o modificación queda auditada (usuario, fecha y hora) de forma
 * automática e inalterable.
 */
data class RegistroAuditoria(
    val id: String,
    val usuario: String,
    val perfil: Perfil,
    val accion: String,
    val detalle: String,
    val fechaHora: String
)

/**
 * Métricas consolidadas de un curso para Dirección. Los valores null significan "todavía no
 * calculado" (ver LibroMpazViewModel.resumenPorCurso()).
 */
data class ResumenCurso(
    val cursoId: String,
    val porcentajeAsistencia: Int? = null,
    val sesionesRegistradas: Int? = null,
    val calificacionesRegistradas: Int? = null,
    val anotacionesNegativas: Int? = null
)
