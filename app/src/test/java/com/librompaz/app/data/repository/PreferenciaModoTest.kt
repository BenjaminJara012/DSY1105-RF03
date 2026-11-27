package com.librompaz.app.data.repository

import com.librompaz.app.testing.AlmacenModoFalso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Preferencia persistente "modo local": valor inicial, persistencia y lectura en cada operación. */
class PreferenciaModoTest {

    @Test
    fun usarApi_soloDefineElValorInicial_falseEsModoLocalYTrueEsModoServidor() {
        assertTrue(modoLocalInicial(usarApi = false))
        assertFalse(modoLocalInicial(usarApi = true))
        assertTrue(PreferenciaModoPersistente(AlmacenModoFalso(), modoLocalInicial(usarApi = false)).modoLocal)
        assertFalse(PreferenciaModoPersistente(AlmacenModoFalso(), modoLocalInicial(usarApi = true)).modoLocal)
    }

    @Test
    fun loElegidoEnLaApp_ganaSobreUsarApi_enElSiguienteArranque() {
        val almacen = AlmacenModoFalso()
        PreferenciaModoPersistente(almacen, modoLocalInicial(usarApi = false)).modoLocal = false   // se pasó a servidor en la app
        // Aunque el build siga diciendo USAR_API=false, manda lo elegido.
        assertFalse(PreferenciaModoPersistente(almacen, modoLocalInicial(usarApi = false)).modoLocal)
    }

    @Test
    fun loElegidoEnLaApp_ganaSobreElValorInicial_ySobreviveAlReinicio() {
        val almacen = AlmacenModoFalso()
        PreferenciaModoPersistente(almacen, valorInicial = false).modoLocal = true
        // "Reinicio": una preferencia nueva sobre el mismo almacén (SharedPreferences) conserva lo elegido.
        val reiniciada = PreferenciaModoPersistente(almacen, valorInicial = false)
        assertTrue(reiniciada.modoLocal)
        assertTrue(reiniciada.cambios.value)
        reiniciada.modoLocal = false
        assertFalse(PreferenciaModoPersistente(almacen, valorInicial = true).modoLocal)
    }

    @Test
    fun cambios_emiteElValorVigenteYCadaCambio() {
        val preferencia = PreferenciaModoPersistente(AlmacenModoFalso(), valorInicial = false)
        assertFalse(preferencia.cambios.value)
        preferencia.modoLocal = true
        assertTrue(preferencia.cambios.value)
        preferencia.modoLocal = false
        assertFalse(preferencia.cambios.value)
    }

    @Test
    fun modoLocal_seLeeDelAlmacenEnCadaConsulta() {
        val almacen = AlmacenModoFalso()
        val preferencia = PreferenciaModoPersistente(almacen, valorInicial = false)
        val antes = almacen.lecturas
        preferencia.modoLocal
        preferencia.modoLocal
        assertEquals(antes + 2, almacen.lecturas)
        almacen.valor = true
        assertTrue(preferencia.modoLocal)
    }
}
