package com.librompaz.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Fecha de hoy en formato AAAA-MM-DD (para dejarla como valor inicial en los formularios). */
fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


/**
 * Valida la fecha utilizada para registrar asistencia.
 *
 * Reglas:
 * - No puede estar vacía.
 * - Debe tener el formato yyyy-MM-dd.
 * - Debe representar una fecha real.
 * - No puede ser posterior al día actual.
 *
 * @param fecha Fecha ingresada por el usuario.
 * @return true si es válida; false en caso contrario.
 */
fun validarFechaAsistencia(fecha: String): Boolean {

    // Valida formato esperado
    if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(fecha)) {
        return false
    }

    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
    }

    return try {
        // Comprueba que la fecha exista realmente.
        val fechaIngresada = formato.parse(fecha) ?: return false

        // Compara solo las fechas, sin considerar la hora.
        val fechaActual = formato.parse(hoy()) ?: return false

        !fechaIngresada.after(fechaActual)

    } catch (e: java.text.ParseException) {
        // Una fecha mal ingresada, como 2026-02-30, no es válida.
        false
    }
}