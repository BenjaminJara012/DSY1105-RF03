package com.librompaz.app.data.remote

import com.google.gson.annotations.SerializedName

// =====================================================================================
// DTOs (Data Transfer Objects): la forma EXACTA del JSON que intercambia la API (ver
// backend/docs/API.md). Cada campo lleva @SerializedName con el nombre del JSON, así el
// contrato no depende de cómo se llamen las variables ni de que el código se ofusque.
// Los mapeadores a entidades de Room están en Mapeadores.kt.
// =====================================================================================

// ---------- Lecturas (misma forma que las entidades) ----------

data class CursoDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String
)

data class AsignaturaDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String
)

data class EstudianteDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombreFicticio") val nombreFicticio: String,
    @SerializedName("cursoId") val cursoId: String
)

data class CargaDocenteDto(
    @SerializedName("id") val id: String,
    @SerializedName("docenteId") val docenteId: String,
    @SerializedName("cursoId") val cursoId: String,
    @SerializedName("asignaturaId") val asignaturaId: String
)

data class ObjetivoDto(
    @SerializedName("id") val id: String,
    @SerializedName("asignaturaId") val asignaturaId: String,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("descripcion") val descripcion: String
)

data class AsistenciaDto(
    @SerializedName("id") val id: String,
    @SerializedName("estudianteId") val estudianteId: String,
    @SerializedName("cursoId") val cursoId: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("registradoPor") val registradoPor: String,
    @SerializedName("fechaHoraRegistro") val fechaHoraRegistro: String
)

data class SesionDto(
    @SerializedName("id") val id: String,
    @SerializedName("cursoId") val cursoId: String,
    @SerializedName("asignaturaId") val asignaturaId: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("bloque") val bloque: String,
    @SerializedName("oaIds") val oaIds: List<String>,
    @SerializedName("contenido") val contenido: String,
    @SerializedName("registradoPor") val registradoPor: String,
    @SerializedName("fechaHoraRegistro") val fechaHoraRegistro: String
)

data class EvaluacionDto(
    @SerializedName("id") val id: String,
    @SerializedName("cursoId") val cursoId: String,
    @SerializedName("asignaturaId") val asignaturaId: String,
    @SerializedName("nombreInstrumento") val nombreInstrumento: String,
    @SerializedName("fecha") val fecha: String
)

data class CalificacionDto(
    @SerializedName("id") val id: String,
    @SerializedName("evaluacionId") val evaluacionId: String,
    @SerializedName("estudianteId") val estudianteId: String,
    @SerializedName("nota") val nota: Double,
    @SerializedName("registradoPor") val registradoPor: String
)

data class AnotacionDto(
    @SerializedName("id") val id: String,
    @SerializedName("estudianteId") val estudianteId: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fechaHora") val fechaHora: String,
    @SerializedName("firmadoPor") val firmadoPor: String
)

data class AuditoriaDto(
    @SerializedName("id") val id: String,
    @SerializedName("usuario") val usuario: String,
    @SerializedName("perfil") val perfil: String,
    @SerializedName("accion") val accion: String,
    @SerializedName("detalle") val detalle: String,
    @SerializedName("fechaHora") val fechaHora: String
)

// ---------- Autenticación y estado ----------

/** Usuario que devuelve el servidor: nunca incluye la clave ni su hash. */
data class UsuarioDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("usuario") val usuario: String,
    @SerializedName("perfil") val perfil: String
)

data class LoginRequest(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("clave") val clave: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("usuario") val usuario: UsuarioDto
)

data class OkDto(@SerializedName("ok") val ok: Boolean)

data class SaludDto(
    @SerializedName("estado") val estado: String,
    @SerializedName("servicio") val servicio: String,
    @SerializedName("version") val version: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("aislado") val aislado: Boolean
)

/** Cuerpo de error de la API: {"error": "...", "campo": "..."}; "campo" solo si el problema es de un campo. */
data class ErrorDto(
    @SerializedName("error") val error: String?,
    @SerializedName("campo") val campo: String?
)

// ---------- Escrituras ----------

data class RegistroEstadoDto(
    @SerializedName("estudianteId") val estudianteId: String,
    @SerializedName("estado") val estado: String
)

data class GuardarAsistenciaRequest(
    @SerializedName("registros") val registros: List<RegistroEstadoDto>
)

data class GuardarAsistenciaResponse(
    @SerializedName("accion") val accion: String,
    @SerializedName("total") val total: Int,
    @SerializedName("registros") val registros: List<AsistenciaDto>
)

data class CrearSesionRequest(
    @SerializedName("cursoId") val cursoId: String,
    @SerializedName("asignaturaId") val asignaturaId: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("bloque") val bloque: String,
    @SerializedName("oaIds") val oaIds: List<String>,
    @SerializedName("contenido") val contenido: String
)

data class CrearEvaluacionRequest(
    @SerializedName("cursoId") val cursoId: String,
    @SerializedName("asignaturaId") val asignaturaId: String,
    @SerializedName("nombreInstrumento") val nombreInstrumento: String,
    @SerializedName("fecha") val fecha: String
)

data class EliminacionDto(
    @SerializedName("id") val id: String,
    @SerializedName("notasEliminadas") val notasEliminadas: Int
)

data class NotaEntradaDto(
    @SerializedName("estudianteId") val estudianteId: String,
    @SerializedName("nota") val nota: Double
)

data class GuardarNotasRequest(
    @SerializedName("notas") val notas: List<NotaEntradaDto>,
    @SerializedName("sinNota") val sinNota: List<String>
)

data class GuardarNotasResponse(
    @SerializedName("guardadas") val guardadas: Int,
    @SerializedName("eliminadas") val eliminadas: Int,
    @SerializedName("calificaciones") val calificaciones: List<CalificacionDto>
)

data class CrearAnotacionRequest(
    @SerializedName("estudianteId") val estudianteId: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("descripcion") val descripcion: String
)
