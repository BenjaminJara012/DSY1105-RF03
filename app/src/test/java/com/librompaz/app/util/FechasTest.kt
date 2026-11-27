package com.librompaz.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FechasTest {

    @Test
    fun diasEntre_cuentaDiasEnterosEntreDosFechas() {
        assertEquals(0L, diasEntre("2026-10-01", "2026-10-01"))
        assertEquals(10L, diasEntre("2026-09-25", "2026-10-05"))
        assertEquals(34L, diasEntre("2026-09-25", "2026-10-29"))
        assertEquals(-3L, diasEntre("2026-10-05", "2026-10-02"))
    }

    @Test
    fun diasEntre_fechaInvalida_esNull() {
        assertNull(diasEntre("2026-02-31", "2026-10-01"))
        assertNull(diasEntre("texto", "2026-10-01"))
    }

    @Test
    fun restarDias_cruzaMesesYAnios() {
        assertEquals("2026-09-28", restarDias("2026-10-05", 7))
        assertEquals("2025-12-31", restarDias("2026-01-03", 3))
        assertNull(restarDias("no es fecha", 7))
    }

    @Test
    fun fechaMillis_ida_y_vuelta() {
        val millis = millisDesdeFecha("2026-10-01")
        assertNotNull(millis)
        assertEquals("2026-10-01", fechaDesdeMillis(millis!!))
        assertNull(millisDesdeFecha("2026-02-31"))
    }

    @Test
    fun hoyYAhora_tienenElFormatoEsperado() {
        assertTrue(Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(hoy()))
        assertTrue(Regex("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$").matches(ahora()))
        assertTrue(ahora().startsWith(hoy()))
    }
}
