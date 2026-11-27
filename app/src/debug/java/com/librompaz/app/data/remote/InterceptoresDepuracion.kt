package com.librompaz.app.data.remote

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Solo en el build debug: registra en Logcat la línea de cada petición y su código de respuesta
 * (nivel BASIC: sin cuerpos, que contienen datos de estudiantes) y oculta el encabezado de sesión.
 */
fun interceptoresDeDepuracion(): List<Interceptor> =
    listOf(
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        }
    )
