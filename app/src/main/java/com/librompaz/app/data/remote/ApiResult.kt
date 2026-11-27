package com.librompaz.app.data.remote

/**
 * Resultado de una llamada a la API. [Exito] trae el dato; [Error] es una respuesta del servidor
 * (regla de negocio, permiso, sesión vencida...) con un mensaje listo para el usuario; [SinConexion]
 * es un problema de red (servidor apagado, otra red, tiempo agotado).
 *
 * Es covariante (out T) y los fallos son ApiResult<Nothing>: un fallo se puede devolver tal cual
 * desde una función que devuelve cualquier otro ApiResult.
 */
sealed class ApiResult<out T> {

    data class Exito<out T>(val dato: T) : ApiResult<T>()

    /** [codigo] es el código HTTP; [campo] el campo del formulario con el problema, si el servidor lo indica. */
    data class Error(val mensaje: String, val codigo: Int? = null, val campo: String? = null) : ApiResult<Nothing>() {
        /** 401: la sesión venció o el servidor se reinició; hay que volver a ingresar. */
        val sesionExpirada: Boolean get() = codigo == 401
    }

    data class SinConexion(val mensaje: String) : ApiResult<Nothing>()

    // TODO(E3-6) [ApiResult.kt] Transformar el dato de un éxito.
    //   QUÉ: si es Exito, devolver Exito(transformar(dato)); si es Error o SinConexion, devolverlo sin cambios.
    //   ACEPTACIÓN: Exito(2).mapear { it * 3 } == Exito(6); Error("x", 409).mapear { it } es el mismo Error.
    fun <R> mapear(transformar: (T) -> R): ApiResult<R> = TODO("E3-6: mapear el dato de un éxito")
}

// TODO(E3-6) [ApiResult.kt] Obtener el dato o salir.
//   QUÉ: devolver el dato si es Exito; si es Error o SinConexion, llamar a alFallar(this) (que normalmente
//        hace "return it" desde la función que lo invoca).
//   ACEPTACIÓN: dentro de una función que devuelve ApiResult<Unit>, "val x = r.obtenerOSalir { return it }"
//        corta la función con el mismo fallo cuando r no es Exito.
inline fun <T> ApiResult<T>.obtenerOSalir(alFallar: (ApiResult<Nothing>) -> Nothing): T = TODO("E3-6: obtener el dato o salir")

// TODO(E3-6) [ApiResult.kt] Texto de fallo para el usuario.
//   QUÉ: null si es Exito; el mensaje si es Error o SinConexion.
//   ACEPTACIÓN: Exito(1).mensajeDeFallo == null; SinConexion("sin red").mensajeDeFallo == "sin red".
val ApiResult<*>.mensajeDeFallo: String?
    get() = TODO("E3-6: mensaje de fallo")
