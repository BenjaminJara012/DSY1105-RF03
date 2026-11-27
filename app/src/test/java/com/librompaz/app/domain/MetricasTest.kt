package com.librompaz.app.domain

import com.librompaz.app.data.MockData
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.TipoAnotacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cálculos de UTP, Inspectoría y Dirección con los datos ficticios de MockData (criterios de aceptación). */
class MetricasTest {

    private val hoy = "2026-10-05"

    private fun registro(estudiante: String, fecha: String, estado: EstadoAsistencia, curso: String = "C3A") =
        RegistroAsistencia(idAsistencia(estudiante, fecha), estudiante, curso, fecha, estado, "Docente", "$fecha 08:00:00")

    private fun negativa(id: String, estudiante: String, fechaHora: String) =
        Anotacion(id, estudiante, TipoAnotacion.NEGATIVA, "Descripción de ejemplo.", fechaHora, "Docente")

    // ---------- RF-05: porcentaje de asistencia ----------

    @Test
    fun porcentaje_3BasicoA_conPresenteAusenteYAtrasado_es67() {
        assertEquals(67, calcularPorcentajeAsistencia(MockData.asistencias.filter { it.cursoId == "C3A" }))
    }

    @Test
    fun porcentaje_cuentaAtrasadoComoAsistenciaYJustificadoComoNo() {
        val registros = listOf(
            registro("E1", "2026-10-01", EstadoAsistencia.PRESENTE),
            registro("E2", "2026-10-01", EstadoAsistencia.ATRASADO),
            registro("E3", "2026-10-01", EstadoAsistencia.AUSENTE),
            registro("E4", "2026-10-01", EstadoAsistencia.JUSTIFICADO),
        )
        assertEquals(50, calcularPorcentajeAsistencia(registros))
    }

    @Test
    fun porcentaje_sinRegistros_esNull_sinDividirPorCero() {
        assertNull(calcularPorcentajeAsistencia(emptyList()))
        val porCurso = calcularPorcentajesPorCurso(MockData.asistencias)
        assertEquals(67, porCurso["C3A"])
        assertNull(porCurso["C4A"])
    }

    // ---------- RF-11 / RN-07: umbrales ----------

    @Test
    fun inasistencias_conUnaAusencia_noApareceEnAlertas() {
        assertTrue(calcularInasistenciasReiteradas(MockData.asistencias, MockData.estudiantes).isEmpty())
    }

    @Test
    fun inasistencias_dosAusenciasNo_tresAusenciasSi() {
        val dosMas = listOf(
            registro("E3A-2", "2026-10-02", EstadoAsistencia.AUSENTE),
            registro("E3A-2", "2026-10-05", EstadoAsistencia.AUSENTE),
        )
        val conDos = calcularInasistenciasReiteradas(MockData.asistencias + dosMas.take(1), MockData.estudiantes)
        assertTrue(conDos.isEmpty())

        val conTres = calcularInasistenciasReiteradas(MockData.asistencias + dosMas, MockData.estudiantes)
        assertEquals(1, conTres.size)
        assertEquals("E3A-2", conTres.first().first.id)
        assertEquals(3, conTres.first().second)
    }

    @Test
    fun inasistencias_losJustificadosNoCuentanComoAusencias() {
        val registros = listOf(
            registro("E3A-1", "2026-10-01", EstadoAsistencia.JUSTIFICADO),
            registro("E3A-1", "2026-10-02", EstadoAsistencia.JUSTIFICADO),
            registro("E3A-1", "2026-10-05", EstadoAsistencia.JUSTIFICADO),
        )
        assertTrue(calcularInasistenciasReiteradas(registros, MockData.estudiantes).isEmpty())
    }

    @Test
    fun inasistencias_seOrdenanDeMayorAMenor() {
        val registros = listOf("2026-10-01", "2026-10-02", "2026-10-05").map { registro("E3A-1", it, EstadoAsistencia.AUSENTE) } +
            listOf("2026-10-01", "2026-10-02", "2026-10-05", "2026-10-06").map { registro("E3A-2", it, EstadoAsistencia.AUSENTE) }
        val alertas = calcularInasistenciasReiteradas(registros, MockData.estudiantes)
        assertEquals(listOf("E3A-2" to 4, "E3A-1" to 3), alertas.map { it.first.id to it.second })
    }

    @Test
    fun intervencion_conUnaSolaNegativa_noRequiere() {
        assertTrue(calcularAnotacionesIntervencion(MockData.anotaciones).isEmpty())
    }

    @Test
    fun intervencion_segundaNegativaDelMismoEstudiante_requiere_yVaDeLaMasRecienteALaMasAntigua() {
        val segunda = negativa("AN3", "E3A-2", "2026-10-05 10:20:00")
        val lista = calcularAnotacionesIntervencion(MockData.anotaciones + segunda)
        assertEquals(listOf("AN3", "AN2"), lista.map { it.id })
    }

    @Test
    fun intervencion_negativasDeEstudiantesDistintosOPositivasNoSuman() {
        val anotaciones = listOf(
            negativa("A1", "E1", "2026-10-01 10:00:00"),
            negativa("A2", "E2", "2026-10-02 10:00:00"),
            Anotacion("A3", "E1", TipoAnotacion.POSITIVA, "Ayuda a sus compañeros.", "2026-10-03 10:00:00", "D"),
            Anotacion("A4", "E2", TipoAnotacion.OBSERVACION, "Se observa distraído.", "2026-10-03 10:00:00", "D"),
        )
        assertTrue(calcularAnotacionesIntervencion(anotaciones).isEmpty())
    }

    // ---------- RF-07: cobertura curricular ----------

    @Test
    fun cobertura_3BasicoAMatematica_tiene1DeLos3OaTrabajados() {
        val trabajados = calcularOasTrabajados(MockData.sesiones, "C3A", "MAT")
        val total = MockData.objetivos.count { it.asignaturaId == "MAT" }
        assertEquals(setOf("OA-MAT-1"), trabajados)
        assertEquals(3, total)
        assertEquals("OA trabajados: 1 de 3 (33 %)", textoCobertura(trabajados.size, total))
    }

    @Test
    fun cobertura_4BasicoAMatematicaSinSesiones_tiene0De3() {
        val trabajados = calcularOasTrabajados(MockData.sesiones, "C4A", "MAT")
        assertTrue(trabajados.isEmpty())
        assertEquals("OA trabajados: 0 de 3 (0 %)", textoCobertura(trabajados.size, 3))
    }

    @Test
    fun cobertura_asignaturaSinOaEnElCatalogo_diceSinOaCargados() {
        assertEquals("Sin OA cargados", textoCobertura(0, 0))
    }

    @Test
    fun cobertura_noRepiteOaYTomaLaUltimaFecha() {
        val s1 = MockData.sesiones.first()
        val s2 = s1.copy(id = "S2", fecha = "2026-10-05", bloque = "Bloque 2", oaIds = listOf("OA-MAT-1", "OA-MAT-2"))
        val sesiones = listOf(s1, s2)
        assertEquals(setOf("OA-MAT-1", "OA-MAT-2"), calcularOasTrabajados(sesiones, "C3A", "MAT"))
        val ultimas = ultimaFechaPorOa(sesiones, "C3A", "MAT")
        assertEquals("2026-10-05", ultimas["OA-MAT-1"])
        assertEquals("2026-10-05", ultimas["OA-MAT-2"])
        assertNull(ultimas["OA-MAT-3"])
    }

    // ---------- RF-09: desfases en las notas ----------

    private fun desfases(hoy: String) = calcularDesfases(
        MockData.cargas, MockData.evaluaciones, MockData.calificaciones,
        MockData.estudiantes, MockData.cursos, MockData.asignaturas, hoy
    )

    @Test
    fun desfases_ev1ConDosNotasDeSeisEstudiantes_alertaDeCuatroNotasFaltantes() {
        val mensajes = desfases("2026-10-05").map { it.mensaje }
        assertTrue(mensajes.contains("Prueba unidad 1 (ejemplo) — 3° Básico A, Matemática: faltan 4 notas"))
    }

    @Test
    fun desfases_cargasSinEvaluaciones_tienenUnaAlertaCadaUna() {
        val sin = desfases("2026-10-05").filter { it.tipo == TipoDesfase.SIN_EVALUACIONES }.map { it.mensaje }
        assertEquals(
            listOf(
                "4° Básico A, Matemática: sin evaluaciones registradas",
                "4° Básico A, Lenguaje y Comunicación: sin evaluaciones registradas"
            ),
            sin
        )
    }

    @Test
    fun desfases_evaluacionConMasDeQuinceDiasYNotasIncompletas_seMarcaAtrasada() {
        val reciente = desfases("2026-10-05").first { it.mensaje.startsWith("Prueba unidad 1") }
        assertEquals(TipoDesfase.NOTAS_FALTANTES, reciente.tipo)   // 10 días desde EV1

        val atrasada = desfases("2026-10-29").first { it.mensaje.startsWith("Prueba unidad 1") }
        assertEquals(TipoDesfase.ATRASADA, atrasada.tipo)          // 34 días desde EV1
        assertTrue(atrasada.mensaje.contains("faltan 4 notas"))
    }

    @Test
    fun desfases_conNotasCompletas_noHayAlertaDeEvaluacion() {
        val completas = MockData.estudiantes.filter { it.cursoId == "C3A" }.map {
            Calificacion(idCalificacion("EV1", it.id), "EV1", it.id, 5.0, "Docente")
        }
        val resultado = calcularDesfases(
            MockData.cargas, MockData.evaluaciones, completas,
            MockData.estudiantes, MockData.cursos, MockData.asignaturas, "2026-10-05"
        )
        assertTrue(resultado.none { it.tipo == TipoDesfase.NOTAS_FALTANTES || it.tipo == TipoDesfase.ATRASADA })
    }

    @Test
    fun desfases_unaNotaFaltante_usaElSingular() {
        val cinco = MockData.estudiantes.filter { it.cursoId == "C3A" }.take(5).map {
            Calificacion(idCalificacion("EV1", it.id), "EV1", it.id, 5.0, "Docente")
        }
        val resultado = calcularDesfases(
            MockData.cargas, MockData.evaluaciones, cinco,
            MockData.estudiantes, MockData.cursos, MockData.asignaturas, "2026-10-05"
        )
        assertTrue(resultado.any { it.mensaje.endsWith("falta 1 nota") })
    }

    // ---------- RF-12: panel de Dirección ----------

    private fun resumen(hoy: String) = calcularResumenPorCurso(
        MockData.cursos, MockData.cargas, MockData.asistencias, MockData.sesiones,
        MockData.evaluaciones, MockData.calificaciones, MockData.anotaciones, MockData.estudiantes, hoy
    )

    @Test
    fun resumen_3BasicoA_muestraAsistencia67_1Clase_2Notas_1AnotacionNegativa() {
        val c3a = resumen(hoy).first { it.cursoId == "C3A" }
        assertEquals(67, c3a.porcentajeAsistencia)
        assertEquals(1, c3a.sesionesRegistradas)
        assertEquals(2, c3a.calificacionesRegistradas)
        assertEquals(1, c3a.anotacionesNegativas)
    }

    @Test
    fun resumen_4BasicoASinDatos_noTieneAsistenciaYTodoEnCero() {
        val c4a = resumen(hoy).first { it.cursoId == "C4A" }
        assertNull(c4a.porcentajeAsistencia)
        assertEquals(0, c4a.sesionesRegistradas)
        assertEquals(0, c4a.calificacionesRegistradas)
        assertEquals(0, c4a.anotacionesNegativas)
    }

    @Test
    fun resumen_consistenciaDeRegistros() {
        val c3a = resumen("2026-10-05").first { it.cursoId == "C3A" }
        // El 2026-10-01 hay 3 registros de 6 estudiantes: asistencia incompleta.
        assertEquals(1, c3a.diasAsistenciaIncompleta)
        // La carga de Matemática tiene una clase el 2026-10-01, dentro de los 7 días previos al 2026-10-05.
        assertEquals(0, c3a.cargasSinClasesRecientes)
        // Una semana y más tarde, esa carga queda sin clases recientes.
        assertEquals(1, resumen("2026-10-20").first { it.cursoId == "C3A" }.cargasSinClasesRecientes)
        // 4° Básico A tiene dos cargas y ninguna clase registrada.
        assertEquals(2, resumen("2026-10-05").first { it.cursoId == "C4A" }.cargasSinClasesRecientes)
    }

    // ---------- RNF-09: volumen del colegio ----------

    @Test
    fun conVolumenDeColegio_270Estudiantes_lasMetricasSeCalculanRapido() {
        val estudiantes = (1..270).map { Estudiante("EV-$it", "Estudiante V-%03d".format(it), if (it % 2 == 0) "C3A" else "C4A") }
        val dias = (1..20).map { "2026-09-%02d".format(it) }
        val asistencias = dias.flatMap { fecha ->
            estudiantes.mapIndexed { i, e ->
                registro(e.id, fecha, if ((i + fecha.hashCode()) % 17 == 0) EstadoAsistencia.AUSENTE else EstadoAsistencia.PRESENTE, e.cursoId)
            }
        }
        val inicio = System.nanoTime()
        val porCurso = calcularPorcentajesPorCurso(asistencias)
        val alertas = calcularInasistenciasReiteradas(asistencias, estudiantes)
        val resumen = calcularResumenPorCurso(
            MockData.cursos, MockData.cargas, asistencias, MockData.sesiones,
            MockData.evaluaciones, MockData.calificaciones, MockData.anotaciones, estudiantes, "2026-10-05"
        )
        val milisegundos = (System.nanoTime() - inicio) / 1_000_000
        assertEquals(2, porCurso.size)
        assertEquals(2, resumen.size)
        assertTrue(alertas.all { it.second >= UMBRAL_INASISTENCIAS })
        assertTrue("tardó $milisegundos ms", milisegundos < 3_000)
    }
}
