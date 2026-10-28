package com.librompaz.app.data

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
 * Datos 100% FICTICIOS (Ley 19.628 y Ley 21.719: prohibido usar nombres, RUT, antecedentes
 * académicos reales o diagnósticos PIE). Sirven para que las pantallas tengan algo que mostrar
 * mientras el equipo implementa Room.
 */
object MockData {

    /** Usuarios de prueba para el login. LibroMpazViewModel.login() todavía NO los usa. */
    val usuarios = listOf(
        Usuario("U1", "Docente Matemática (demo)", "docente.mat@libromp.cl", "docente123", Perfil.DOCENTE),
        Usuario("U2", "Docente Lenguaje (demo)", "docente.len@libromp.cl", "docente123", Perfil.DOCENTE),
        Usuario("U3", "UTP (demo)", "utp@libromp.cl", "utp123", Perfil.UTP),
        Usuario("U4", "Inspectoría (demo)", "inspectoria@libromp.cl", "inspectoria123", Perfil.INSPECTORIA),
        Usuario("U5", "Dirección (demo)", "direccion@libromp.cl", "direccion123", Perfil.DIRECCION),
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

    val asistencias = listOf(
        RegistroAsistencia("RA1", "E3A-1", "C3A", "2026-10-01", EstadoAsistencia.PRESENTE, "Docente Matemática (demo)", "2026-10-01 08:05"),
        RegistroAsistencia("RA2", "E3A-2", "C3A", "2026-10-01", EstadoAsistencia.AUSENTE, "Docente Matemática (demo)", "2026-10-01 08:05"),
        RegistroAsistencia("RA3", "E3A-3", "C3A", "2026-10-01", EstadoAsistencia.ATRASADO, "Docente Matemática (demo)", "2026-10-01 08:05"),
    )

    val sesiones = listOf(
        SesionClase("S1", "C3A", "MAT", "2026-10-01", "Bloque 1", listOf("OA-MAT-1"), "Conteo de 10 en 10 hasta 1.000.", "Docente Matemática (demo)", "2026-10-01 09:30"),
    )

    val evaluaciones = listOf(
        Evaluacion("EV1", "C3A", "MAT", "Prueba unidad 1 (ejemplo)", "2026-09-25"),
    )

    val calificaciones = listOf(
        Calificacion("N1", "EV1", "E3A-1", 6.2, "Docente Matemática (demo)"),
        Calificacion("N2", "EV1", "E3A-2", 4.5, "Docente Matemática (demo)"),
    )

    val anotaciones = listOf(
        Anotacion("AN1", "E3A-1", TipoAnotacion.POSITIVA, "Ayuda a sus compañeros durante la actividad.", "2026-09-30 10:15", "Docente Matemática (demo)"),
        Anotacion("AN2", "E3A-2", TipoAnotacion.NEGATIVA, "Interrumpe la clase en reiteradas ocasiones (ejemplo).", "2026-10-01 11:40", "Docente Matemática (demo)"),
    )

    val auditoria = listOf(
        RegistroAuditoria("AU1", "Docente Matemática (demo)", Perfil.DOCENTE, "CREAR", "Asistencia 3° Básico A, 2026-10-01", "2026-10-01 08:05"),
        RegistroAuditoria("AU2", "Docente Matemática (demo)", Perfil.DOCENTE, "CREAR", "Sesión de clase 3° Básico A, Matemática", "2026-10-01 09:30"),
    )
}
