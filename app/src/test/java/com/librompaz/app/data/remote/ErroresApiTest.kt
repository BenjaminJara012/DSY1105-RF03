package com.librompaz.app.data.remote

import org.junit.Ignore
import org.junit.Test

/**
 * ESQUELETO (E3-6, E3-7 y E3-8): pruebas del manejo de errores y de ApiResult, sin red.
 *
 * Para llamarApi no necesitas servidor: pasa un bloque que lance la excepción que quieres simular, por ejemplo
 *   runBlocking { llamarApi<Unit> { throw java.net.SocketTimeoutException() } }
 * (con kotlinx-coroutines-test también puedes usar runTest). Una HttpException se arma con
 *   HttpException(retrofit2.Response.error<Any>(409, "{\"error\":\"...\"}".toResponseBody("application/json".toMediaType())))
 *
 * Casos que deben cubrir:
 *  C07 parsearCuerpoDeError: JSON válido -> ErrorDto; "<html>" -> null; null y vacío -> null
 *  C08 mensajePorCodigo: 401 es MENSAJE_SESION_EXPIRADA; 403, 404 y 409 son textos distintos; 503 habla del servidor
 *  C09 traducirRespuestaHttp usa el mensaje y el campo que manda el servidor (409 con {"error","campo"})
 *  C10 traducirRespuestaHttp sin cuerpo usa mensajePorCodigo y deja el campo en null
 *  C11 llamarApi: la llamada exitosa devuelve Exito con el dato
 *  C12 llamarApi: HttpException 400, 401, 403, 404 y 409 -> Error con el código HTTP; sesionExpirada solo en el 401
 *  C13 llamarApi: IOException -> SinConexion(MENSAJE_SIN_CONEXION); SocketTimeoutException -> SinConexion(MENSAJE_TIEMPO_AGOTADO)
 *  C14 llamarApi: JsonParseException (cuerpo mal formado) -> Error(MENSAJE_RESPUESTA_INVALIDA)
 *  C15 llamarApi: la CancellationException se vuelve a lanzar (no se convierte en ApiResult)
 *  C16 probarServidor: estado "ok" -> Exito; 404 en /health -> Error "no es el servidor de LibroMpaz"; sin red -> SinConexion
 *  C17 ApiResult: mapear transforma el Exito y deja intactos Error y SinConexion; mensajeDeFallo; obtenerOSalir
 */
@Ignore("E3: completar")
class ErroresApiTest {

    @Test
    fun `C07 parsearCuerpoDeError lee el JSON de error`() {
        TODO("E3: completar")
    }

    @Test
    fun `C08 mensajePorCodigo entrega un texto por codigo`() {
        TODO("E3: completar")
    }

    @Test
    fun `C09 traducirRespuestaHttp usa el mensaje del servidor`() {
        TODO("E3: completar")
    }

    @Test
    fun `C10 traducirRespuestaHttp sin cuerpo usa el mensaje por codigo`() {
        TODO("E3: completar")
    }

    @Test
    fun `C11 llamarApi devuelve Exito`() {
        TODO("E3: completar")
    }

    @Test
    fun `C12 llamarApi traduce los codigos 400 401 403 404 y 409`() {
        TODO("E3: completar")
    }

    @Test
    fun `C13 llamarApi distingue sin conexion y tiempo agotado`() {
        TODO("E3: completar")
    }

    @Test
    fun `C14 llamarApi detecta un cuerpo mal formado`() {
        TODO("E3: completar")
    }

    @Test
    fun `C15 llamarApi no se traga la cancelacion`() {
        TODO("E3: completar")
    }

    @Test
    fun `C16 probarServidor consulta health`() {
        TODO("E3: completar")
    }

    @Test
    fun `C17 ApiResult mapear mensajeDeFallo y obtenerOSalir`() {
        TODO("E3: completar")
    }
}
