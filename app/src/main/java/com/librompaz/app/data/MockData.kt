package com.librompaz.app.data

import com.librompaz.app.domain.idAsistencia
import com.librompaz.app.domain.idCalificacion
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.Perfil
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario

/**
 * Datos de ejemplo 100 % FICTICIOS (Ley 19.628 y Ley 21.719: prohibido usar nombres, RUT,
 * antecedentes académicos reales o diagnósticos PIE). Los estudiantes se identifican como
 * "Estudiante 3A-01", etc.
 *
 * La pantalla no los lee directamente: LibroMpazRepository.poblarSiVacia() los inserta en Room
 * la primera vez que se abre la app (base vacía). Desde ahí todo se lee y se guarda en SQLite.
 *
 * Supuesto vigente (SUP-06): basta una muestra de 2 cursos con 6 estudiantes cada uno.
 *
 * Las claves de los usuarios de prueba (ver README) se guardan como hash SHA-256 con sal
 * (domain/ClaveSegura.kt); aquí no aparecen en texto plano.
 */
object MockData {

    private const val DOCENTE_MAT = "Docente Matemática (demo)"

    val usuarios = listOf(
        Usuario("U1", DOCENTE_MAT, "docente.mat@libromp.cl", "a570b57f202f013f1b740d6c4d7f6020d98bf9f6a588a040041b6e4d98189bb5", Perfil.DOCENTE),
        Usuario("U2", "Docente Lenguaje (demo)", "docente.len@libromp.cl", "a570b57f202f013f1b740d6c4d7f6020d98bf9f6a588a040041b6e4d98189bb5", Perfil.DOCENTE),
        Usuario("U3", "UTP (demo)", "utp@libromp.cl", "8554be2f38709da7adb46edcf158f8444c344ad1a805341a9d4d610c7a617712", Perfil.UTP),
        Usuario("U4", "Inspectoría (demo)", "inspectoria@libromp.cl", "f85e3e7d535d2eb7d38cce6d10c71fe0db6ea7a7f6365e987d7c3b8b3eccd186", Perfil.INSPECTORIA),
        Usuario("U5", "Dirección (demo)", "direccion@libromp.cl", "bb0b02bad2825162ac512255c6a0f921217c22240aeae0bb923a6ef8b65eaa7b", Perfil.DIRECCION),
    )

    val cursos = listOf(Curso("C3A", "3° Básico A"), Curso("C4A", "4° Básico A"))

    val asignaturas = listOf(
        Asignatura("MAT", "Matemática"),
        Asignatura("LEN", "Lenguaje y Comunicación"),
        Asignatura("CN", "Ciencias Naturales"),
    )

    val estudiantes = (1..6).map { Estudiante("E3A-$it", "Estudiante 3A-%02d".format(it), "C3A") } +
        (1..6).map { Estudiante("E4A-$it", "Estudiante 4A-%02d".format(it), "C4A") }

    /** El docente U1 tiene Matemática en 3°A y 4°A; U2 tiene Lenguaje en 4°A. */
    val cargas = listOf(
        CargaDocente("CD1", "U1", "C3A", "MAT"),
        CargaDocente("CD2", "U1", "C4A", "MAT"),
        CargaDocente("CD3", "U2", "C4A", "LEN"),
    )

    val objetivos = listOf(
        ObjetivoAprendizaje("OA-MAT-1", "MAT", "OA 1 (ejemplo)", "Contar números del 0 al 1.000."),
        ObjetivoAprendizaje("OA-MAT-2", "MAT", "OA 2 (ejemplo)", "Leer y representar números hasta el 1.000."),
        ObjetivoAprendizaje("OA-MAT-3", "MAT", "OA 3 (ejemplo)", "Resolver adiciones y sustracciones."),
        ObjetivoAprendizaje("OA-LEN-1", "LEN", "OA 1 (ejemplo)", "Leer textos con fluidez."),
        ObjetivoAprendizaje("OA-LEN-2", "LEN", "OA 2 (ejemplo)", "Comprender textos narrativos."),
        ObjetivoAprendizaje("OA-CN-1", "CN", "OA 1 (ejemplo)", "Describir necesidades de los seres vivos."),
    )

    /** 3° Básico A, 2026-10-01: Estudiante 3A-01 presente, 3A-02 ausente y 3A-03 atrasado (67 %). */
    val asistencias = listOf(
        asistencia("E3A-1", EstadoAsistencia.PRESENTE),
        asistencia("E3A-2", EstadoAsistencia.AUSENTE),
        asistencia("E3A-3", EstadoAsistencia.ATRASADO),
    )

    val sesiones = listOf(
        SesionClase("S1", "C3A", "MAT", "2026-10-01", "Bloque 1", listOf("OA-MAT-1"), "Conteo de 10 en 10 hasta 1.000.", DOCENTE_MAT, "2026-10-01 09:30:00"),
    )

    val evaluaciones = listOf(
        Evaluacion("EV1", "C3A", "MAT", "Prueba unidad 1 (ejemplo)", "2026-09-25"),
    )

    /** EV1 queda incompleta a propósito (2 notas de 6 estudiantes) para que UTP vea una omisión. */
    val calificaciones = listOf(
        Calificacion(idCalificacion("EV1", "E3A-1"), "EV1", "E3A-1", 6.2, DOCENTE_MAT),
        Calificacion(idCalificacion("EV1", "E3A-2"), "EV1", "E3A-2", 4.5, DOCENTE_MAT),
    )

    val anotaciones = listOf(
        Anotacion("AN1", "E3A-1", TipoAnotacion.POSITIVA, "Ayuda a sus compañeros durante la actividad.", "2026-09-30 10:15:00", DOCENTE_MAT),
        Anotacion("AN2", "E3A-2", TipoAnotacion.NEGATIVA, "Interrumpe la clase en reiteradas ocasiones (ejemplo).", "2026-10-01 11:40:00", DOCENTE_MAT),
    )

    val auditoria = listOf(
        RegistroAuditoria("AU1", DOCENTE_MAT, Perfil.DOCENTE, "CREAR", "Asistencia 3° Básico A, 2026-10-01", "2026-10-01 08:05:00"),
        RegistroAuditoria("AU2", DOCENTE_MAT, Perfil.DOCENTE, "CREAR", "Sesión de clase 3° Básico A, Matemática, 2026-10-01 Bloque 1", "2026-10-01 09:30:00"),
    )

    private fun asistencia(estudianteId: String, estado: EstadoAsistencia) = RegistroAsistencia(
        id = idAsistencia(estudianteId, "2026-10-01"),
        estudianteId = estudianteId,
        cursoId = "C3A",
        fecha = "2026-10-01",
        estado = estado,
        registradoPor = DOCENTE_MAT,
        fechaHoraRegistro = "2026-10-01 08:05:00"
    )
}
