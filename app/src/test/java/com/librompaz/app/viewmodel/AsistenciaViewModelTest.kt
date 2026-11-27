
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
}
