package com.librompaz.app.domain

import com.librompaz.app.data.MockData
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Evaluacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas compartidas por el ViewModel y el modo local (funciones puras de domain/ReglasDeEscritura.kt). */
class ReglasDeEscrituraTest {

    private val hoy = "2026-11-20"
    private val oaMat = MockData.objetivos.filter { it.asignaturaId == "MAT" }

    private fun sesion(fecha: String = "2026-11-19", bloque: String = "Bloque 1", oa: List<String> = listOf("OA-MAT-1"), contenido: String = "Sumas") =
        erroresDeSesionNueva(fecha, bloque, oa, contenido, hoy, oaMat, MockData.sesiones, "C3A", "MAT")

    @Test
    fun sesionNueva_valida_noTieneErrores() {
        assertFalse(sesion().hayErrores)
    }

    @Test
    fun sesionNueva_oaDeOtraAsignaturaYClaseRepetida_tienenSuMensaje() {
        assertEquals(MENSAJE_OA_AJENOS, sesion(oa = listOf("OA-LEN-1")).objetivos)
        // MockData ya tiene una clase en 3°A, Matemática, 2026-10-01, "Bloque 1" (sin distinguir mayúsculas).
        assertEquals(MENSAJE_SESION_DUPLICADA, sesion(fecha = "2026-10-01", bloque = "bloque 1").bloque)
        assertNull(sesion(fecha = "2026-10-01", bloque = "Bloque 2").bloque)
    }

    @Test
    fun sesionNueva_elPrimerMensajeSigueElOrdenDeLosCampos() {
        val errores = sesion(fecha = "", bloque = "", oa = emptyList(), contenido = "")
        assertEquals("La fecha es obligatoria", errores.primerMensaje())
        assertNull(sesion().primerMensaje())
    }

    @Test
    fun evaluacionNueva_repetidaEnLaMismaFechaEsError_peroNoEnOtraFecha() {
        val existentes = listOf(Evaluacion("EV9", "C3A", "MAT", "Control 1", "2026-11-18"))
        val repetida = erroresDeEvaluacionNueva(" control 1 ", "2026-11-18", hoy, existentes, "C3A", "MAT")
        assertEquals(MENSAJE_EVALUACION_DUPLICADA, repetida.nombre)
        assertFalse(erroresDeEvaluacionNueva("Control 1", "2026-11-19", hoy, existentes, "C3A", "MAT").hayErrores)
        assertEquals("La fecha no puede ser futura", erroresDeEvaluacionNueva("Control 2", "2026-11-21", hoy, existentes, "C3A", "MAT").primerMensaje())
    }

    @Test
    fun listaDeAsistencia_vaciaOConEstudiantesAjenos_esError() {
        val delCurso = setOf("E3A-1", "E3A-2")
        assertNull(errorListaAsistencia(delCurso, mapOf("E3A-1" to EstadoAsistencia.PRESENTE)))
        assertEquals(MENSAJE_LISTA_ASISTENCIA, errorListaAsistencia(delCurso, emptyMap()))
        assertEquals(MENSAJE_LISTA_ASISTENCIA, errorListaAsistencia(delCurso, mapOf("E4A-1" to EstadoAsistencia.PRESENTE)))
    }

    @Test
    fun notaNumerica_sigueLaMismaReglaQueLaNotaEscrita_RN04() {
        for (nota in listOf(1.0, 4.0, 5.8, 7.0)) assertNull("$nota", validarNotaNumerica(nota))
        assertEquals(MENSAJE_NOTA_FUERA_DE_RANGO, validarNotaNumerica(0.9))
        assertEquals(MENSAJE_NOTA_FUERA_DE_RANGO, validarNotaNumerica(7.1))
        assertEquals(MENSAJE_NOTA_DECIMALES, validarNotaNumerica(5.55))
        assertEquals(MENSAJE_NOTA_INVALIDA, validarNotaNumerica(Double.NaN))
        // Coincide con la validación del texto escrito para los mismos valores.
        for (texto in listOf("1,0", "5,8", "7,0", "0,9", "7,1", "5,55")) {
            val numero = parsearNota(texto)!!
            assertEquals(texto, validarNota(texto), validarNotaNumerica(numero))
        }
    }

    @Test
    fun primerMensajeDeAnotacion_eligeElPrimerCampoConError() {
        assertEquals("Elige un estudiante", validarAnotacion(false, true, "Descripción válida").primerMensaje())
        assertEquals("Escribe al menos 10 caracteres", validarAnotacion(true, true, "Corta").primerMensaje())
        assertTrue(validarAnotacion(true, true, "Descripción válida").primerMensaje() == null)
    }
}
