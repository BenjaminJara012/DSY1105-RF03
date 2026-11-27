package com.librompaz.app.model

/**
 * Los cuatro perfiles de la app (RN-01). Ningún estudiante ni apoderado tiene acceso: no existen
 * perfiles, credenciales ni vistas para ellos.
 *
 * Supuesto vigente: los roles son excluyentes; cada usuario tiene uno solo.
 */
enum class Perfil(val etiqueta: String) {
    DOCENTE("Docente"),
    UTP("Unidad Técnico-Pedagógica (UTP)"),
    INSPECTORIA("Inspectoría"),
    DIRECCION("Dirección")
}
