package com.librompaz.app.model

/**
 * Los cuatro perfiles del caso (ver Requerimientos_DSY1105-005, "Actores"). Ningún estudiante
 * ni apoderado tiene acceso a la app (privacidad hermética).
 *
 * Pendiente con el CITT: si los roles son excluyentes o una persona puede tener más de uno.
 */
enum class Perfil(val etiqueta: String) {
    DOCENTE("Docente"),
    UTP("Unidad Técnico-Pedagógica (UTP)"),
    INSPECTORIA("Inspectoría"),
    DIRECCION("Dirección")
}
