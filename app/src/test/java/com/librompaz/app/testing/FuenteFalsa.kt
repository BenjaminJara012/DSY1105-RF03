package com.librompaz.app.testing

import com.librompaz.app.data.MockData
import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.data.repository.AlmacenModo
import com.librompaz.app.data.repository.LibroMpazDatos
import com.librompaz.app.data.repository.ResultadoLogin
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
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** [AlmacenModo] en memoria (hace de SharedPreferences en las pruebas). */
class AlmacenModoFalso(var valor: Boolean? = null) : AlmacenModo {
    var lecturas = 0
    override fun leer(): Boolean? {
        lecturas += 1
        return valor
    }

    override fun guardar(valor: Boolean) {
        this.valor = valor
    }
}

/**
 * [LibroMpazDatos] de pruebas: anota cada operación en [llamadas] y se distingue por el nombre del curso
 * que publica ([nombre]). Con [prohibida] cualquier operación falla: sirve para demostrar que una
 * fuente NO se usó (por ejemplo, que el modo local no toca el servidor).
 */
class FuenteFalsa(val nombre: String, private val prohibida: Boolean = false) : LibroMpazDatos {

    val llamadas = mutableListOf<String>()
    var resultadoEscritura: ApiResult<Unit> = ApiResult.Exito(Unit)

    private fun anotar(operacion: String) {
        if (prohibida) throw AssertionError("la fuente \"$nombre\" no debía usarse (operación: $operacion)")
        llamadas += operacion
    }

    private fun <T> flujo(valor: T): Flow<T> {
        if (prohibida) throw AssertionError("la fuente \"$nombre\" no debía observarse")
        return MutableStateFlow(valor)
    }

    override val cursos: Flow<List<Curso>> get() = flujo(listOf(Curso("C-$nombre", nombre)))
    override val asignaturas: Flow<List<Asignatura>> get() = flujo(emptyList())
    override val estudiantes: Flow<List<Estudiante>> get() = flujo(emptyList())
    override val objetivos: Flow<List<ObjetivoAprendizaje>> get() = flujo(emptyList())
    override val cargas: Flow<List<CargaDocente>> get() = flujo(emptyList())
    override val asistencias: Flow<List<RegistroAsistencia>> get() = flujo(emptyList())
    override val sesiones: Flow<List<SesionClase>> get() = flujo(emptyList())
    override val evaluaciones: Flow<List<Evaluacion>> get() = flujo(emptyList())
    override val calificaciones: Flow<List<Calificacion>> get() = flujo(emptyList())
    override val anotaciones: Flow<List<Anotacion>> get() = flujo(emptyList())
    override val auditoria: Flow<List<RegistroAuditoria>> get() = flujo(emptyList())

    override fun cargasDe(docenteId: String): Flow<List<CargaDocente>> =
        flujo(listOf(CargaDocente("CD-$nombre", docenteId, "C-$nombre", "MAT")))

    override suspend fun poblarSiVacia() = anotar("poblarSiVacia")

    override suspend fun ingresar(correo: String, clave: String): ResultadoLogin {
        anotar("ingresar")
        val usuario = MockData.usuarios.first()
        return if (nombre == "local") ResultadoLogin.Aceptado(usuario) else ResultadoLogin.SinConexion(usuario, "sin servidor")
    }

    override fun retirarToken(): String? {
        anotar("retirarToken")
        return "token-$nombre"
    }

    override suspend fun cerrarSesionRemota(token: String?) = anotar("cerrarSesionRemota")

    override suspend fun sincronizar(usuario: Usuario): ApiResult<Unit> {
        anotar("sincronizar")
        return ApiResult.Exito(Unit)
    }

    override suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia> {
        anotar("asistenciaDe")
        return emptyList()
    }

    override suspend fun guardarAsistencia(
        usuario: Usuario,
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        detalle: String
    ): ApiResult<Unit> {
        anotar("guardarAsistencia")
        return resultadoEscritura
    }

    override suspend fun guardarSesion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String,
        detalle: String
    ): ApiResult<Unit> {
        anotar("guardarSesion")
        return resultadoEscritura
    }

    override suspend fun crearEvaluacion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String,
        detalle: String
    ): ApiResult<Unit> {
        anotar("crearEvaluacion")
        return resultadoEscritura
    }

    override suspend fun eliminarEvaluacion(usuario: Usuario, evaluacion: Evaluacion, detalle: String): ApiResult<Unit> {
        anotar("eliminarEvaluacion")
        return resultadoEscritura
    }

    override suspend fun guardarCalificaciones(
        usuario: Usuario,
        evaluacionId: String,
        notas: Map<String, Double>,
        sinNota: List<String>,
        detalle: String
    ): ApiResult<Unit> {
        anotar("guardarCalificaciones")
        return resultadoEscritura
    }

    override suspend fun registrarAnotacion(
        usuario: Usuario,
        estudianteId: String,
        tipo: TipoAnotacion,
        descripcion: String,
        detalle: String
    ): ApiResult<Unit> {
        anotar("registrarAnotacion")
        return resultadoEscritura
    }

    override suspend fun restablecerDatosLocales() = anotar("restablecerDatosLocales")
}
