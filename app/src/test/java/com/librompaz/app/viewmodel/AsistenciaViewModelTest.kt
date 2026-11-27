
package com.librompaz.app.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.librompaz.app.data.MockData
import com.librompaz.app.data.repository.PreferenciaModoPersistente
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Perfil
import com.librompaz.app.testing.AlmacenModoFalso
import com.librompaz.app.testing.FuenteFalsa
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.model.RegistroAsistencia


@OptIn(ExperimentalCoroutinesApi::class)
class AsistenciaViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var fuente: FuenteFalsa
    private lateinit var viewModel: LibroMpazViewModel

    private val cursoId = "C3A"
    private val fechaValida = "2026-10-01"

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)

        val docente = MockData.usuarios.first {
            it.perfil == Perfil.DOCENTE
        }

        fuente = FuenteFalsa("local").apply {
            usuarioPrueba = docente
            cursosPrueba = MockData.cursos
            estudiantesPrueba = MockData.estudiantes

            cargasPrueba = listOf(
                CargaDocente(
                    "CD-TEST",
                    docente.id,
                    cursoId,
                    "MAT"
                )
            )
        }

        val modo = PreferenciaModoPersistente(
            almacen = AlmacenModoFalso(true),
            valorInicial = true
        )

        viewModel = LibroMpazViewModel(
            mockk<Application>(relaxed = true),
            modo,
            fuente
        )
    }

    @After
    fun limpiar() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }

        Dispatchers.resetMain()
    }

    private fun iniciarSesion() {
        viewModel.onUsuarioChange(fuente.usuarioPrueba.usuario)
        viewModel.onClaveChange("docente123")
        viewModel.ingresar {}
    }

    private fun estadosValidos(): Map<String, EstadoAsistencia> =
        fuente.estudiantesPrueba
            .filter { it.cursoId == cursoId }
            .associate { it.id to EstadoAsistencia.PRESENTE }

    @Test
    fun guardarAsistenciaValida_muestraExito() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)
        assertTrue(estadosValidos().isNotEmpty())

        var finalizado = false

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizado = true
        }

        advanceUntilIdle()

        assertTrue(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(finalizado)
        assertNotNull(viewModel.aviso.value)
        assertFalse(viewModel.aviso.value!!.esError)
        assertFalse(viewModel.guardando.value)
    }

    @Test
    fun guardarSinSesion_rechazaOperacion() = runTest {
        advanceUntilIdle()

        var finalizado = false

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizado = true
        }

        advanceUntilIdle()

        assertFalse(fuente.llamadas.contains("guardarAsistencia"))
        assertFalse(finalizado)
        assertTrue(viewModel.aviso.value?.esError == true)
    }

    @Test
    fun guardarConFechaFutura_rechazaOperacion() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)
        assertNotNull(viewModel.validarFechaAsistencia("2099-12-31"))

        viewModel.guardarAsistencia(
            cursoId,
            "2099-12-31",
            estadosValidos()
        ) {
            fail("No debería finalizar un guardado inválido")
        }

        advanceUntilIdle()

        assertFalse(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)
    }

    @Test
    fun guardarListaVacia_rechazaOperacion() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            emptyMap()
        ) {
            fail("No debería guardar una lista vacía")
        }

        advanceUntilIdle()

        assertFalse(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)
    }

    @Test
    fun guardarCursoNoAutorizado_rechazaOperacion() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        val otroCurso = fuente.cursosPrueba.first {
            it.id != cursoId
        }

        val estados = fuente.estudiantesPrueba
            .filter { it.cursoId == otroCurso.id }
            .associate { it.id to EstadoAsistencia.PRESENTE }

        assertNotNull(viewModel.usuarioActivo.value)
        assertTrue(estados.isNotEmpty())

        viewModel.guardarAsistencia(
            otroCurso.id,
            fechaValida,
            estados
        ) {
            fail("No debería guardar en un curso no autorizado")
        }

        advanceUntilIdle()

        assertFalse(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)
    }

    @Test
    fun guardarEstudianteAjeno_rechazaOperacion() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        val estudianteAjeno = fuente.estudiantesPrueba.first {
            it.cursoId != cursoId
        }

        val estados = estadosValidos() +
                (estudianteAjeno.id to EstadoAsistencia.AUSENTE)

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estados
        ) {
            fail("No debería guardar estudiantes ajenos al curso")
        }

        advanceUntilIdle()

        assertFalse(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)
    }

    @Test
    fun guardarFechaInexistente_rechazaOperacion() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        val fechaInvalida = "2026-02-30"

        assertNotNull(
            viewModel.validarFechaAsistencia(fechaInvalida)
        )

        viewModel.guardarAsistencia(
            cursoId,
            fechaInvalida,
            estadosValidos()
        ) {
            fail("No debería guardar una fecha inexistente")
        }

        advanceUntilIdle()

        assertFalse(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)
    }

    @Test
    fun guardarConErrorRepositorio_muestraError() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)

        fuente.resultadoEscritura = ApiResult.Error(
            mensaje = "Conflicto al guardar asistencia",
            codigo = 409
        )

        var finalizado = false

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizado = true
        }

        advanceUntilIdle()

        assertTrue(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)
        assertFalse(finalizado)
        assertFalse(viewModel.guardando.value)
        assertNotNull(viewModel.usuarioActivo.value)
    }

    @Test
    fun guardarSinConexion_actualizaEstadoSync() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)

        fuente.resultadoEscritura = ApiResult.SinConexion(
            mensaje = "Servidor no disponible"
        )

        var finalizado = false

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizado = true
        }

        advanceUntilIdle()

        assertTrue(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)

        assertTrue(
            viewModel.estadoSync.value is
                    EstadoSincronizacion.SinConexion
        )

        assertFalse(finalizado)
        assertFalse(viewModel.guardando.value)
        assertNotNull(viewModel.usuarioActivo.value)
    }

    @Test
    fun guardarConSesionExpirada_reiniciaSesion() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)

        fuente.resultadoEscritura = ApiResult.Error(
            mensaje = "Sesion expirada",
            codigo = 401
        )

        var finalizado = false

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizado = true
        }

        advanceUntilIdle()

        assertTrue(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(fuente.llamadas.contains("retirarToken"))
        assertNull(viewModel.usuarioActivo.value)
        assertNotNull(viewModel.login.value.errores.general)
        assertFalse(finalizado)
        assertFalse(viewModel.guardando.value)
    }

    @Test
    fun recuperarAsistenciaGuardada_devuelveEstadosCorrectos() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)

        val estudiante = fuente.estudiantesPrueba.first {
            it.cursoId == cursoId
        }

        val registro = RegistroAsistencia(
            id = "RA-TEST-1",
            estudianteId = estudiante.id,
            cursoId = cursoId,
            fecha = fechaValida,
            estado = EstadoAsistencia.AUSENTE,
            registradoPor = fuente.usuarioPrueba.id,
            fechaHoraRegistro = "${fechaValida}T08:00:00"
        )

        fuente.asistenciasPrueba = listOf(
            registro,
            registro.copy(
                id = "RA-TEST-2",
                fecha = "2026-10-02",
                estado = EstadoAsistencia.PRESENTE
            ),
            registro.copy(
                id = "RA-TEST-3",
                cursoId = "OTRO-CURSO",
                estado = EstadoAsistencia.ATRASADO
            )
        )

        val resultado = viewModel.asistenciaGuardada(
            cursoId,
            fechaValida
        )

        assertTrue(fuente.llamadas.contains("asistenciaDe"))

        assertEquals(
            mapOf(estudiante.id to EstadoAsistencia.AUSENTE),
            resultado
        )
    }

    @Test
    fun guardarConExcepcion_muestraErrorYLiberaGuardado() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)

        fuente.excepcionEscritura =
            IllegalStateException("Fallo simulado de persistencia")

        var finalizado = false

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizado = true
        }

        advanceUntilIdle()

        assertTrue(fuente.llamadas.contains("guardarAsistencia"))
        assertTrue(viewModel.aviso.value?.esError == true)
        assertFalse(finalizado)
        assertFalse(viewModel.guardando.value)
        assertNotNull(viewModel.usuarioActivo.value)
    }

    @Test
    fun guardarDosVeces_ejecutaUnaSolaOperacion() = runTest {
        iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.usuarioActivo.value)

        var finalizaciones = 0

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizaciones++
        }

        assertTrue(viewModel.guardando.value)

        viewModel.guardarAsistencia(
            cursoId,
            fechaValida,
            estadosValidos()
        ) {
            finalizaciones++
        }

        advanceUntilIdle()

        assertEquals(
            1,
            fuente.llamadas.count {
                it == "guardarAsistencia"
            }
        )

        assertEquals(1, finalizaciones)
        assertFalse(viewModel.guardando.value)
    }
}