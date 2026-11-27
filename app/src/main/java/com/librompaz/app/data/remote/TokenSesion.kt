package com.librompaz.app.data.remote

/** Token de la sesión activa. Vive solo en memoria: al cerrar la app o la sesión se pierde. */
class TokenSesion {
    @Volatile
    var token: String? = null
}
