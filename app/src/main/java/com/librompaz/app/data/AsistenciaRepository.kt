package com.librompaz.app.data

import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.data.local.AsistenciaEntity
import com.librompaz.app.data.local.AuditoriaEntity
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Perfil
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AsistenciaRepository(private val db: AppDatabase) {

    suspend fun obtenerAsistencia(
        cursoId: String,
        fecha: String
    ): List<RegistroAsistencia> =
        db.asistenciaDao().delCursoEnFecha(cursoId, fecha).map {
            RegistroAsistencia(
                id = it.id,
                estudianteId = it.estudianteId,
                cursoId = it.cursoId,
                fecha = it.fecha,
                estado = EstadoAsistencia.valueOf(it.estado),
                registradoPor = it.registradoPor,
                fechaHoraRegistro = it.fechaHoraRegistro
            )
        }

    suspend fun guardarAsistencia(
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        usuario: Usuario,
        detalle: String
    ): String {
        val previos = db.asistenciaDao().delCursoEnFecha(cursoId, fecha)
            .associateBy { it.estudianteId }
        val momento = ahora()

        val registros = estados.map { (estudianteId, estado) ->
            AsistenciaEntity(
                id = previos[estudianteId]?.id ?: UUID.randomUUID().toString(),
                estudianteId = estudianteId,
                cursoId = cursoId,
                fecha = fecha,
                estado = estado.name,
                registradoPor = usuario.nombre,
                fechaHoraRegistro = momento
            )
        }

        val accion = if (previos.isEmpty()) "CREAR" else "MODIFICAR"
        val auditoria = AuditoriaEntity(
            id = UUID.randomUUID().toString(),
            usuario = usuario.nombre,
            perfil = usuario.perfil.name,
            accion = accion,
            detalle = detalle,
            fechaHora = momento
        )

        db.guardarConAuditoria(registros, auditoria)
        return accion
    }

    val auditoria: Flow<List<RegistroAuditoria>> =
        db.auditoriaDao().todas().map { lista ->
            lista.map {
                RegistroAuditoria(
                    id = it.id,
                    usuario = it.usuario,
                    perfil = Perfil.valueOf(it.perfil),
                    accion = it.accion,
                    detalle = it.detalle,
                    fechaHora = it.fechaHora
                )
            }
        }
}

private fun ahora(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())