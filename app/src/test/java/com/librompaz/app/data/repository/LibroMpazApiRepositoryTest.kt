package com.librompaz.app.data.repository

import org.junit.Ignore
import org.junit.Test

/**
 * ESQUELETO (E3-10 a E3-15): pruebas del repositorio con API + Room como caché, SIN red ni Android.
 *
 * Armado típico:
 *   val cache = com.librompaz.app.testing.CacheFalsa()                 // ya entregada: CacheLocal en memoria
 *   val remota = mockk<FuenteRemota>()                                 // MockK (ya declarado en build.gradle)
 *   val sesion = TokenSesion()
 *   val repo = LibroMpazApiRepository(cache, remota, sesion)
 * y se configura cada respuesta con coEvery { remota.descargarTodo(Perfil.DOCENTE) } returns ApiResult.Exito(InstantaneaServidor(...)).
 * Verifica con coVerify (cuántas veces se llamó) y con cache.reemplazos / cache.ultima. Usa runBlocking o runTest.
 *
 * Casos que deben cubrir:
 *  C40 ingresar con la API OK: guarda el token en la sesión y el usuario (con el HASH de la clave, nunca la clave) en la caché -> Aceptado
 *  C41 ingresar con clave incorrecta (la API responde 401) -> Rechazado con el mensaje del servidor; no queda token ni usuario nuevo
 *  C42 ingresar sin red con credenciales usadas antes en el equipo (usuario en la caché con su hash) -> SinConexion con el aviso
 *  C43 ingresar sin red y sin credenciales previas -> NoDisponible; con clave distinta a la guardada -> Rechazado
 *  C44 sincronizar OK: la caché se reemplaza UNA vez y sus Flow emiten lo descargado
 *  C45 sincronizar con la API caída o con error: la caché queda como estaba (reemplazos == 0) y se devuelve el fallo
 *  C46 sincronizar cuando Room falla (fallarAlReemplazar = true) -> Error "No se pudo actualizar la copia local de los datos."
 *  C47 escribir OK (por ejemplo guardarAsistencia): se llama a la API una vez y luego se sincroniza una vez
 *  C48 escribir con 409 / 422 del servidor: devuelve el mensaje del servidor y la caché NO cambia
 *  C49 escribir sin red: devuelve SinConexion cuyo mensaje termina en "No se guardó nada." y la caché NO cambia
 *  C50 asistenciaDe: con la API OK devuelve lo del servidor; con la API sin red devuelve lo que hay en la caché
 *  C51 cerrarSesionRemota y retirarToken: retirarToken devuelve el token y deja la sesión vacía; sin token no llama a la API
 */
@Ignore("E3: completar")
class LibroMpazApiRepositoryTest {

    @Test
    fun `C40 ingresar guarda token y usuario`() {
        TODO("E3: completar")
    }

    @Test
    fun `C41 ingresar con clave incorrecta`() {
        TODO("E3: completar")
    }

    @Test
    fun `C42 ingresar sin red con credenciales previas`() {
        TODO("E3: completar")
    }

    @Test
    fun `C43 ingresar sin red sin credenciales previas`() {
        TODO("E3: completar")
    }

    @Test
    fun `C44 sincronizar reemplaza la cache una vez`() {
        TODO("E3: completar")
    }

    @Test
    fun `C45 sincronizar con la API caida deja la cache intacta`() {
        TODO("E3: completar")
    }

    @Test
    fun `C46 sincronizar cuando Room falla`() {
        TODO("E3: completar")
    }

    @Test
    fun `C47 escribir correctamente sincroniza una vez`() {
        TODO("E3: completar")
    }

    @Test
    fun `C48 escribir con error del servidor no cambia la cache`() {
        TODO("E3: completar")
    }

    @Test
    fun `C49 escribir sin red no guarda nada`() {
        TODO("E3: completar")
    }

    @Test
    fun `C50 asistenciaDe usa la cache sin red`() {
        TODO("E3: completar")
    }

    @Test
    fun `C51 retirarToken y cerrarSesionRemota`() {
        TODO("E3: completar")
    }
}
