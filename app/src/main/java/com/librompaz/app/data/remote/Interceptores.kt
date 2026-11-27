package com.librompaz.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Agrega `Authorization: Bearer <token>` a cada petición si hay sesión y la petición no lo trae ya. */
class TokenInterceptor(private val sesion: TokenSesion) : Interceptor {

    // TODO(E3-2) [Interceptores.kt] Token de sesión.
    //   QUÉ: leer sesion.token; si es null o vacío, o si la petición ya trae el encabezado Authorization,
    //        continuar sin cambios; si no, continuar con una copia de la petición que agrega
    //        "Authorization: Bearer <token>".
    //   ACEPTACIÓN: con token "abc" el servidor recibe "Authorization: Bearer abc"; sin token no recibe el
    //        encabezado; el logout (que lo envía a mano) no queda con el encabezado duplicado.
    override fun intercept(chain: Interceptor.Chain): Response = TODO("E3-2: agregar el token Bearer")
}

/**
 * RN-01 y RNF-07 (flujo aislado): corta cualquier petición cuyo destino no sea la red local, aunque
 * la URL guardada se haya manipulado.
 */
class RedLocalInterceptor : Interceptor {

    // TODO(E3-3) [Interceptores.kt] Bloqueo de destinos fuera de la red local (RN-01, RNF-07).
    //   QUÉ: si ConfigServidor.esHostLocal(request.url.host) es false, lanzar IOException con el mensaje
    //        "Destino fuera de la red local bloqueado: los datos no pueden salir del aula" ANTES de abrir
    //        ninguna conexión; si es local, continuar con chain.proceed(request).
    //   ACEPTACIÓN: una petición a https://api.ejemplo.com/ lanza IOException y no genera tráfico; una a
    //        10.0.2.2, localhost o 192.168.x.x pasa. llamarApi (E3-8) la convierte en "sin conexión".
    override fun intercept(chain: Interceptor.Chain): Response = TODO("E3-3: bloquear destinos no locales")
}
