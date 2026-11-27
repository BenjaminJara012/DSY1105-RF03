package com.librompaz.app.data.remote

import java.util.concurrent.ConcurrentHashMap
import okhttp3.OkHttpClient

/**
 * Construye el cliente HTTP (OkHttp) y los servicios Retrofit.
 *
 * La URL del servidor se puede cambiar en el cuadro "Servidor" sin recompilar, así que no hay un
 * único Retrofit: [servicio] crea (y recuerda) uno por cada URL base. El cliente OkHttp, con sus
 * interceptores, se comparte entre todos.
 *
 * Solo se usa cuando BuildConfig.USAR_API es true; en modo local nunca se construye.
 */
class ApiClient(private val tokenSesion: TokenSesion) {

    // TODO(E3-1) [ApiClient.kt] Cliente HTTP y Retrofit.
    //   QUÉ: crear el OkHttpClient con connectTimeout 8 s, readTimeout 15 s y writeTimeout 15 s, y los interceptores
    //        en este orden: RedLocalInterceptor (E3-3), TokenInterceptor(tokenSesion) (E3-2) y, al final,
    //        los de interceptoresDeDepuracion() (ya entregada: solo registra en el build debug).
    //   ACEPTACIÓN: una petición a un host no local lanza IOException sin abrir conexión; una petición a un host
    //        local lleva "Authorization: Bearer <token>" cuando hay sesión (verificar con MockWebServer).
    private val http: OkHttpClient by lazy { TODO("E3-1: construir el OkHttpClient") }

    private val servicios = ConcurrentHashMap<String, ApiService>()

    // TODO(E3-1) [ApiClient.kt] Servicio Retrofit por URL base.
    //   QUÉ: devolver (y recordar en [servicios]) un Retrofit con baseUrl(urlBase), el [http] anterior y
    //        GsonConverterFactory, y crear con él el ApiService. [urlBase] ya viene validada por
    //        ConfigServidor.validarUrl (termina en "/").
    //   ACEPTACIÓN: dos llamadas con la misma URL devuelven la misma instancia; con otra URL, otra distinta.
    fun servicio(urlBase: String): ApiService = TODO("E3-1: crear el ApiService con Retrofit")
}
