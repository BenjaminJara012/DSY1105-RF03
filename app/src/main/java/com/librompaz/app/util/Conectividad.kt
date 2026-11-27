package com.librompaz.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * true si el equipo tiene una red activa con capacidad de datos (Wi-Fi, datos móviles, Ethernet).
 * Usa ACCESS_NETWORK_STATE. Solo sirve para dar una pista al usuario cuando el servidor no responde:
 * no se usa para bloquear llamadas, porque un teléfono conectado por USB (adb reverse) puede alcanzar
 * el servidor sin tener Wi-Fi.
 */
fun hayRedActiva(contexto: Context): Boolean {
    val administrador = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val red = administrador.activeNetwork ?: return false
    val capacidades = administrador.getNetworkCapabilities(red) ?: return false
    return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
        capacidades.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        capacidades.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}
