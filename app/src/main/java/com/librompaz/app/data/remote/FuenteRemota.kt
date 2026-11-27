package com.librompaz.app.data.remote

import com.librompaz.app.data.local.InstantaneaServidor
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Perfil
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.TipoAnotacion

/**
 * FUENTE DE DATOS REMOTA: la única clase que conoce a [ApiService]. Cada función hace UNA operación
 * contra la API, la envuelve con [llamarApi] (nunca lanza excepciones) y devuelve [ApiResult] con
 * entidades de la app ya mapeadas (Mapeadores.kt), no con DTO. No decide qué hacer con el resultado
 * (guardar en Room, avisar al usuario): eso es trabajo del repositorio (LibroMpazApiRepository).
 *
 * [apiProvider] entrega el servicio de la URL configurada en ese momento: la URL puede cambiar sin
 * recompilar, por eso no se guarda un único ApiService.
 *
 * Solo se usa cuando BuildConfig.USAR_API es true.
 */
class FuenteRemota(private val apiProvider: () -> ApiService) {

    // ---------- Sesión y lecturas (TODO E3-10) ----------

    // TODO(E3-10) [FuenteRemota.kt] Ingresar: POST /auth/login.
    //   QUÉ: llamar a apiProvider().login(LoginRequest(correo, clave)) con llamarApi y devolver el resultado
    //        (LoginResponse trae el token y el UsuarioDto).
    //   ACEPTACIÓN: credenciales correctas -> Exito con token no vacío; clave incorrecta (401) -> Error con el
    //        mensaje del servidor; servidor apagado -> SinConexion.

    /**
     * Envía las credenciales al servidor para iniciar sesión.
     *
     * llamarApi transforma las respuestas y errores HTTP
     * en ApiResult.Exito, ApiResult.Error o ApiResult.SinConexion.
     */
    suspend fun iniciarSesion(
        correo: String,
        clave: String
    ): ApiResult<LoginResponse> {

        return llamarApi {
            apiProvider().login(
                LoginRequest(
                    usuario = correo.trim(),
                    clave = clave
                )
            )
        }
    }

    // TODO(E3-10) [FuenteRemota.kt] Cerrar sesión: POST /auth/logout.
    //   QUÉ: llamar a logout("Bearer $token") (aquí el encabezado va a mano) con llamarApi.
    //   ACEPTACIÓN: la petición lleva "Authorization: Bearer <token>" una sola vez; si falla, no lanza excepción.

    /**
     * Solicita al servidor cerrar la sesión.
     * Envía el token en el encabezado Authorization.
     */
    suspend fun cerrarSesion(token: String): ApiResult<OkDto> {
        return llamarApi {
            apiProvider().logout("Bearer $token")
        }
    }

    // TODO(E3-10) [FuenteRemota.kt] Descargar todo lo que el perfil puede leer.
    //   QUÉ: pedir cursos, asignaturas y objetivos (los leen todos) y, solo las colecciones que
    //        AlcanceLectura.puedeLeer(perfil, ...) permite, estudiantes, cargas, asistencias, sesiones, evaluaciones,
    //        calificaciones, anotaciones y auditoría. Cada una con llamarApi; si CUALQUIERA falla, devolver ese
    //        fallo (usar obtenerOSalir). Convertir con los mapeadores (mapNotNull donde el mapeador puede dar
    //        null) y armar la InstantaneaServidor; las colecciones no permitidas quedan vacías.
    //   ACEPTACIÓN: para UTP no se llama a /asistencias, /anotaciones ni /auditoria; para Dirección sí se llama a
    //        /auditoria; un 500 en /sesiones devuelve Error y no una instantánea parcial.
    suspend fun descargarTodo(perfil: Perfil): ApiResult<InstantaneaServidor> = TODO("E3-10: descargar datos")

    // TODO(E3-10) [FuenteRemota.kt] Asistencia de un curso en una fecha: GET /asistencias?cursoId=&fecha=.
    //   QUÉ: llamar con llamarApi y mapear con mapNotNull { it.aEntidad() }.
    //   ACEPTACIÓN: la petición lleva los dos parámetros en la URL; una fila con estado desconocido se descarta.
    suspend fun asistenciaDe(cursoId: String, fecha: String): ApiResult<List<RegistroAsistencia>> = TODO("E3-10: asistencia de un día")

    // ---------- Escrituras (TODO E3-11) ----------
    // Todas devuelven ApiResult<Unit>: lo que importa es si el servidor las aceptó. El servidor firma, pone la
    // hora y audita (RN-02, RNF-04): la app NO envía firma, hora ni auditoría.

    // TODO(E3-11) [FuenteRemota.kt] Guardar asistencia: PUT /cursos/{cursoId}/asistencia/{fecha}.
    //   QUÉ: cuerpo GuardarAsistenciaRequest(estados.aRegistrosDto()), con llamarApi y descartando la respuesta
    //        (mapear { }). ACEPTACIÓN: el JSON enviado tiene "registros":[{"estudianteId":...,"estado":"AUSENTE"},...].
    suspend fun guardarAsistencia(cursoId: String, fecha: String, estados: Map<String, EstadoAsistencia>): ApiResult<Unit> =
        TODO("E3-11: guardar asistencia")

    // TODO(E3-11) [FuenteRemota.kt] Registrar una clase: POST /sesiones con CrearSesionRequest.
    //   ACEPTACIÓN: un 409 (mismo curso, asignatura, fecha y bloque) llega como Error con el mensaje del servidor.
    suspend fun crearSesion(
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String
    ): ApiResult<Unit> = TODO("E3-11: crear sesión")

    // TODO(E3-11) [FuenteRemota.kt] Crear una evaluación: POST /evaluaciones con CrearEvaluacionRequest.
    //   ACEPTACIÓN: un 422 llega como Error con el "campo" que indica el servidor.
    suspend fun crearEvaluacion(cursoId: String, asignaturaId: String, nombreInstrumento: String, fecha: String): ApiResult<Unit> =
        TODO("E3-11: crear evaluación")

    // TODO(E3-11) [FuenteRemota.kt] Eliminar una evaluación y sus notas: DELETE /evaluaciones/{id}.
    //   ACEPTACIÓN: un 404 llega como Error (no como excepción).
    suspend fun eliminarEvaluacion(evaluacionId: String): ApiResult<Unit> = TODO("E3-11: eliminar evaluación")

    // TODO(E3-11) [FuenteRemota.kt] Guardar notas: PUT /evaluaciones/{id}/calificaciones con armarNotasDto(notas, sinNota).
    //   ACEPTACIÓN: la nota 5,8 viaja como número 5.8 (no como texto) y los estudiantes sin nota en "sinNota".
    suspend fun guardarCalificaciones(evaluacionId: String, notas: Map<String, Double>, sinNota: List<String>): ApiResult<Unit> =
        TODO("E3-11: guardar calificaciones")

    // TODO(E3-11) [FuenteRemota.kt] Registrar una anotación: POST /anotaciones con CrearAnotacionRequest(tipo.name).
    //   ACEPTACIÓN: el cuerpo NO incluye firma ni fecha; no existe función para editar ni borrar (RN-06).
    suspend fun crearAnotacion(estudianteId: String, tipo: TipoAnotacion, descripcion: String): ApiResult<Unit> =
        TODO("E3-11: crear anotación")
}
