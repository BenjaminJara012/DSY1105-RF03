package com.librompaz.app.data.remote

import android.content.Context
import com.librompaz.app.BuildConfig

/**
 * Dirección del servidor elegida por el usuario en el cuadro "Servidor", guardada en SharedPreferences
 * (solo en este equipo). Si no hay una guardada, o si la guardada ya no es válida, se usa la que trae el
 * build (BuildConfig.API_BASE_URL, definida en gradle.properties).
 *
 * Solo se usa cuando BuildConfig.USAR_API es true.
 */
class ServidorPreferencias(contexto: Context) {

    @Suppress("unused")
    private val preferencias = contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    // TODO(E3-16) [ServidorPreferencias.kt] URL vigente.
    //   QUÉ: leer CLAVE_URL de [preferencias]; si existe y ConfigServidor.validarUrl la acepta (Valida), devolver la
    //        URL normalizada; si no existe o ya no es válida, devolver urlPorDefecto().
    //   ACEPTACIÓN: sin nada guardado devuelve la URL del build; con "http://192.168.1.10:3000/" guardada la devuelve;
    //        si lo guardado fuera una dirección pública (archivo manipulado) se ignora y vuelve a la del build.
    fun urlActual(): String = TODO("E3-16: leer la URL guardada")

    /** La del build, o la del emulador si alguien dejó en gradle.properties una dirección no válida. */
    fun urlPorDefecto(): String {
        val r = ConfigServidor.validarUrl(BuildConfig.API_BASE_URL)
        return if (r is ConfigServidor.ResultadoUrl.Valida) r.url else ConfigServidor.URL_EMULADOR
    }

    // TODO(E3-16) [ServidorPreferencias.kt] Guardar la URL (ya validada) en CLAVE_URL.
    //   ACEPTACIÓN: tras guardar("http://aula-pc:3000/"), urlActual() la devuelve aunque se cierre y abra la app.
    fun guardar(urlValida: String) {
        TODO("E3-16: guardar la URL")
    }

    // TODO(E3-16) [ServidorPreferencias.kt] Volver a la dirección del build: borrar CLAVE_URL.
    //   ACEPTACIÓN: tras restablecer(), urlActual() == urlPorDefecto().
    fun restablecer() {
        TODO("E3-16: restablecer la URL")
    }

    private companion object {
        const val ARCHIVO = "servidor"
        const val CLAVE_URL = "url_base"
    }
}
