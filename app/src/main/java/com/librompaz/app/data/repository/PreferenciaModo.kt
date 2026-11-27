package com.librompaz.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencia persistente "modo local" (true = trabajar solo con la base de este equipo, sin
 * servidor; false = modo servidor).
 *
 *  - [modoLocal] se LEE en cada operación (no se copia al arrancar): el cambio es inmediato.
 *  - [cambios] emite el valor vigente y cada cambio, para que las pantallas y los Flow de datos se
 *    reconecten a la otra fuente sin reiniciar la app.
 */
interface PreferenciaModo {
    var modoLocal: Boolean
    val cambios: StateFlow<Boolean>
}

/**
 * Valor inicial de "modo local" a partir de BuildConfig.USAR_API: false = modo local (como se entrega),
 * true = modo servidor. Solo rige mientras nadie elija otro modo desde la app (cuadro "Servidor").
 */
fun modoLocalInicial(usarApi: Boolean): Boolean = !usarApi

/** Dónde se guarda el valor (SharedPreferences en la app; memoria en las pruebas). null = nunca se eligió. */
interface AlmacenModo {
    fun leer(): Boolean?
    fun guardar(valor: Boolean)
}

/**
 * [PreferenciaModo] sobre un [AlmacenModo]. [valorInicial] es el que rige mientras nadie haya elegido
 * otro desde la app (en esta versión sale de [modoLocalInicial]).
 */
class PreferenciaModoPersistente(
    private val almacen: AlmacenModo,
    private val valorInicial: Boolean
) : PreferenciaModo {

    private val _cambios = MutableStateFlow(almacen.leer() ?: valorInicial)

    override val cambios: StateFlow<Boolean> = _cambios.asStateFlow()

    override var modoLocal: Boolean
        get() = almacen.leer() ?: valorInicial
        set(valor) {
            almacen.guardar(valor)
            _cambios.value = valor
        }
}
