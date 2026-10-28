package com.librompaz.app

import com.librompaz.app.util.hoy
import com.librompaz.app.util.validarFechaAsistencia
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para las reglas de fecha
 * del registro de asistencia RF-03.
 */
class FechasTest {

    @Test
    fun fechaActualEsValida() {
        // Debe permitir registrar asistencia del día actual.
        assertTrue(validarFechaAsistencia(hoy()))
    }

    @Test
    fun fechaPasadaEsValida() {
        // Una fecha histórica válida debe ser aceptada.
        assertTrue(validarFechaAsistencia("2020-01-15"))
    }

    @Test
    fun fechaInexistenteEsRechazada() {
        // Febrero nunca tiene 30 días.
        assertFalse(validarFechaAsistencia("2026-02-30"))
    }

    @Test
    fun formatoIncorrectoEsRechazado() {
        // El formato permitido es yyyy-MM-dd.
        assertFalse(validarFechaAsistencia("27-10-2026"))
    }

    @Test
    fun fechaVaciaEsRechazada() {
        assertFalse(validarFechaAsistencia(""))
    }

    @Test
    fun fechaFuturaEsRechazada() {
        // Esta fecha es posterior al plazo del proyecto.
        assertFalse(validarFechaAsistencia("2099-12-31"))
    }
}
