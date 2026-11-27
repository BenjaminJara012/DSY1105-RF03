package com.librompaz.app.data.remote

import com.librompaz.app.model.Perfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsuarioMapeadorTest {

    @Test
    fun perfilValido_creaElUsuario() {
        val dto = UsuarioDto("U9", "Usuario Prueba", "prueba@libromp.cl", "DOCENTE")

        val usuario = dto.aEntidad("hash-de-prueba")

        assertEquals(Perfil.DOCENTE, usuario?.perfil)
        assertEquals("hash-de-prueba", usuario?.claveHash)
    }

    @Test
    fun perfilDesconocido_devuelveNull() {
        val dto = UsuarioDto("U9", "Usuario Prueba", "prueba@libromp.cl", "ESTUDIANTE")

        assertNull(dto.aEntidad("hash-de-prueba"))
    }
}