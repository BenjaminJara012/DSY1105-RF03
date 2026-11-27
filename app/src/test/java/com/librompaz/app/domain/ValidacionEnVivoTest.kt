package com.librompaz.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Límites de las reglas que se muestran en vivo: anotación, bloque y contenidos. */
class ValidacionEnVivoTest {

    private val hoy = "2026-11-27"
    private val oa = listOf("OA-MAT-1")

    @Test
    fun anotacion_con9CaracteresTieneError_yCon10EsValida() {
        assertEquals("Escribe al menos 10 caracteres", validarAnotacion(true, true, "a".repeat(9)).descripcion)
        assertNull(validarAnotacion(true, true, "a".repeat(10)).descripcion)
    }

    @Test
    fun bloque_admiteHasta30Caracteres() {
        assertNull(validarSesion(hoy, "b".repeat(30), oa, "Contenido", hoy).bloque)
        assertEquals("Máximo 30 caracteres", validarSesion(hoy, "b".repeat(31), oa, "Contenido", hoy).bloque)
    }

    @Test
    fun contenido_admiteHasta500Caracteres() {
        assertNull(validarSesion(hoy, "Bloque 1", oa, "c".repeat(500), hoy).contenido)
        assertEquals("Máximo 500 caracteres", validarSesion(hoy, "Bloque 1", oa, "c".repeat(501), hoy).contenido)
    }

    @Test
    fun contenido_soloEspaciosEsObligatorio() {
        assertEquals("Describe los contenidos abordados", validarSesion(hoy, "Bloque 1", oa, "   ", hoy).contenido)
    }
}