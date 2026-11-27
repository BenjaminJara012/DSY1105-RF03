package com.librompaz.app.data.remote

/**
 * Dirección del servidor (URL base) y su validación. Son funciones puras: se prueban con JUnit sin Android.
 *
 * Regla del caso (RN-01 y RNF-07, flujo aislado): la app solo puede hablar con un servidor de la red
 * del aula. Por eso se aceptan únicamente direcciones locales:
 *  - localhost y cualquier nombre que termine en ".localhost";
 *  - IPv4 de loopback (127.x.x.x), del alias del emulador (10.0.2.2, dentro de 10.x.x.x), privadas
 *    (10.0.0.0/8, 172.16.0.0 a 172.31.255.255, 192.168.0.0/16) y de enlace local (169.254.x.x);
 *  - IPv6 local (::1, fe80:..., fc00::/7);
 *  - nombres internos: sin punto ("aula-pc") o que terminen en .local, .lan, .home.arpa o .internal.
 * Cualquier otra dirección (por ejemplo una IP pública o un dominio de internet) se rechaza.
 */
object ConfigServidor {

    /** Dirección del computador visto desde el emulador de Android Studio. */
    const val URL_EMULADOR = "http://10.0.2.2:3000/"

    const val MENSAJE_VACIA = "Escribe la dirección del servidor"
    const val MENSAJE_FORMATO = "Usa el formato http://192.168.1.10:3000"
    const val MENSAJE_ESQUEMA = "La dirección debe empezar con http:// o https://"
    const val MENSAJE_PUERTO = "El puerto debe estar entre 1 y 65535"
    const val MENSAJE_RUTA = "Escribe solo el servidor y el puerto, sin rutas"
    const val MENSAJE_NO_LOCAL =
        "Solo se admiten servidores de la red local (localhost, 10.0.2.2, 192.168.x.x, 10.x.x.x o 172.16 a 172.31). " +
            "Los datos no pueden salir de la red del aula."

    sealed class ResultadoUrl {
        /** [url] ya normalizada: esquema y host en minúsculas, con puerto si se indicó y "/" final. */
        data class Valida(val url: String) : ResultadoUrl()
        data class Invalida(val mensaje: String) : ResultadoUrl()
    }

    // TODO(E3-4) [ConfigServidor.kt] ¿Es un host de la red local? (RN-01, RNF-07)
    //   QUÉ: devolver true solo si [host] cumple las reglas de la documentación de esta clase (ignora
    //        mayúsculas, espacios y los corchetes de IPv6). Cuidado con 172.32.x.x, 192.169.x.x, octetos
    //        mayores que 255 y direcciones incompletas como "192.168.1".
    //   ACEPTACIÓN: true para localhost, 127.0.0.1, 10.0.2.2, 172.16.0.1, 172.31.255.255, 192.168.1.10, ::1 y
    //        "aula-pc"; false para 8.8.8.8, 172.32.0.1, 192.169.0.1, 300.1.1.1, 192.168.1 y www.google.com.
    fun esHostLocal(host: String): Boolean = TODO("E3-4: reconocer hosts de la red local")

    // TODO(E3-5) [ConfigServidor.kt] Validar y normalizar la URL escrita por el usuario.
    //   QUÉ: recortar espacios; vacía -> MENSAJE_VACIA; sin esquema asumir "http://"; esquema distinto de
    //        http/https -> MENSAJE_ESQUEMA; con ruta -> MENSAJE_RUTA; otro formato incorrecto -> MENSAJE_FORMATO;
    //        puerto fuera de 1..65535 -> MENSAJE_PUERTO; host no local (esHostLocal) -> MENSAJE_NO_LOCAL.
    //        Si es válida, devolver Valida con "esquema://host[:puerto]/" (esquema y host en minúsculas).
    //   ACEPTACIÓN: "10.0.2.2:3000" -> Valida("http://10.0.2.2:3000/"); "HTTP://Aula-PC" -> Valida("http://aula-pc/");
    //        "https://api.ejemplo.com" -> Invalida(MENSAJE_NO_LOCAL); "" -> MENSAJE_VACIA;
    //        "ftp://192.168.1.10" -> MENSAJE_ESQUEMA; "192.168.1.10:99999" -> MENSAJE_PUERTO;
    //        "192.168.1.10:3000/api" -> MENSAJE_RUTA.
    fun validarUrl(texto: String): ResultadoUrl = TODO("E3-5: validar y normalizar la URL")
}
