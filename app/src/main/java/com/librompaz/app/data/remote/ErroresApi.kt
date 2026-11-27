package com.librompaz.app.data.remote

// =====================================================================================
// Traducción de los fallos de red y de servidor a mensajes en español para el usuario.
// Nada de trazas ni de texto técnico debe llegar a la pantalla.
// =====================================================================================

const val MENSAJE_SIN_CONEXION =
    "No hay conexión con el servidor. Revisa que esté encendido y que estés en la misma red."
const val MENSAJE_NO_SE_GUARDO = " No se guardó nada."
const val MENSAJE_TIEMPO_AGOTADO = "El servidor tardó demasiado en responder. Intenta nuevamente."
const val MENSAJE_RESPUESTA_INVALIDA = "El servidor respondió algo que la app no entiende. Revisa la dirección del servidor."
const val MENSAJE_ERROR_INESPERADO = "Ocurrió un error inesperado al comunicarse con el servidor."
const val MENSAJE_SESION_EXPIRADA = "Tu sesión venció. Vuelve a iniciar sesión."

// TODO(E3-7) [ErroresApi.kt] Leer el cuerpo de error de la API.
//   QUÉ: convertir el texto JSON {"error": "...", "campo": "..."} en un ErrorDto con Gson; devolver null si
//        el texto es null, está en blanco o no es JSON válido (sin lanzar excepción).
//   ACEPTACIÓN: {"error":"Ya existe","campo":"bloque"} -> ErrorDto("Ya existe","bloque"); "<html>" -> null; null -> null.
fun parsearCuerpoDeError(cuerpo: String?): ErrorDto? = TODO("E3-7: parsear el cuerpo de error")

// TODO(E3-7) [ErroresApi.kt] Mensaje por defecto de un código HTTP.
//   QUÉ: texto en español, distinto para cada caso, para 400, 401 (usar MENSAJE_SESION_EXPIRADA), 403, 404, 405,
//        409, 413, 422, 500..599 y cualquier otro código (este último incluye el número).
//   ACEPTACIÓN: mensajePorCodigo(401) == MENSAJE_SESION_EXPIRADA; los códigos 403, 404 y 409 devuelven textos
//        distintos entre sí; mensajePorCodigo(503) habla de un problema del servidor.
fun mensajePorCodigo(codigo: Int): String = TODO("E3-7: mensaje por código HTTP")

// TODO(E3-7) [ErroresApi.kt] Convertir una respuesta HTTP de error en ApiResult.Error.
//   QUÉ: usar el mensaje del servidor si viene y no está vacío (ya está en español); si no, mensajePorCodigo(codigo).
//        Conservar el código HTTP y el "campo" (null si viene vacío).
//   ACEPTACIÓN: (409, {"error":"Ya existe una clase en ese bloque","campo":"bloque"}) -> Error(esa frase, 409, "bloque");
//        (404, null) -> Error(mensajePorCodigo(404), 404, null).
fun traducirRespuestaHttp(codigo: Int, cuerpo: String?): ApiResult.Error = TODO("E3-7: traducir la respuesta HTTP")

// TODO(E3-8) [ErroresApi.kt] Ejecutar una llamada de Retrofit y devolver su ApiResult.
//   QUÉ: éxito -> Exito(bloque()); HttpException -> traducirRespuestaHttp(code, errorBody().string());
//        SocketTimeoutException -> SinConexion(MENSAJE_TIEMPO_AGOTADO); otra IOException -> SinConexion(MENSAJE_SIN_CONEXION);
//        JsonParseException (cuerpo mal formado) -> Error(MENSAJE_RESPUESTA_INVALIDA); cualquier otra excepción ->
//        Error(MENSAJE_ERROR_INESPERADO). La CancellationException de las corrutinas se vuelve a lanzar SIEMPRE.
//        Ojo con el orden de los catch: SocketTimeoutException es una IOException.
//   ACEPTACIÓN: un 409 con cuerpo JSON -> Error con ese mensaje y código 409; un servidor apagado -> SinConexion;
//        un timeout -> SinConexion(MENSAJE_TIEMPO_AGOTADO); cancelar la corrutina no produce un ApiResult.
suspend fun <T> llamarApi(bloque: suspend () -> T): ApiResult<T> = TODO("E3-8: llamarApi")

// TODO(E3-8) [ErroresApi.kt] "Probar conexión": consultar GET /health.
//   QUÉ: llamar a servicio.salud() con llamarApi. Exito con estado "ok" -> el mismo Exito; Exito con otro estado ->
//        Error("El servidor respondió, pero no está listo (estado: X)."); Error con código HTTP (la dirección responde
//        pero no es el servidor de LibroMpaz) -> Error("La dirección responde, pero no es el servidor de LibroMpaz.", código);
//        SinConexion -> se devuelve igual.
//   ACEPTACIÓN: /health {"estado":"ok",...} -> Exito; un 404 en /health -> Error con "no es el servidor de LibroMpaz".
suspend fun probarServidor(servicio: ApiService): ApiResult<SaludDto> = TODO("E3-8: probar el servidor")
