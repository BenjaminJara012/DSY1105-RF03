package com.librompaz.app.domain

import com.librompaz.app.data.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** RNF-08: las claves nunca se guardan ni se comparan en texto plano. */
class ClaveSeguraTest {

    @Test
    fun hash_esDeterministaYTiene64CaracteresHexadecimales() {
        val hash = ClaveSegura.hash("docente123")
        assertEquals(hash, ClaveSegura.hash("docente123"))
        assertEquals(64, hash.length)
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun hash_noEsLaClaveEnTextoPlano_yCambiaConCadaClave() {
        assertNotEquals("docente123", ClaveSegura.hash("docente123"))
        assertNotEquals(ClaveSegura.hash("docente123"), ClaveSegura.hash("docente124"))
    }

    @Test
    fun coincide_aceptaLaClaveCorrectaYRechazaLasDemas() {
        val guardado = ClaveSegura.hash("utp123")
        assertTrue(ClaveSegura.coincide("utp123", guardado))
        assertFalse(ClaveSegura.coincide("utp124", guardado))
        assertFalse(ClaveSegura.coincide("", guardado))
        assertFalse(ClaveSegura.coincide(guardado, guardado))
    }

    @Test
    fun usuariosDePrueba_guardanElHashDeSuClaveDocumentada() {
        val claves = mapOf(
            "docente.mat@libromp.cl" to "docente123",
            "docente.len@libromp.cl" to "docente123",
            "utp@libromp.cl" to "utp123",
            "inspectoria@libromp.cl" to "inspectoria123",
            "direccion@libromp.cl" to "direccion123"
        )
        assertEquals(claves.keys, MockData.usuarios.map { it.usuario }.toSet())
        MockData.usuarios.forEach { usuario ->
            val clave = claves.getValue(usuario.usuario)
            assertTrue("hash de ${usuario.usuario}", ClaveSegura.coincide(clave, usuario.claveHash))
            assertNotEquals(clave, usuario.claveHash)
        }
    }
}
