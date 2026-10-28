package com.librompaz.app.model

enum class TipoAnotacion(val etiqueta: String) {
    POSITIVA("Anotación positiva"),
    NEGATIVA("Anotación negativa"),
    OBSERVACION("Observación conductual")
}

/**
 * Anotación de la hoja de vida escolar (entidad del caso), con marca temporal y "firma digital"
 * del docente responsable. Pendiente con el CITT: qué mecanismo exacto se espera para la firma
 * (registro automático de usuario + fecha + hora, o algo adicional como reingresar la clave).
 */
data class Anotacion(
    val id: String,
    val estudianteId: String,
    val tipo: TipoAnotacion,
    val descripcion: String,
    val fechaHora: String,
    val firmadoPor: String
)
