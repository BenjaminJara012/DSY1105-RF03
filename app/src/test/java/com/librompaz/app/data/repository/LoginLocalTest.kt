package com.librompaz.app.data.repository

import com.librompaz.app.data.MockData
import com.librompaz.app.domain.ClaveSegura
import com.librompaz.app.model.Perfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ingreso en MODO LOCAL (RF-01, RN-01, RNF-08): la decisión la toma la función pura verificarCredenciales,
 * que usa el LibroMpazRepository local con el usuario que encuentra en Room. Mismo hash y misma sal que el servidor.
 */
class LoginLocalTest {

    private fun usuario(correo: String) = MockData.usuarios.first { it.usuario == correo }

    @Test
    fun credencialesCorrectas_sonAceptadas_ConElMismoHashDelServidor() {
        val docente = usuario("docente.mat@libromp.cl")
        val r = verificarCredenciales(docente, "docente123")
        assertEquals(ResultadoLogin.Aceptado(docente), r)
        assertEquals(ClaveSegura.hash("docente123"), (r as ResultadoLogin.Aceptado).usuario.claveHash)
    }

    @Test
    fun claveIncorrectaYUsuarioInexistente_danElMismoMensajeGenerico() {
        val mala = verificarCredenciales(usuario("docente.mat@libromp.cl"), "otra")
        val inexistente = verificarCredenciales(null, "docente123")
        assertEquals(ResultadoLogin.Rechazado("Correo o clave incorrectos"), mala)
        assertEquals(mala, inexistente)   // no revela si el correo existe (RN-01)
    }

    @Test
    fun entranLosCincoPerfilesConSusClaves() {
        val claves = mapOf(
            "docente.mat@libromp.cl" to "docente123", "docente.len@libromp.cl" to "docente123",
            "utp@libromp.cl" to "utp123", "inspectoria@libromp.cl" to "inspectoria123",
            "direccion@libromp.cl" to "direccion123"
        )
        val perfiles = claves.map { (correo, clave) ->
            val r = verificarCredenciales(usuario(correo), clave)
            assertTrue("$correo debería ingresar", r is ResultadoLogin.Aceptado)
            (r as ResultadoLogin.Aceptado).usuario.perfil
        }
        assertEquals(setOf(Perfil.DOCENTE, Perfil.UTP, Perfil.INSPECTORIA, Perfil.DIRECCION), perfiles.toSet())
    }

    @Test
    fun laClaveNuncaSeGuardaEnClaro_yUnaClaveVaciaNoEntra() {
        assertTrue(MockData.usuarios.none { it.claveHash.contains("123") })
        assertTrue(verificarCredenciales(usuario("utp@libromp.cl"), "") is ResultadoLogin.Rechazado)
    }
}
