package com.librompaz.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.librompaz.app.BuildConfig
import com.librompaz.app.data.local.AlmacenModoPreferencias
import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.data.local.CacheRoom
import com.librompaz.app.data.remote.ApiClient
import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.data.remote.ConfigServidor
import com.librompaz.app.data.remote.FuenteRemota
import com.librompaz.app.data.remote.MENSAJE_SESION_EXPIRADA
import com.librompaz.app.data.remote.ServidorPreferencias
import com.librompaz.app.data.remote.TokenSesion
import com.librompaz.app.data.remote.mensajeDeFallo
import com.librompaz.app.data.remote.probarServidor
import com.librompaz.app.data.repository.LibroMpazApiRepository
import com.librompaz.app.data.repository.LibroMpazDatos
import com.librompaz.app.data.repository.LibroMpazRepository
import com.librompaz.app.data.repository.PreferenciaModo
import com.librompaz.app.data.repository.PreferenciaModoPersistente
import com.librompaz.app.data.repository.ResultadoLogin
import com.librompaz.app.data.repository.SelectorFuente
import com.librompaz.app.data.repository.modoLocalInicial
import com.librompaz.app.domain.Desfase
import com.librompaz.app.domain.ErroresAnotacion
import com.librompaz.app.domain.ErroresEvaluacion
import com.librompaz.app.domain.ErroresLogin
import com.librompaz.app.domain.ErroresSesion
import com.librompaz.app.domain.MENSAJE_EVALUACION_INEXISTENTE
import com.librompaz.app.domain.PoliticaAcceso
import com.librompaz.app.domain.calcularAnotacionesIntervencion
import com.librompaz.app.domain.calcularDesfases
import com.librompaz.app.domain.calcularInasistenciasReiteradas
import com.librompaz.app.domain.calcularOasTrabajados
import com.librompaz.app.domain.calcularPorcentajesPorCurso
import com.librompaz.app.domain.calcularResumenPorCurso
import com.librompaz.app.domain.errorListaAsistencia
import com.librompaz.app.domain.erroresDeEvaluacionNueva
import com.librompaz.app.domain.erroresDeSesionNueva
import com.librompaz.app.domain.parsearNota
import com.librompaz.app.domain.ultimaFechaPorOa
import com.librompaz.app.domain.validarAnotacion
import com.librompaz.app.domain.validarFecha
import com.librompaz.app.domain.validarLogin
import com.librompaz.app.domain.validarNotasDeEvaluacion
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.Perfil
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.ResumenCurso
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import com.librompaz.app.util.hayRedActiva
import com.librompaz.app.util.hoy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del formulario de login (data class + StateFlow en el ViewModel). */
data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val errores: ErroresLogin = ErroresLogin(),
    val ingresando: Boolean = false
)

/**
 * Mensaje de resultado para el usuario. [numero] es distinto en cada aviso, así dos avisos con el
 * mismo texto seguidos igual se vuelven a mostrar (y a vibrar).
 */
data class Aviso(val texto: String, val esError: Boolean, val numero: Long)

/** Estado de la sincronización con el servidor (se muestra en el banner inferior, con "Reintentar"). Solo en modo API. */
sealed interface EstadoSincronizacion {
    /** Sin sesión, o sincronizado: no se muestra nada. */
    data object AlDia : EstadoSincronizacion

    data object Sincronizando : EstadoSincronizacion

    /** El servidor no responde: se trabaja con la copia local de la última sincronización. */
    data class SinConexion(val mensaje: String) : EstadoSincronizacion

    /** El servidor respondió con un problema (no de red). */
    data class Fallo(val mensaje: String) : EstadoSincronizacion
}

/** Estado del cuadro "Servidor" del login: URL escrita, validación y resultado de "Probar conexión". */
data class ServidorUiState(
    val url: String = "",
    val error: String? = null,
    val probando: Boolean = false,
    val resultado: String? = null,
    val resultadoEsError: Boolean = false
)

/**
 * VIEWMODEL único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 * Guarda el estado de las pantallas y decide QUÉ hacer: aplica el control de acceso (RF-02), llama
 * a las reglas puras del paquete domain (validaciones y métricas) y delega el guardado en el
 * repositorio. No sabe CÓMO se guardan los datos: eso es trabajo de LibroMpazRepository.
 *
 * Flujo de un dato (el SelectorFuente elige el modo en cada operación, según la preferencia "modo local"):
 *   modo local:    Pantalla (@Composable) -> ViewModel (autoriza y valida) -> LibroMpazRepository -> DAO -> Room.
 *   modo servidor: Pantalla -> ViewModel -> LibroMpazApiRepository -> FuenteRemota -> API (Retrofit); si el servidor
 *   acepta, el repositorio vuelve a leer y reemplaza la copia local en Room.
 * Y de vuelta, en ambos modos: Room emite un Flow -> el ViewModel lo expone como StateFlow -> la pantalla lo
 * observa con collectAsState() y se redibuja sola.
 *
 * Es AndroidViewModel porque necesita un Context para abrir la base de datos.
 */
class LibroMpazViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Preferencia persistente "modo local" (el interruptor de la Experiencia 3 en tiempo de ejecución).
     * BuildConfig.USAR_API solo define el VALOR INICIAL (false = modo local, true = modo servidor);
     * después manda lo que se elija en el cuadro "Servidor". Se lee en cada operación (ver SelectorFuente).
     */
    private val modo: PreferenciaModo = PreferenciaModoPersistente(
        almacen = AlmacenModoPreferencias(application),
        valorInicial = modoLocalInicial(BuildConfig.USAR_API)
    )

    // Solo se usan en modo servidor (lazy): en modo local nunca se tocan, así que no hay cliente HTTP ni red.
    private val servidor by lazy { ServidorPreferencias(application) }
    private val tokenSesion by lazy { TokenSesion() }
    private val apiClient by lazy { ApiClient(tokenSesion) }

    // MODO LOCAL: solo Room, su propia base (independiente de la copia del servidor), sin red.
    private val repositorioLocal: LibroMpazDatos =
        LibroMpazRepository(AppDatabase.getInstance(application, AppDatabase.NOMBRE_LOCAL))

    // MODO SERVIDOR: la API es la fuente de verdad y Room (otra base) la caché. Construirlo no abre ninguna conexión.
    private val repositorioServidor: LibroMpazDatos = LibroMpazApiRepository(
        cache = CacheRoom(AppDatabase.getInstance(application)),
        remota = FuenteRemota { apiClient.servicio(servidor.urlActual()) },
        sesion = tokenSesion
    )

    // Lo único que ve el resto del ViewModel: elige la fuente según la preferencia en cada operación.
    private val repository: LibroMpazDatos = SelectorFuente(modo, repositorioLocal, repositorioServidor)

    /** true si se trabaja en modo servidor (se lee en el momento, no al arrancar). */
    private fun enModoServidor(): Boolean = !modo.modoLocal

    /**
     * Convierte un Flow de Room en StateFlow. Eagerly: empieza a escuchar de inmediato y no se
     * detiene, así .value siempre está al día (funciones como cursoPorId() lo leen). Es seguro
     * porque hay un solo ViewModel y la base es pequeña.
     */
    private fun <T> Flow<List<T>>.comoEstado(): StateFlow<List<T>> =
        stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // =====================================================================
    // Avisos y estado de "guardando"
    // =====================================================================

    private var contadorAvisos = 0L
    private val _aviso = MutableStateFlow<Aviso?>(null)

    /** Último resultado (éxito o error) de una operación; null cuando no hay nada que mostrar. */
    val aviso: StateFlow<Aviso?> = _aviso.asStateFlow()

    private val _guardando = MutableStateFlow(false)

    /** true mientras se escribe en la base (los botones de guardar se deshabilitan). */
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    fun descartarAviso() {
        _aviso.value = null
    }

    private fun avisar(texto: String, esError: Boolean = false) {
        contadorAvisos += 1
        _aviso.value = Aviso(texto, esError, contadorAvisos)
    }

    /** Si [mensaje] no es null la operación fue denegada: se avisa del error y devuelve true. */
    private fun denegado(mensaje: String?): Boolean {
        if (mensaje == null) return false
        avisar(mensaje, esError = true)
        return true
    }

    /**
     * Guarda en segundo plano (viewModelScope: el hilo principal queda libre, RF-04 y RNF-09).
     * La operación devuelve un [ApiResult]: si salió bien se avisa y se ejecuta [alTerminar]; si el
     * servidor la rechazó (regla de negocio, permiso) o no hay red, se muestra el mensaje y no se
     * guardó nada. Si ya hay un guardado en curso no hace nada. Si Room falla, avisa en vez de cerrar la app.
     */
    private fun persistir(mensajeExito: String, alTerminar: () -> Unit, operacion: suspend () -> ApiResult<Unit>) {
        if (_guardando.value) return
        _guardando.value = true
        viewModelScope.launch {
            try {
                when (val resultado = operacion()) {
                    is ApiResult.Exito -> {
                        avisar(mensajeExito)
                        alTerminar()
                    }
                    is ApiResult.Error ->
                        if (resultado.sesionExpirada) expirarSesion() else avisar(resultado.mensaje, esError = true)
                    is ApiResult.SinConexion -> {
                        avisar(resultado.mensaje, esError = true)
                        _estadoSync.value = EstadoSincronizacion.SinConexion(resultado.mensaje)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                avisar("No se pudo guardar. Intenta nuevamente.", esError = true)
            } finally {
                _guardando.value = false
            }
        }
    }

    // =====================================================================
    // Módulo "Autenticación y alcance por rol" (RF-01, RF-02)
    // =====================================================================

    /** false mientras se pobla la base la primera vez (el login muestra un indicador de carga). */
    private val _datosListos = MutableStateFlow(false)
    val datosListos: StateFlow<Boolean> = _datosListos.asStateFlow()

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    // Al escribir se limpia el error de ese campo.
    fun onUsuarioChange(valor: String) =
        _login.update { it.copy(usuario = valor, errores = it.errores.copy(usuario = null, general = null)) }

    fun onClaveChange(valor: String) =
        _login.update { it.copy(clave = valor, errores = it.errores.copy(clave = null, general = null)) }

    /**
     * Valida los campos y le pide al repositorio que ingrese: en modo local busca al usuario en Room
     * y compara el hash de la clave; en modo API usa POST /auth/login (y, sin conexión, solo acepta
     * credenciales ya usadas en este equipo). Si el ingreso es correcto, deja el usuario activo, en
     * modo API sincroniza los datos del perfil a Room y llama a [onIngresar] con su perfil:
     * AppNavigation lleva a cada perfil a su pantalla.
     */
    fun ingresar(onIngresar: (Perfil) -> Unit) {
        val estado = _login.value
        if (estado.ingresando) return
        val errores = validarLogin(estado.usuario, estado.clave)
        if (errores.hayErrores) {
            _login.update { it.copy(errores = errores) }
            return
        }
        viewModelScope.launch {
            _login.update { it.copy(ingresando = true) }
            val resultado = try {
                repository.ingresar(estado.usuario.trim(), estado.clave)
            } catch (e: CancellationException) {
                throw e
            } catch (e: NotImplementedError) {
                // Modo servidor elegido antes de completar los TODO(E3-n): se avisa qué falta en vez de cerrar la app.
                _login.update {
                    it.copy(
                        ingresando = false,
                        errores = ErroresLogin(general = "Falta completar la integración con el servidor (${e.message}). Usa el modo local en \"Servidor\".")
                    )
                }
                return@launch
            } catch (e: Exception) {
                _login.update {
                    it.copy(ingresando = false, errores = ErroresLogin(general = "No se pudo ingresar. Intenta nuevamente."))
                }
                return@launch
            }
            when (resultado) {
                is ResultadoLogin.Aceptado -> {
                    _usuarioActivo.value = resultado.usuario
                    _login.value = LoginUiState()   // limpia el formulario: la clave no queda en memoria
                    if (enModoServidor()) sincronizarAhora(resultado.usuario)
                    onIngresar(resultado.usuario.perfil)
                }
                is ResultadoLogin.SinConexion -> {
                    _usuarioActivo.value = resultado.usuario
                    _login.value = LoginUiState()
                    _estadoSync.value = EstadoSincronizacion.SinConexion(avisoDeRed(resultado.aviso))
                    onIngresar(resultado.usuario.perfil)
                }
                // Mensaje genérico a propósito: no revela si el correo existe ni qué perfiles hay (RN-01).
                is ResultadoLogin.Rechazado ->
                    _login.update { it.copy(ingresando = false, errores = ErroresLogin(general = resultado.mensaje)) }
                is ResultadoLogin.NoDisponible ->
                    _login.update {
                        it.copy(
                            ingresando = false,
                            errores = ErroresLogin(
                                general = avisoDeRed(resultado.mensaje) + " Si no hay servidor, activa el modo local en \"Servidor\"."
                            )
                        )
                    }
            }
        }
    }

    /** Cierra la sesión (en modo API también en el servidor, si responde) y borra todo estado de la persona. */
    fun logout() {
        val token = repository.retirarToken()
        viewModelScope.launch { repository.cerrarSesionRemota(token) }
        reiniciarSesionLocal()
    }

    private fun reiniciarSesionLocal() {
        _usuarioActivo.value = null
        _login.value = LoginUiState()
        _aviso.value = null
        _estadoSync.value = EstadoSincronizacion.AlDia
        seleccionarCurso(null)
    }

    /** 401 en una operación con sesión: el token venció (por ejemplo, se reinició el servidor). */
    private fun expirarSesion() {
        repository.retirarToken()
        reiniciarSesionLocal()
        _login.update { it.copy(errores = ErroresLogin(general = MENSAJE_SESION_EXPIRADA)) }
    }

    // =====================================================================
    // Sincronización con el servidor y cuadro "Servidor" (solo modo API)
    // =====================================================================

    private val _estadoSync = MutableStateFlow<EstadoSincronizacion>(EstadoSincronizacion.AlDia)

    /** Estado de la última sincronización; AppNavigation lo muestra en un banner con "Reintentar". */
    val estadoSync: StateFlow<EstadoSincronizacion> = _estadoSync.asStateFlow()

    /** Agrega una pista cuando el equipo ni siquiera tiene una red activa. */
    private fun avisoDeRed(mensaje: String): String =
        if (hayRedActiva(getApplication())) mensaje else "$mensaje El equipo no tiene una red activa (Wi-Fi o datos)."

    private suspend fun sincronizarAhora(usuario: Usuario) {
        if (_estadoSync.value == EstadoSincronizacion.Sincronizando) return
        _estadoSync.value = EstadoSincronizacion.Sincronizando
        _estadoSync.value = when (val r = repository.sincronizar(usuario)) {
            is ApiResult.Exito -> EstadoSincronizacion.AlDia
            is ApiResult.SinConexion -> EstadoSincronizacion.SinConexion(avisoDeRed(r.mensaje))
            is ApiResult.Error -> {
                if (r.sesionExpirada) {
                    expirarSesion()
                    EstadoSincronizacion.AlDia
                } else {
                    EstadoSincronizacion.Fallo(r.mensaje)
                }
            }
        }
    }

    /** "Reintentar" del banner: vuelve a descargar los datos del servidor. */
    fun sincronizar() {
        val usuario = _usuarioActivo.value ?: return
        viewModelScope.launch {
            try {
                sincronizarAhora(usuario)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estadoSync.value = EstadoSincronizacion.Fallo("No se pudo sincronizar. Intenta nuevamente.")
            }
        }
    }

    private val _servidor = MutableStateFlow(ServidorUiState())

    /** Estado del cuadro "Servidor" (URL escrita, error de validación, resultado de la prueba). */
    val servidorUi: StateFlow<ServidorUiState> = _servidor.asStateFlow()

    private val _servidorVigente = MutableStateFlow("")

    /** Dirección que la app está usando ahora (se muestra en el login). Vacía en modo local. */
    val servidorVigente: StateFlow<String> = _servidorVigente.asStateFlow()

    /**
     * URL del servidor, o "" si todavía no se completó TODO(E3-16) (así el cuadro "Servidor" y el cambio de
     * modo funcionan aunque falte esa parte, y se puede volver al modo local).
     */
    private fun urlVigenteOVacia(): String =
        try {
            servidor.urlActual()
        } catch (e: NotImplementedError) {
            ""
        }

    /** Al abrir el cuadro "Servidor" muestra la URL vigente (solo en modo servidor) y borra resultados anteriores. */
    fun abrirServidor() {
        _servidor.value = ServidorUiState(url = if (enModoServidor()) urlVigenteOVacia() else "")
    }

    fun onUrlServidorChange(valor: String) =
        _servidor.update { it.copy(url = valor, error = null, resultado = null) }

    // TODO(E3-17) [LibroMpazViewModel.kt] "Probar conexión" del cuadro Servidor.
    //   QUÉ: validar la URL con ConfigServidor.validarUrl; si es Invalida, dejar el mensaje en el campo (error) y no
    //        llamar a la red. Si es Valida, poner probando = true y, en viewModelScope, llamar a
    //        probarServidor(apiClient.servicio(url)) (captura excepciones; relanza CancellationException). Al terminar:
    //        Exito -> resultado = "Conexión correcta con <url>"; fallo -> resultado = r.mensajeDeFallo con resultadoEsError = true.
    //        NO guarda la URL.
    //   ACEPTACIÓN: "https://api.ejemplo.com" muestra el mensaje de red local bajo el campo y no hace ninguna petición;
    //        con el servidor encendido muestra "Conexión correcta"; apagado, el mensaje de sin conexión; el botón se
    //        deshabilita mientras prueba.
    fun probarConexion() {
        TODO("E3-17: probar la conexión con /health")
    }

    // TODO(E3-17) [LibroMpazViewModel.kt] Guardar la URL del servidor.
    //   QUÉ: validar con ConfigServidor.validarUrl; si es Invalida dejar el error en el campo y devolver false; si es
    //        Valida, guardarla con servidor.guardar(url normalizada), actualizar [_servidor] y [_servidorVigente] y devolver true.
    //   ACEPTACIÓN: una dirección pública no se guarda (RN-01, RNF-07); una local se guarda, se muestra en el login
    //        ("Servidor: ...") y se conserva al cerrar la app.
    fun guardarServidor(): Boolean = TODO("E3-17: guardar la URL del servidor")

    /** Vuelve a la dirección del build (por defecto, el emulador). */
    fun restablecerServidor() {
        servidor.restablecer()
        _servidor.value = ServidorUiState(url = servidor.urlActual())
        _servidorVigente.value = servidor.urlActual()
    }

    // =====================================================================
    // Modo local / modo servidor (interruptor en tiempo de ejecución)
    // =====================================================================

    /**
     * true = la app trabaja solo con la base de este equipo (modo local, sin servidor); false = modo
     * servidor. Lo muestran la franja inferior y el cuadro "Servidor".
     */
    val modoLocal: StateFlow<Boolean> = modo.cambios

    /**
     * Cambia de modo al instante (la preferencia se guarda y se lee en cada operación). Los datos de cada
     * modo son independientes: lo creado en modo local no se sincroniza con el servidor. Si había una
     * sesión abierta se cierra, porque el usuario y los datos pertenecen al modo anterior. El token del
     * servidor solo se olvida en memoria: no se hace ninguna llamada de red al cambiar de modo.
     */
    fun cambiarModoLocal(local: Boolean) {
        if (modo.modoLocal == local) return
        val habiaSesion = _usuarioActivo.value != null
        repository.retirarToken()   // por el modo vigente, antes de cambiarlo
        modo.modoLocal = local
        _servidorVigente.value = if (local) "" else urlVigenteOVacia()
        if (habiaSesion) reiniciarSesionLocal() else _login.update { it.copy(errores = ErroresLogin()) }
        _servidor.update {
            it.copy(
                url = if (local) "" else urlVigenteOVacia(),
                error = null,
                resultado = if (local) "Modo local activado: la app usa solo los datos de este equipo." else "Modo servidor activado.",
                resultadoEsError = false
            )
        }
    }

    /** Tras un "Probar conexión" fallido: pasa a modo local para poder seguir trabajando sin servidor. */
    fun usarModoLocal() = cambiarModoLocal(true)

    /** Borra lo creado en modo local y vuelve a cargar los datos de ejemplo. No toca lo del servidor. */
    fun restablecerDatosLocales() {
        viewModelScope.launch {
            val mensaje = try {
                repository.restablecerDatosLocales()
                "Datos locales restablecidos a los de ejemplo."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                "No se pudieron restablecer los datos locales."
            }
            _servidor.update { it.copy(resultado = mensaje, resultadoEsError = mensaje.startsWith("No se pudieron")) }
        }
    }

    /** Todas las cargas del colegio: solo para que PoliticaAcceso y las métricas decidan. */
    private val cargas: StateFlow<List<CargaDocente>> = repository.cargas.comoEstado()

    /**
     * RF-01 (RBAC): las cargas SOLO del docente autenticado. La consulta filtra por docenteId en
     * SQL (AcademicoDao.cargasDe). flatMapLatest cambia de consulta cada vez que cambia el usuario.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val misCargas: StateFlow<List<CargaDocente>> = _usuarioActivo
        .flatMapLatest { usuario ->
            if (usuario?.perfil == Perfil.DOCENTE) repository.cargasDe(usuario.id) else flowOf(emptyList())
        }
        .comoEstado()

    /** Usuario con sesión, o null avisando del error: ninguna función que guarda corre sin sesión. */
    private fun usuarioOAviso(): Usuario? {
        val usuario = _usuarioActivo.value
        if (usuario == null) avisar(PoliticaAcceso.MENSAJE_SIN_SESION, esError = true)
        return usuario
    }

    // =====================================================================
    // Datos de referencia y selección
    // =====================================================================

    val cursos: StateFlow<List<Curso>> = repository.cursos.comoEstado()
    val asignaturas: StateFlow<List<Asignatura>> = repository.asignaturas.comoEstado()
    val estudiantes: StateFlow<List<Estudiante>> = repository.estudiantes.comoEstado()
    val objetivos: StateFlow<List<ObjetivoAprendizaje>> = repository.objetivos.comoEstado()

    private val _cursoSeleccionadoId = MutableStateFlow<String?>(null)
    val cursoSeleccionadoId: StateFlow<String?> = _cursoSeleccionadoId.asStateFlow()

    private val _asignaturaSeleccionadaId = MutableStateFlow<String?>(null)
    val asignaturaSeleccionadaId: StateFlow<String?> = _asignaturaSeleccionadaId.asStateFlow()

    /** El curso y la asignatura elegidos se comparten entre pantallas a través del ViewModel. */
    fun seleccionarCurso(cursoId: String?, asignaturaId: String? = null) {
        _cursoSeleccionadoId.value = cursoId
        _asignaturaSeleccionadaId.value = asignaturaId
        _aviso.value = null
    }

    fun cursoPorId(id: String?): Curso? = cursos.value.find { it.id == id }
    fun asignaturaPorId(id: String?): Asignatura? = asignaturas.value.find { it.id == id }
    fun estudiantePorId(id: String?): Estudiante? = estudiantes.value.find { it.id == id }
    fun estudiantesDelCurso(cursoId: String?): List<Estudiante> = estudiantes.value.filter { it.cursoId == cursoId }
    fun objetivosDe(asignaturaId: String?): List<ObjetivoAprendizaje> = objetivos.value.filter { it.asignaturaId == asignaturaId }

    // =====================================================================
    // Módulo "Asistencia" (RF-03, RF-04, RF-05)
    // =====================================================================

    private val asistencias = repository.asistencias.comoEstado()

    /** RF-05: porcentaje de asistencia de cada curso (cursoId -> %); los cursos sin registros no aparecen. */
    val porcentajesAsistencia: StateFlow<Map<String, Int>> = asistencias
        .map { calcularPorcentajesPorCurso(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** RF-11 (RN-07): estudiantes con inasistencias reiteradas y su número de ausencias. */
    val alertasInasistencia: StateFlow<List<Pair<Estudiante, Int>>> =
        combine(asistencias, estudiantes) { registros, lista -> calcularInasistenciasReiteradas(registros, lista) }
            .comoEstado()

    /**
     * RF-03: asistencia ya guardada del curso en esa fecha (idEstudiante -> estado), para mostrarla
     * y poder corregirla. Vacía si el usuario no tiene permiso sobre ese curso.
     */
    suspend fun asistenciaGuardada(cursoId: String, fecha: String): Map<String, EstadoAsistencia> {
        if (PoliticaAcceso.verificarAsistencia(_usuarioActivo.value, cursoId, cargas.value) != null) return emptyMap()
        return repository.asistenciaDe(cursoId, fecha).associate { it.estudianteId to it.estado }
    }

    /** Error de la fecha de asistencia (formato AAAA-MM-DD, existente y no futura) o null. */
    fun validarFechaAsistencia(fecha: String): String? = validarFecha(fecha, hoy())

    /**
     * RF-03, RF-04 y RF-05: guarda (o reemplaza, RN-05) la asistencia del curso en esa fecha. Pueden
     * hacerlo el docente del curso o Inspectoría (cualquier curso); la auditoría la registra el
     * repositorio en la misma transacción. Si algo no corresponde, no guarda y avisa del error.
     */
    fun guardarAsistencia(
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        alTerminar: () -> Unit
    ) {
        val usuario = usuarioOAviso() ?: return
        if (denegado(PoliticaAcceso.verificarAsistencia(usuario, cursoId, cargas.value))) return
        val curso = cursoPorId(cursoId)
        if (curso == null) {
            avisar("El curso no existe.", esError = true)
            return
        }
        val errorFecha = validarFechaAsistencia(fecha)
        if (errorFecha != null) {
            avisar(errorFecha, esError = true)
            return
        }
        val delCurso = estudiantesDelCurso(cursoId).map { it.id }.toSet()
        val errorLista = errorListaAsistencia(delCurso, estados)
        if (errorLista != null) {
            avisar(errorLista, esError = true)
            return
        }
        val detalle = "Asistencia ${curso.nombre}, $fecha"
        persistir("Asistencia del $fecha guardada.", alTerminar) {
            repository.guardarAsistencia(usuario, cursoId, fecha, estados, detalle)
        }
    }

    // =====================================================================
    // Módulo "Cobertura curricular" (RF-06, RF-07)
    // =====================================================================

    val sesiones: StateFlow<List<SesionClase>> = repository.sesiones.comoEstado()

    fun sesionesDe(cursoId: String?, asignaturaId: String?): List<SesionClase> =
        sesiones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    /**
     * RF-06: valida y guarda la clase con sus OA y contenidos (RN-03: solo en las cargas del
     * docente). Devuelve los errores para mostrarlos junto a cada campo; los errores de permiso o
     * de guardado salen por [aviso].
     */
    fun guardarSesion(
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String,
        alTerminar: () -> Unit
    ): ErroresSesion {
        val usuario = usuarioOAviso() ?: return ErroresSesion()
        if (denegado(PoliticaAcceso.verificarCarga(usuario, cursoId, asignaturaId, cargas.value))) return ErroresSesion()

        val errores = erroresDeSesionNueva(
            fecha, bloque, oaIds, contenido, hoy(),
            objetivosDeLaAsignatura = objetivosDe(asignaturaId),
            existentes = sesiones.value,
            cursoId = cursoId,
            asignaturaId = asignaturaId
        )
        if (errores.hayErrores) return errores

        val detalle = "Sesión de clase ${cursoPorId(cursoId)?.nombre}, ${asignaturaPorId(asignaturaId)?.nombre}, " +
            "$fecha ${bloque.trim()}"
        persistir("Clase del $fecha registrada.", alTerminar) {
            repository.guardarSesion(usuario, cursoId, asignaturaId, fecha, bloque.trim(), oaIds, contenido.trim(), detalle)
        }
        return errores
    }

    /** RF-07: ids de los OA ya trabajados en el curso y asignatura (vista de UTP, solo lectura). */
    fun oasTrabajados(cursoId: String?, asignaturaId: String?): Set<String> =
        calcularOasTrabajados(sesiones.value, cursoId, asignaturaId)

    /** RF-07 (opcional): fecha de la última clase en que se trabajó cada OA (oaId -> AAAA-MM-DD). */
    fun ultimaFechaOa(cursoId: String?, asignaturaId: String?): Map<String, String> =
        ultimaFechaPorOa(sesiones.value, cursoId, asignaturaId)

    // =====================================================================
    // Módulo "Calificaciones" (RF-08, RF-09)
    // =====================================================================

    val evaluaciones: StateFlow<List<Evaluacion>> = repository.evaluaciones.comoEstado()
    val calificaciones: StateFlow<List<Calificacion>> = repository.calificaciones.comoEstado()

    fun evaluacionesDe(cursoId: String?, asignaturaId: String?): List<Evaluacion> =
        evaluaciones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    fun calificacionesDe(evaluacionId: String?): List<Calificacion> =
        calificaciones.value.filter { it.evaluacionId == evaluacionId }

    /** RF-08: crea una evaluación (nombre obligatorio, fecha válida, sin repetir nombre en la misma fecha). */
    fun crearEvaluacion(
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String,
        alTerminar: () -> Unit
    ): ErroresEvaluacion {
        val usuario = usuarioOAviso() ?: return ErroresEvaluacion()
        if (denegado(PoliticaAcceso.verificarCarga(usuario, cursoId, asignaturaId, cargas.value))) return ErroresEvaluacion()

        val errores = erroresDeEvaluacionNueva(
            nombreInstrumento, fecha, hoy(), evaluaciones.value, cursoId, asignaturaId
        )
        if (errores.hayErrores) return errores

        val nombre = nombreInstrumento.trim()
        val detalle = "Evaluación \"$nombre\" ${cursoPorId(cursoId)?.nombre}, ${asignaturaPorId(asignaturaId)?.nombre}, $fecha"
        persistir("Evaluación \"$nombre\" creada. Elígela para ingresar las notas.", alTerminar) {
            repository.crearEvaluacion(usuario, cursoId, asignaturaId, nombre, fecha, detalle)
        }
        return errores
    }

    /** RN-04: error de una nota escrita (o null si es válida). La regla está en domain/Validaciones.kt. */
    fun validarNota(texto: String): String? = com.librompaz.app.domain.validarNota(texto)

    /**
     * RF-08: guarda las notas escritas (idEstudiante -> texto, con coma o punto). No guarda nada si
     * alguna nota tiene error (RN-04) o si algún estudiante no es del curso de la evaluación
     * (RN-08). Crea o reemplaza una nota por estudiante; una nota que existía y se dejó en blanco
     * se elimina. La evaluación se toma de la base, no de lo que envíe la pantalla.
     */
    fun guardarCalificaciones(evaluacionId: String, notas: Map<String, String>, alTerminar: () -> Unit) {
        val usuario = usuarioOAviso() ?: return
        val evaluacion = evaluaciones.value.find { it.id == evaluacionId }
        if (evaluacion == null) {
            avisar(MENSAJE_EVALUACION_INEXISTENTE, esError = true)
            return
        }
        if (denegado(PoliticaAcceso.verificarCarga(usuario, evaluacion.cursoId, evaluacion.asignaturaId, cargas.value))) return

        val delCurso = estudiantesDelCurso(evaluacion.cursoId).map { it.id }.toSet()
        val errores = validarNotasDeEvaluacion(evaluacion, delCurso, notas)
        if (errores.hayErrores) {
            avisar(errores.general ?: "Revisa las notas marcadas: no se guardó nada.", esError = true)
            return
        }

        val validas = notas.mapNotNull { (estudianteId, texto) -> parsearNota(texto)?.let { estudianteId to it } }.toMap()
        val sinNota = calificacionesDe(evaluacionId).map { it.estudianteId }.filter { it !in validas }
        if (validas.isEmpty() && sinNota.isEmpty()) {
            avisar("Escribe al menos una nota.", esError = true)
            return
        }

        val detalle = "Notas de \"${evaluacion.nombreInstrumento}\" ${cursoPorId(evaluacion.cursoId)?.nombre}, " +
            "${asignaturaPorId(evaluacion.asignaturaId)?.nombre}, ${evaluacion.fecha} (${validas.size} notas)"
        persistir("Notas guardadas correctamente.", alTerminar) {
            repository.guardarCalificaciones(usuario, evaluacionId, validas, sinNota, detalle)
        }
    }

    /** Elimina la evaluación y sus notas (la pantalla pide confirmación antes, RNF-03). Queda auditado. */
    fun eliminarEvaluacion(evaluacionId: String, alTerminar: () -> Unit) {
        val usuario = usuarioOAviso() ?: return
        val evaluacion = evaluaciones.value.find { it.id == evaluacionId }
        if (evaluacion == null) {
            avisar(MENSAJE_EVALUACION_INEXISTENTE, esError = true)
            return
        }
        if (denegado(PoliticaAcceso.verificarCarga(usuario, evaluacion.cursoId, evaluacion.asignaturaId, cargas.value))) return
        val detalle = "Evaluación \"${evaluacion.nombreInstrumento}\" ${cursoPorId(evaluacion.cursoId)?.nombre}, " +
            "${asignaturaPorId(evaluacion.asignaturaId)?.nombre}, ${evaluacion.fecha} y sus notas"
        persistir("Evaluación eliminada.", alTerminar) {
            repository.eliminarEvaluacion(usuario, evaluacion, detalle)
        }
    }

    /** RF-09: desfases u omisiones en el registro de notas, para UTP (solo lectura). */
    val desfases: StateFlow<List<Desfase>> = combine(
        cargas,
        evaluaciones,
        calificaciones,
        estudiantes,
        combine(cursos, asignaturas) { c, a -> c to a }
    ) { listaCargas, listaEvaluaciones, listaNotas, listaEstudiantes, (listaCursos, listaAsignaturas) ->
        calcularDesfases(
            listaCargas, listaEvaluaciones, listaNotas, listaEstudiantes, listaCursos, listaAsignaturas, hoy()
        )
    }.comoEstado()

    // =====================================================================
    // Módulo "Hoja de vida" (RF-10, RF-11)
    // =====================================================================

    val anotaciones: StateFlow<List<Anotacion>> = repository.anotaciones.comoEstado()

    fun anotacionesDe(estudianteId: String?): List<Anotacion> =
        anotaciones.value.filter { it.estudianteId == estudianteId }

    /** Hoja de vida del colegio filtrada por tipo (null = todas), para Inspectoría. */
    fun anotacionesPorTipo(tipo: TipoAnotacion?): List<Anotacion> =
        anotaciones.value.filter { tipo == null || it.tipo == tipo }

    fun validarNuevaAnotacion(estudiante: Estudiante?, tipo: TipoAnotacion?, descripcion: String): ErroresAnotacion =
        validarAnotacion(estudiante != null, tipo != null, descripcion)

    /**
     * RF-10: registra la anotación con marca temporal y firma (usuario activo + fecha y hora del
     * sistema). Solo el docente, y solo para estudiantes de un curso de sus cargas. Una vez
     * guardada no se puede editar ni eliminar (RN-06): no existen funciones para hacerlo.
     */
    fun registrarAnotacion(estudianteId: String, tipo: TipoAnotacion, descripcion: String, alTerminar: () -> Unit) {
        val usuario = usuarioOAviso() ?: return
        val estudiante = estudiantePorId(estudianteId)
        if (estudiante == null) {
            avisar("El estudiante no existe.", esError = true)
            return
        }
        if (denegado(PoliticaAcceso.verificarAnotacion(usuario, estudiante.cursoId, cargas.value))) return
        if (validarNuevaAnotacion(estudiante, tipo, descripcion).hayErrores) {
            avisar("Revisa la descripción de la anotación.", esError = true)
            return
        }
        val detalle = "${tipo.etiqueta} a ${estudiante.nombreFicticio}, ${cursoPorId(estudiante.cursoId)?.nombre}"
        persistir("Anotación registrada y firmada.", alTerminar) {
            repository.registrarAnotacion(usuario, estudiante.id, tipo, descripcion.trim(), detalle)
        }
    }

    /** RF-11 (RN-07): anotaciones negativas de estudiantes que requieren intervención de Inspectoría. */
    val alertasAnotaciones: StateFlow<List<Anotacion>> = anotaciones
        .map { calcularAnotacionesIntervencion(it) }
        .comoEstado()

    // =====================================================================
    // Módulo "Panel administrativo" (Dirección, RF-12) y auditoría (RF-13, RN-02)
    // =====================================================================

    /** RF-13: registro de auditoría, del más reciente al más antiguo. Es de solo lectura. */
    val auditoria: StateFlow<List<RegistroAuditoria>> = repository.auditoria.comoEstado()

    /** RF-12: métricas consolidadas por curso. Se recalculan solas cuando cambia cualquier tabla. */
    val resumenCursos: StateFlow<List<ResumenCurso>> = combine(
        combine(cursos, cargas, estudiantes) { c, k, e -> Triple(c, k, e) },
        combine(asistencias, sesiones, anotaciones) { a, s, n -> Triple(a, s, n) },
        combine(evaluaciones, calificaciones) { e, c -> e to c }
    ) { (listaCursos, listaCargas, listaEstudiantes), (listaAsistencias, listaSesiones, listaAnotaciones), (listaEvaluaciones, listaNotas) ->
        calcularResumenPorCurso(
            listaCursos, listaCargas, listaAsistencias, listaSesiones,
            listaEvaluaciones, listaNotas, listaAnotaciones, listaEstudiantes, hoy()
        )
    }.comoEstado()

    // =====================================================================
    // Arranque
    // =====================================================================

    init {
        if (enModoServidor()) _servidorVigente.value = urlVigenteOVacia()
        viewModelScope.launch {
            try {
                repository.poblarSiVacia()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _login.update { it.copy(errores = ErroresLogin(general = "No se pudo preparar la base de datos.")) }
            }
            _datosListos.value = true
        }
    }
}
