package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de auditoría. RN-02: toda creación o modificación queda auditada (usuario, perfil,
 * acción, detalle, fecha y hora) de forma automática e inalterable.
 *
 * Tabla "auditoria". Es INALTERABLE porque su DAO (AuditoriaDao) solo tiene @Insert y consultas:
 * no existe ninguna función para actualizar ni borrar estas filas.
 */
@Entity(tableName = "auditoria")
data class RegistroAuditoria(
    @PrimaryKey val id: String,
    val usuario: String,
    val perfil: Perfil,
    val accion: String,
    val detalle: String,
    val fechaHora: String
)

/**
 * Métricas consolidadas de un curso para Dirección (RF-12). NO es una tabla: se calcula a partir
 * de las otras (ver domain/Metricas.kt). porcentajeAsistencia es null si el curso no tiene registros.
 *
 * Consistencia de registros: días con asistencia incompleta y cargas sin clases en la última semana.
 */
data class ResumenCurso(
    val cursoId: String,
    val porcentajeAsistencia: Int? = null,
    val sesionesRegistradas: Int = 0,
    val calificacionesRegistradas: Int = 0,
    val anotacionesNegativas: Int = 0,
    val diasAsistenciaIncompleta: Int = 0,
    val cargasSinClasesRecientes: Int = 0
)
