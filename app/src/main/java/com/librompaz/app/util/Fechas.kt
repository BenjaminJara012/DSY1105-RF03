package com.librompaz.app.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val FORMATO_FECHA = "yyyy-MM-dd"
private const val FORMATO_FECHA_HORA = "yyyy-MM-dd HH:mm:ss"
private const val MILIS_POR_DIA = 24L * 60 * 60 * 1000

/** Fecha de hoy en formato AAAA-MM-DD (hora local del teléfono). */
fun hoy(): String = SimpleDateFormat(FORMATO_FECHA, Locale.ROOT).format(Date())

/** Fecha y hora actuales (AAAA-MM-DD HH:mm:ss): marca temporal de la auditoría y de las firmas. */
fun ahora(): String = SimpleDateFormat(FORMATO_FECHA_HORA, Locale.ROOT).format(Date())

/**
 * El DatePicker de Material 3 entrega la fecha elegida como milisegundos a medianoche en UTC.
 * Por eso se formatea en UTC (si no, en Chile podría aparecer el día anterior).
 */
fun fechaDesdeMillis(millis: Long): String = formatoUtc().format(Date(millis))

/** Lo inverso: AAAA-MM-DD a milisegundos UTC. Devuelve null si el texto no es una fecha válida. */
fun millisDesdeFecha(fecha: String): Long? =
    try {
        formatoUtc().parse(fecha)?.time
    } catch (e: ParseException) {
        null
    }

/** Días enteros entre dos fechas AAAA-MM-DD (hasta - desde). null si alguna no es válida. */
fun diasEntre(desde: String, hasta: String): Long? {
    val inicio = millisDesdeFecha(desde) ?: return null
    val fin = millisDesdeFecha(hasta) ?: return null
    return Math.floorDiv(fin - inicio, MILIS_POR_DIA)
}

/** La fecha AAAA-MM-DD que queda al restar [dias] a [fecha]. null si [fecha] no es válida. */
fun restarDias(fecha: String, dias: Int): String? {
    val base = millisDesdeFecha(fecha) ?: return null
    return fechaDesdeMillis(base - dias * MILIS_POR_DIA)
}

private fun formatoUtc() = SimpleDateFormat(FORMATO_FECHA, Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
    isLenient = false   // no acepta fechas como 2026-02-31
}
