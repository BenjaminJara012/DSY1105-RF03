package com.librompaz.app.data.repository

import com.librompaz.app.data.MockData
import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.testing.AlmacenModoFalso
import com.librompaz.app.testing.FuenteFalsa
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Selector de fuente: elige modo local o servidor según la preferencia, la lee en cada operación y
 * reconecta las lecturas observables cuando cambia. En modo local nunca toca la fuente del servidor.
 */
class SelectorFuenteTest {

    private val usuario = MockData.usuarios.first()

    private class Entorno(modoLocal: Boolean, servidorProhibido: Boolean = false) {
        val preferencia = PreferenciaModoPersistente(AlmacenModoFalso(modoLocal), valorInicial = modoLocalInicial(usarApi = false))
        val local = FuenteFalsa("local")
        val servidor = FuenteFalsa("servidor", prohibida = servidorProhibido)
        val selector = SelectorFuente(preferencia, local, servidor)
    }

    @Test
    fun conModoLocal_lasOperacionesVanAlLocalYNoAlServidor() = runBlocking {
        val e = Entorno(modoLocal = true)
        e.selector.guardarAsistencia(usuario, "C3A", "2026-11-19", mapOf("E3A-1" to EstadoAsistencia.PRESENTE), "d")
        e.selector.crearEvaluacion(usuario, "C3A", "MAT", "Control", "2026-11-19", "d")
        assertEquals(listOf("guardarAsistencia", "crearEvaluacion"), e.local.llamadas)
        assertTrue(e.servidor.llamadas.isEmpty())
    }

    @Test
    fun conModoServidor_lasOperacionesVanAlServidorYNoAlLocal() = runBlocking {
        val e = Entorno(modoLocal = false)
        e.selector.guardarSesion(usuario, "C3A", "MAT", "2026-11-19", "Bloque 1", listOf("OA-MAT-1"), "Contenido", "d")
        e.selector.registrarAnotacion(usuario, "E3A-1", TipoAnotacion.POSITIVA, "Muy buena participación.", "d")
        assertEquals(listOf("guardarSesion", "registrarAnotacion"), e.servidor.llamadas)
        assertTrue(e.local.llamadas.isEmpty())
    }

    @Test
    fun elCambioDeModoEnCaliente_valeDesdeLaOperacionSiguiente() = runBlocking {
        val e = Entorno(modoLocal = false)
        val delServidor = e.selector.ingresar("docente.mat@libromp.cl", "docente123")
        assertTrue(delServidor is ResultadoLogin.SinConexion)

        e.preferencia.modoLocal = true          // sin reiniciar nada
        val local = e.selector.ingresar("docente.mat@libromp.cl", "docente123")
        assertTrue(local is ResultadoLogin.Aceptado)

        e.preferencia.modoLocal = false
        assertTrue(e.selector.ingresar("docente.mat@libromp.cl", "docente123") is ResultadoLogin.SinConexion)
        assertEquals(listOf("ingresar", "ingresar"), e.servidor.llamadas)
        assertEquals(listOf("ingresar"), e.local.llamadas)
    }

    @Test
    fun laPreferenciaSeLeeEnCadaOperacion_inclusoSiCambiaPorFueraDelSelector() = runBlocking {
        val almacen = AlmacenModoFalso(false)
        val preferencia = PreferenciaModoPersistente(almacen, valorInicial = false)
        val local = FuenteFalsa("local")
        val servidor = FuenteFalsa("servidor")
        val selector = SelectorFuente(preferencia, local, servidor)
        selector.sincronizar(usuario)
        val lecturasAntes = almacen.lecturas
        almacen.valor = true                    // otra parte de la app cambió el valor guardado
        selector.sincronizar(usuario)
        assertTrue(almacen.lecturas > lecturasAntes)
        assertEquals(listOf("sincronizar"), servidor.llamadas)
        assertEquals(listOf("sincronizar"), local.llamadas)
    }

    @Test
    fun enModoLocal_ningunaOperacionNiLecturaTocaAlServidor() = runBlocking {
        val e = Entorno(modoLocal = true, servidorProhibido = true)
        val s = e.selector
        s.ingresar("docente.mat@libromp.cl", "docente123")
        s.retirarToken()
        s.cerrarSesionRemota("t")
        s.sincronizar(usuario)
        s.asistenciaDe("C3A", "2026-11-19")
        s.guardarAsistencia(usuario, "C3A", "2026-11-19", mapOf("E3A-1" to EstadoAsistencia.PRESENTE), "d")
        s.guardarSesion(usuario, "C3A", "MAT", "2026-11-19", "Bloque 1", listOf("OA-MAT-1"), "C", "d")
        s.crearEvaluacion(usuario, "C3A", "MAT", "Control", "2026-11-19", "d")
        s.eliminarEvaluacion(usuario, MockData.evaluaciones.first(), "d")
        s.guardarCalificaciones(usuario, "EV1", mapOf("E3A-1" to 5.0), emptyList(), "d")
        s.registrarAnotacion(usuario, "E3A-1", TipoAnotacion.POSITIVA, "Muy buena participación.", "d")
        s.restablecerDatosLocales()
        assertEquals("local", s.cursos.first().single().nombre)
        assertEquals("local", s.cargasDe("U1").first().single().id.removePrefix("CD-"))
        assertTrue(e.servidor.llamadas.isEmpty())   // y si lo hubiera tocado, FuenteFalsa habría lanzado AssertionError
    }

    @Test
    fun lasLecturasObservables_sigueAlModoEnCaliente() = runBlocking {
        val e = Entorno(modoLocal = true)
        assertEquals("local", e.selector.cursos.first().single().nombre)
        e.preferencia.modoLocal = false
        assertEquals("servidor", e.selector.cursos.first().single().nombre)

        // Un observador que ya estaba suscrito se reconecta solo a la otra fuente.
        val vistos = mutableListOf<String>()
        val trabajo = launch { e.selector.cursos.collect { vistos += it.single().nombre } }
        repeat(5) { yield() }
        e.preferencia.modoLocal = true
        repeat(5) { yield() }
        e.preferencia.modoLocal = false
        repeat(5) { yield() }
        trabajo.cancel()
        assertEquals(listOf("servidor", "local", "servidor"), vistos)
    }

    @Test
    fun poblarSiVacia_preparaLasDosBases_yRestablecerSoloLaLocal() = runBlocking {
        val e = Entorno(modoLocal = false)
        e.selector.poblarSiVacia()
        assertEquals(listOf("poblarSiVacia"), e.local.llamadas)
        assertEquals(listOf("poblarSiVacia"), e.servidor.llamadas)

        e.selector.restablecerDatosLocales()    // aunque el modo sea servidor, solo toca lo local
        assertEquals(listOf("poblarSiVacia", "restablecerDatosLocales"), e.local.llamadas)
        assertEquals(listOf("poblarSiVacia"), e.servidor.llamadas)
    }

    @Test
    fun unRechazoDelLocal_llegaTalCual_yElServidorNoParticipa() = runBlocking {
        val e = Entorno(modoLocal = true, servidorProhibido = true)
        e.local.resultadoEscritura = ApiResult.Error("No tienes asignado este curso o asignatura.", 403)
        val r = e.selector.guardarSesion(usuario, "C3A", "MAT", "2026-11-19", "Bloque 1", listOf("OA-MAT-1"), "C", "d")
        assertEquals(ApiResult.Error("No tienes asignado este curso o asignatura.", 403), r)
    }
}
