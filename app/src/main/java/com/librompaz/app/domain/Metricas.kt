package com.librompaz.app.domain

import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.ResumenCurso
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.util.diasEntre
import com.librompaz.app.util.restarDias

// =====================================================================================
// Cálculos de las vistas de supervisión (UTP, Inspectoría y Dirección), como funciones puras:
// reciben listas y devuelven el resultado. El ViewModel las aplica sobre los Flow de Room y
// también se prueban con JUnit (ver app/src/test/.../domain/MetricasTest.kt).
// =====================================================================================

/** RN-07: desde cuántas ausencias (AUSENTE, no justificadas) un estudiante tiene inasistencia reiterada. */
const val UMBRAL_INASISTENCIAS = 3

/** RN-07: desde cuántas anotaciones negativas un estudiante requiere intervención. */
const val UMBRAL_ANOTACIONES_NEGATIVAS = 2

/** RF-09: una evaluación con notas incompletas se marca como atrasada pasados estos días. */
const val DIAS_ATRASO_NOTAS = 15

/** RF-12: ventana (en días) para detectar cargas sin clases registradas recientemente. */
const val DIAS_VENTANA_CLASES = 7

// ---------- Asistencia (RF-05) ----------

/**
 * Porcentaje de asistencia (0 a 100) de un conjunto de registros:
 * (presentes + atrasados) / total de registros x 100, redondeado. Ausente y justificado no cuentan
 * como asistencia. null si no hay registros (nunca divide por cero).
 */
fun calcularPorcentajeAsistencia(registros: List<RegistroAsistencia>): Int? {
    if (registros.isEmpty()) return null
    val asistieron = registros.count {
        it.estado == EstadoAsistencia.PRESENTE || it.estado == EstadoAsistencia.ATRASADO
    }
    return Math.round(asistieron * 100.0 / registros.size).toInt()
}

/** Porcentaje de asistencia de cada curso (cursoId -> %). Los cursos sin registros no aparecen. */
fun calcularPorcentajesPorCurso(asistencias: List<RegistroAsistencia>): Map<String, Int> =
    asistencias.groupBy { it.cursoId }
        .mapNotNull { (cursoId, registros) -> calcularPorcentajeAsistencia(registros)?.let { cursoId to it } }
        .toMap()

// ---------- Alertas de Inspectoría (RF-11, RN-07) ----------

/** Estudiantes con [UMBRAL_INASISTENCIAS] o más ausencias, de mayor a menor. */
fun calcularInasistenciasReiteradas(
    asistencias: List<RegistroAsistencia>,
    estudiantes: List<Estudiante>
): List<Pair<Estudiante, Int>> {
    val ausenciasPorEstudiante = asistencias
        .filter { it.estado == EstadoAsistencia.AUSENTE }
        .groupingBy { it.estudianteId }
        .eachCount()
    return estudiantes
        .mapNotNull { estudiante -> ausenciasPorEstudiante[estudiante.id]?.let { estudiante to it } }
        .filter { (_, ausencias) -> ausencias >= UMBRAL_INASISTENCIAS }
        .sortedByDescending { it.second }
}

/**
 * Anotaciones negativas de los estudiantes que acumulan [UMBRAL_ANOTACIONES_NEGATIVAS] o más,
 * de la más reciente a la más antigua.
 */
fun calcularAnotacionesIntervencion(anotaciones: List<Anotacion>): List<Anotacion> {
    val negativas = anotaciones.filter { it.tipo == TipoAnotacion.NEGATIVA }
    val estudiantesEnRiesgo = negativas
        .groupingBy { it.estudianteId }
        .eachCount()
        .filterValues { it >= UMBRAL_ANOTACIONES_NEGATIVAS }
        .keys
    return negativas
        .filter { it.estudianteId in estudiantesEnRiesgo }
        .sortedByDescending { it.fechaHora }
}

// ---------- Cobertura curricular (RF-07) ----------

private fun sesionesDe(sesiones: List<SesionClase>, cursoId: String?, asignaturaId: String?) =
    sesiones.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

/** Ids de los OA ya trabajados en un curso y asignatura, sin repetidos. */
fun calcularOasTrabajados(sesiones: List<SesionClase>, cursoId: String?, asignaturaId: String?): Set<String> =
    sesionesDe(sesiones, cursoId, asignaturaId).flatMap { it.oaIds }.toSet()

/** Fecha de la última sesión en que se trabajó cada OA (oaId -> AAAA-MM-DD). */
fun ultimaFechaPorOa(sesiones: List<SesionClase>, cursoId: String?, asignaturaId: String?): Map<String, String> {
    val ultimas = linkedMapOf<String, String>()
    for (sesion in sesionesDe(sesiones, cursoId, asignaturaId)) {
        for (oaId in sesion.oaIds) {
            val actual = ultimas[oaId]
            if (actual == null || sesion.fecha > actual) ultimas[oaId] = sesion.fecha
        }
    }
    return ultimas
}

/** Texto de avance de cobertura. Si la asignatura no tiene OA en el catálogo, "Sin OA cargados". */
fun textoCobertura(trabajados: Int, total: Int): String =
    if (total == 0) "Sin OA cargados"
    else "OA trabajados: $trabajados de $total (${Math.round(trabajados * 100.0 / total)} %)"

// ---------- Desfases en las notas (RF-09) ----------

enum class TipoDesfase { SIN_EVALUACIONES, NOTAS_FALTANTES, ATRASADA }

/** Alerta de UTP: [mensaje] para mostrar y [tipo] para destacarla según su gravedad. */
data class Desfase(val mensaje: String, val tipo: TipoDesfase)

/**
 * Desfases u omisiones en el registro de notas:
 *  - cargas (curso + asignatura) que todavía no tienen ninguna evaluación;
 *  - evaluaciones en las que faltan notas de estudiantes del curso;
 *  - de esas, las que llevan más de [DIAS_ATRASO_NOTAS] días desde su fecha se marcan atrasadas.
 *
 * [hoy] es la fecha de hoy en AAAA-MM-DD.
 */
fun calcularDesfases(
    cargas: List<CargaDocente>,
    evaluaciones: List<Evaluacion>,
    calificaciones: List<Calificacion>,
    estudiantes: List<Estudiante>,
    cursos: List<Curso>,
    asignaturas: List<Asignatura>,
    hoy: String
): List<Desfase> {
    fun nombreCurso(id: String) = cursos.find { it.id == id }?.nombre ?: id
    fun nombreAsignatura(id: String) = asignaturas.find { it.id == id }?.nombre ?: id

    val sinEvaluaciones = cargas
        .filter { carga -> evaluaciones.none { it.cursoId == carga.cursoId && it.asignaturaId == carga.asignaturaId } }
        .map {
            Desfase(
                "${nombreCurso(it.cursoId)}, ${nombreAsignatura(it.asignaturaId)}: sin evaluaciones registradas",
                TipoDesfase.SIN_EVALUACIONES
            )
        }

    val notasIncompletas = evaluaciones.mapNotNull { evaluacion ->
        val idsDelCurso = estudiantes.filter { it.cursoId == evaluacion.cursoId }.map { it.id }.toSet()
        val ingresadas = calificaciones.count { it.evaluacionId == evaluacion.id && it.estudianteId in idsDelCurso }
        val faltan = idsDelCurso.size - ingresadas
        if (faltan <= 0) return@mapNotNull null

        val dias = diasEntre(evaluacion.fecha, hoy) ?: 0
        val atrasada = dias > DIAS_ATRASO_NOTAS
        val faltantes = if (faltan == 1) "falta 1 nota" else "faltan $faltan notas"
        val base = "${evaluacion.nombreInstrumento} — ${nombreCurso(evaluacion.cursoId)}, " +
            "${nombreAsignatura(evaluacion.asignaturaId)}: $faltantes"
        if (atrasada) {
            Desfase("$base (atrasada: $dias días desde la evaluación)", TipoDesfase.ATRASADA)
        } else {
            Desfase(base, TipoDesfase.NOTAS_FALTANTES)
        }
    }
    return sinEvaluaciones + notasIncompletas
}

// ---------- Panel de Dirección (RF-12) ----------

/**
 * Métricas consolidadas de cada curso: asistencia (misma fórmula de RF-05), clases, notas y
 * anotaciones negativas, más dos indicadores de consistencia de registros:
 *  - días con asistencia incompleta (menos registros que estudiantes);
 *  - cargas sin clases registradas en los últimos [DIAS_VENTANA_CLASES] días.
 */
fun calcularResumenPorCurso(
    cursos: List<Curso>,
    cargas: List<CargaDocente>,
    asistencias: List<RegistroAsistencia>,
    sesiones: List<SesionClase>,
    evaluaciones: List<Evaluacion>,
    calificaciones: List<Calificacion>,
    anotaciones: List<Anotacion>,
    estudiantes: List<Estudiante>,
    hoy: String
): List<ResumenCurso> {
    val desdeReciente = restarDias(hoy, DIAS_VENTANA_CLASES)
    return cursos.map { curso ->
        val evaluacionesDelCurso = evaluaciones.filter { it.cursoId == curso.id }.map { it.id }.toSet()
        val estudiantesDelCurso = estudiantes.filter { it.cursoId == curso.id }.map { it.id }.toSet()
        val asistenciasDelCurso = asistencias.filter { it.cursoId == curso.id }
        val diasIncompletos = if (estudiantesDelCurso.isEmpty()) 0 else
            asistenciasDelCurso.groupBy { it.fecha }.count { (_, registros) ->
                registros.map { it.estudianteId }.distinct().size < estudiantesDelCurso.size
            }
        val sinClasesRecientes = if (desdeReciente == null) 0 else
            cargas.filter { it.cursoId == curso.id }.count { carga ->
                sesiones.none {
                    it.cursoId == carga.cursoId && it.asignaturaId == carga.asignaturaId &&
                        it.fecha >= desdeReciente && it.fecha <= hoy
                }
            }
        ResumenCurso(
            cursoId = curso.id,
            porcentajeAsistencia = calcularPorcentajeAsistencia(asistenciasDelCurso),
            sesionesRegistradas = sesiones.count { it.cursoId == curso.id },
            calificacionesRegistradas = calificaciones.count { it.evaluacionId in evaluacionesDelCurso },
            anotacionesNegativas = anotaciones.count {
                it.tipo == TipoAnotacion.NEGATIVA && it.estudianteId in estudiantesDelCurso
            },
            diasAsistenciaIncompleta = diasIncompletos,
            cargasSinClasesRecientes = sinClasesRecientes
        )
    }
}
