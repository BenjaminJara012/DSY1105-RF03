package com.librompaz.app.data.remote

import okhttp3.Interceptor

/** En el build release no se registra ninguna petición. */
fun interceptoresDeDepuracion(): List<Interceptor> = emptyList()
