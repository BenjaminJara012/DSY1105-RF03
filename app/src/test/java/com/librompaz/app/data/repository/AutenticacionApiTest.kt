
package com.librompaz.app.data.repository

import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.data.remote.FuenteRemota
import com.librompaz.app.data.remote.TokenSesion
import com.librompaz.app.testing.CacheFalsa
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.librompaz.app.data.MockData
import org.junit.Assert.assertFalse

/**
 * Pruebas de autenticación contra la API.
 *
 * Utilizamos un servidor simulado y una caché falsa.
 * Así no dependemos de Internet ni de un dispositivo Android.
 */
class AutenticacionApiTest {

    /**
     * C41: una contraseña incorrecta debe ser rechazada.
     *
     * También comprobamos que no se guarde un token
     * ni un usuario nuevo en la caché.
     */
    @Test
    fun credencialesIncorrectas_noInicianSesion() = runTest {

        // Simula la caché local, inicialmente vacía.
        val cache = CacheFalsa()

        // Simula la clase que realiza las peticiones HTTP.
        val remota = mockk<FuenteRemota>()

        // Almacena el token de la sesión.
        val sesion = TokenSesion()

        // Creamos el repositorio que estamos probando.
        val repositorio = LibroMpazApiRepository(
            cache = cache,
            remota = remota,
            sesion = sesion
        )

        // Simulamos que el servidor rechaza la contraseña.
        coEvery {
            remota.iniciarSesion(
                "docente.mat@libromp.cl",
                "incorrecta"
            )
        } returns ApiResult.Error(
            mensaje = "Correo o clave incorrectos",
            codigo = 401
        )

        // Intentamos ingresar con la contraseña equivocada.
        val resultado = repositorio.ingresar(
            "docente.mat@libromp.cl",
            "incorrecta"
        )

        // El inicio de sesión debe ser rechazado.
        assertEquals(
            ResultadoLogin.Rechazado(
                "Correo o clave incorrectos"
            ),
            resultado
        )

        // No debe existir un token de sesión.
        assertNull(sesion.token)

        // No debe haberse guardado ningún usuario.
        assertTrue(cache.usuariosGuardados.isEmpty())

        // Comprobamos que se consultó al servidor una vez.
        coVerify(exactly = 1) {
            remota.iniciarSesion(
                "docente.mat@libromp.cl",
                "incorrecta"
            )
        }
    }


    /**
     * C42: si el servidor está desconectado,
     * permite ingresar con credenciales guardadas anteriormente.
     */
    @Test
    fun credencialesGuardadas_permitenIngresarSinConexion() = runTest {

        // Usuario ficticio que ya ingresó anteriormente.
        val docente = MockData.usuarios.first {
            it.usuario == "docente.mat@libromp.cl"
        }

        // Simulamos que ese usuario ya existe en el teléfono.
        val cache = CacheFalsa(usuarios = listOf(docente))

        val remota = mockk<FuenteRemota>()
        val sesion = TokenSesion()

        val repositorio = LibroMpazApiRepository(
            cache = cache,
            remota = remota,
            sesion = sesion
        )

        // Simulamos que el servidor no responde.
        coEvery {
            remota.iniciarSesion(
                "docente.mat@libromp.cl",
                "docente123"
            )
        } returns ApiResult.SinConexion("Sin conexión")

        // Intentamos iniciar sesión.
        val resultado = repositorio.ingresar(
            "docente.mat@libromp.cl",
            "docente123"
        )

        // La aplicación debe reconocer al usuario guardado.
        assertTrue(resultado is ResultadoLogin.SinConexion)

        val ingreso = resultado as ResultadoLogin.SinConexion

        assertEquals(docente, ingreso.usuario)

        // Debe informar que utiliza datos de la última sincronización.
        assertTrue(
            ingreso.aviso.contains("última sincronización")
        )

        // Sin servidor no se genera un token nuevo.
        assertNull(sesion.token)
    }


    /**
     * C43 - Caso 1:
     * Sin servidor y sin credenciales guardadas,
     * no se permite iniciar sesión.
     */
    @Test
    fun sinConexion_sinUsuarioGuardado_noPermiteEntrar() = runTest {

        // Caché vacía: el usuario nunca ingresó en este teléfono.
        val cache = CacheFalsa()
        val remota = mockk<FuenteRemota>()
        val sesion = TokenSesion()

        val repositorio = LibroMpazApiRepository(
            cache = cache,
            remota = remota,
            sesion = sesion
        )

        // Simulamos que el servidor está desconectado.
        coEvery {
            remota.iniciarSesion(
                "nuevo@libromp.cl",
                "clave123"
            )
        } returns ApiResult.SinConexion("Sin conexión")

        val resultado = repositorio.ingresar(
            "nuevo@libromp.cl",
            "clave123"
        )

        // Sin credenciales anteriores, el ingreso no está disponible.
        assertEquals(
            ResultadoLogin.NoDisponible("Sin conexión"),
            resultado
        )

        // No se genera un token.
        assertNull(sesion.token)
    }

    /**
     * C43 - Caso 2:
     * Si el usuario está guardado, pero escribe
     * una contraseña incorrecta, se rechaza el ingreso.
     */
    @Test
    fun sinConexion_claveIncorrecta_noPermiteEntrar() = runTest {

        // Usuario ficticio guardado anteriormente.
        val docente = MockData.usuarios.first {
            it.usuario == "docente.mat@libromp.cl"
        }

        val cache = CacheFalsa(usuarios = listOf(docente))
        val remota = mockk<FuenteRemota>()
        val sesion = TokenSesion()

        val repositorio = LibroMpazApiRepository(
            cache = cache,
            remota = remota,
            sesion = sesion
        )

        // El servidor está desconectado.
        coEvery {
            remota.iniciarSesion(
                "docente.mat@libromp.cl",
                "claveIncorrecta"
            )
        } returns ApiResult.SinConexion("Sin conexión")

        val resultado = repositorio.ingresar(
            "docente.mat@libromp.cl",
            "claveIncorrecta"
        )

        // Aunque el usuario exista, la contraseña no coincide.
        assertEquals(
            ResultadoLogin.Rechazado(
                "Correo o clave incorrectos"
            ),
            resultado
        )

        assertNull(sesion.token)
    }



}
