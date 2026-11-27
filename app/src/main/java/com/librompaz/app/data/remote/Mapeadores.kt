package com.librompaz.app.data.remote

import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.Usuario
import com.librompaz.app.model.Perfil

// =====================================================================================
// Mapeadores DTO <-> entidad (modelo de la app). Los enums viajan por su nombre ("PRESENTE",
// "DOCENTE"...). Si el servidor enviara un valor que la app no conoce, esa fila se descarta en vez
// de cerrar la app: los mapeadores de enums devuelven null y las listas se convierten con mapNotNull.
//
// Los dos primeros pares están resueltos como ejemplo del patrón. El resto es el TODO(E3-9).
// =====================================================================================

/** Convierte el texto de un enum en su valor, o null si no existe (valor desconocido). */
@Suppress("unused")
private inline fun <reified E : Enum<E>> enumONull(nombre: String): E? =
    enumValues<E>().firstOrNull { it.name == nombre }

// ---------- Ejemplo resuelto ----------

fun CursoDto.aEntidad() = Curso(id, nombre)
fun Curso.aDto() = CursoDto(id, nombre)

fun AsignaturaDto.aEntidad() = Asignatura(id, nombre)
fun Asignatura.aDto() = AsignaturaDto(id, nombre)

// ---------- TODO(E3-9) ----------
// TODO(E3-9) [Mapeadores.kt] Mapeadores restantes.
//   QUÉ: implementar cada función copiando campo por campo (los nombres de los DTO ya son el contrato de la API,
//        ver Dtos.kt y backend/docs/API.md). Reglas: (1) los DTO cuyo enum puede venir desconocido
//        (AsistenciaDto, AnotacionDto, AuditoriaDto, UsuarioDto) devuelven null si el enum no existe (usar enumONull);
//        (2) la nota viaja como Double; (3) el estado de asistencia viaja con estado.name.
//   ACEPTACIÓN: para cada par, entidad.aDto().aEntidad() == entidad (ida y vuelta); AsistenciaDto con
//        estado "XYZ".aEntidad() == null; Map(est1 -> PRESENTE).aRegistrosDto() == [RegistroEstadoDto(est1, "PRESENTE")];
//        todos los campos de backend/data/seed.json aparecen en los DTO (la prueba de contrato lo comprueba).

fun EstudianteDto.aEntidad(): Estudiante = TODO("E3-9")
fun Estudiante.aDto(): EstudianteDto = TODO("E3-9")

fun CargaDocenteDto.aEntidad(): CargaDocente = TODO("E3-9")
fun CargaDocente.aDto(): CargaDocenteDto = TODO("E3-9")

fun ObjetivoDto.aEntidad(): ObjetivoAprendizaje = TODO("E3-9")
fun ObjetivoAprendizaje.aDto(): ObjetivoDto = TODO("E3-9")

fun AsistenciaDto.aEntidad(): RegistroAsistencia? = TODO("E3-9: null si el estado no existe")
fun RegistroAsistencia.aDto(): AsistenciaDto = TODO("E3-9")

/** Estados del formulario de asistencia (estudiante -> estado) como cuerpo de la petición. */
fun Map<String, EstadoAsistencia>.aRegistrosDto(): List<RegistroEstadoDto> = TODO("E3-9")

fun SesionDto.aEntidad(): SesionClase = TODO("E3-9")
fun SesionClase.aDto(): SesionDto = TODO("E3-9")

fun EvaluacionDto.aEntidad(): Evaluacion = TODO("E3-9")
fun Evaluacion.aDto(): EvaluacionDto = TODO("E3-9")

fun CalificacionDto.aEntidad(): Calificacion = TODO("E3-9")
fun Calificacion.aDto(): CalificacionDto = TODO("E3-9")

/** Notas ya validadas (estudiante -> nota) y estudiantes cuya nota se elimina, como cuerpo de la petición. */
fun armarNotasDto(notas: Map<String, Double>, sinNota: List<String>): GuardarNotasRequest = TODO("E3-9")

fun AnotacionDto.aEntidad(): Anotacion? = TODO("E3-9: null si el tipo no existe")
fun Anotacion.aDto(): AnotacionDto = TODO("E3-9")

fun AuditoriaDto.aEntidad(): RegistroAuditoria? = TODO("E3-9: null si el perfil no existe")
fun RegistroAuditoria.aDto(): AuditoriaDto = TODO("E3-9")

/**
 * El servidor nunca envía la clave. [claveHash] es el SHA-256 con sal de la clave que el usuario acaba de
 * escribir (ClaveSegura.hash): permite ingresar sin conexión con las mismas credenciales.
 * Devuelve null si el perfil no es uno de los cuatro definidos (RN-01).
 */
fun UsuarioDto.aEntidad(claveHash: String): Usuario? {
    val perfilValido = enumONull<Perfil>(perfil) ?: return null
    return Usuario(id, nombre, usuario, claveHash, perfilValido)
}
fun Usuario.aDto(): UsuarioDto = TODO("E3-9")
