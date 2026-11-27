package com.librompaz.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato Retrofit con la API local (ver backend/docs/API.md). Todas las funciones son
 * `suspend`: Retrofit las ejecuta fuera del hilo principal y, si el servidor responde con un
 * código distinto de 2xx, lanza HttpException (se traduce en ErroresApi.kt).
 *
 * Las rutas no llevan "/" inicial porque la URL base siempre termina en "/".
 * El token Bearer lo agrega TokenInterceptor; solo el cierre de sesión lo envía a mano.
 */
interface ApiService {

    // ---------- Estado y autenticación ----------

    @GET("health")
    suspend fun salud(): SaludDto

    @POST("auth/login")
    suspend fun login(@Body cuerpo: LoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") autorizacion: String): OkDto

    @GET("me")
    suspend fun yo(): UsuarioDto

    // ---------- Datos de referencia ----------

    @GET("cursos")
    suspend fun cursos(): List<CursoDto>

    @GET("asignaturas")
    suspend fun asignaturas(): List<AsignaturaDto>

    @GET("objetivos")
    suspend fun objetivos(@Query("asignaturaId") asignaturaId: String?): List<ObjetivoDto>

    @GET("estudiantes")
    suspend fun estudiantes(@Query("cursoId") cursoId: String?): List<EstudianteDto>

    @GET("cargas")
    suspend fun cargas(@Query("docenteId") docenteId: String?): List<CargaDocenteDto>

    // ---------- Asistencia (RF-03, RF-04, RF-05) ----------

    @GET("asistencias")
    suspend fun asistencias(
        @Query("cursoId") cursoId: String?,
        @Query("fecha") fecha: String?
    ): List<AsistenciaDto>

    @PUT("cursos/{cursoId}/asistencia/{fecha}")
    suspend fun guardarAsistencia(
        @Path("cursoId") cursoId: String,
        @Path("fecha") fecha: String,
        @Body cuerpo: GuardarAsistenciaRequest
    ): GuardarAsistenciaResponse

    // ---------- Clases con OA (RF-06, RF-07) ----------

    @GET("sesiones")
    suspend fun sesiones(
        @Query("cursoId") cursoId: String?,
        @Query("asignaturaId") asignaturaId: String?
    ): List<SesionDto>

    @POST("sesiones")
    suspend fun crearSesion(@Body cuerpo: CrearSesionRequest): SesionDto

    // ---------- Calificaciones (RF-08, RF-09) ----------

    @GET("evaluaciones")
    suspend fun evaluaciones(
        @Query("cursoId") cursoId: String?,
        @Query("asignaturaId") asignaturaId: String?
    ): List<EvaluacionDto>

    @POST("evaluaciones")
    suspend fun crearEvaluacion(@Body cuerpo: CrearEvaluacionRequest): EvaluacionDto

    @DELETE("evaluaciones/{id}")
    suspend fun eliminarEvaluacion(@Path("id") id: String): EliminacionDto

    @GET("calificaciones")
    suspend fun calificaciones(@Query("evaluacionId") evaluacionId: String?): List<CalificacionDto>

    @PUT("evaluaciones/{id}/calificaciones")
    suspend fun guardarCalificaciones(
        @Path("id") evaluacionId: String,
        @Body cuerpo: GuardarNotasRequest
    ): GuardarNotasResponse

    // ---------- Hoja de vida (RF-10, RF-11) ----------

    @GET("anotaciones")
    suspend fun anotaciones(
        @Query("estudianteId") estudianteId: String?,
        @Query("tipo") tipo: String?
    ): List<AnotacionDto>

    @POST("anotaciones")
    suspend fun crearAnotacion(@Body cuerpo: CrearAnotacionRequest): AnotacionDto

    // ---------- Auditoría (RF-13): solo lectura ----------

    @GET("auditoria")
    suspend fun auditoria(
        @Query("accion") accion: String?,
        @Query("perfil") perfil: String?,
        @Query("limite") limite: Int?
    ): List<AuditoriaDto>
}
