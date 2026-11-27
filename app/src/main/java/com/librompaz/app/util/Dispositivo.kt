package com.librompaz.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import java.util.Locale

// =====================================================================================
// RECURSOS NATIVOS del teléfono que usa la app. Ninguno usa GPS, cámara, micrófono ni
// biometría, ni envía datos fuera del teléfono (restricciones del caso, RNF-05 y RNF-07).
// =====================================================================================

/**
 * Recurso nativo 1: VIBRACIÓN. Respuesta háptica corta cuando una operación termina (éxito) o es
 * rechazada (error), útil cuando el docente mira a los estudiantes y no la pantalla.
 * El permiso VIBRATE es "normal": se declara en el manifest y no se pide en pantalla.
 */
@Suppress("DEPRECATION")
fun vibrar(contexto: Context, milisegundos: Long = 80L) {
    val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        contexto.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        contexto.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrador == null || !vibrador.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrador.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrador.vibrate(milisegundos)
    }
}

/**
 * Recurso nativo 2: TEXTO A VOZ (motor TextToSpeech del sistema). Lee en voz alta, por el
 * parlante, el resumen de cobertura curricular que ve UTP. No usa el micrófono, no pide permisos
 * y el texto no sale del teléfono.
 */
class LectorDeVoz(contexto: Context) {

    @Volatile
    private var listo = false

    private val motor: TextToSpeech = TextToSpeech(contexto.applicationContext) { estado ->
        listo = estado == TextToSpeech.SUCCESS && configurarIdioma()
    }

    // Primero español de Chile; si el teléfono no lo tiene, cualquier español.
    private fun configurarIdioma(): Boolean =
        listOf(Locale("es", "CL"), Locale("es")).any { idioma ->
            motor.setLanguage(idioma) >= TextToSpeech.LANG_AVAILABLE
        }

    /** Devuelve false si el motor todavía no está listo o el teléfono no tiene voz en español. */
    fun leer(texto: String): Boolean {
        if (!listo) return false
        motor.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "libro_mpaz_lectura")
        return true
    }

    /** Hay que liberar el motor al salir de la pantalla. */
    fun cerrar() {
        motor.stop()
        motor.shutdown()
    }
}
