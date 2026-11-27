package com.librompaz.app.domain

import com.librompaz.app.data.MockData
import com.librompaz.app.model.EstadoAsistencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** RN-05 (un registro por estudiante y fecha), RF-03 y fórmula de asistencia de RF-05. */
class AsistenciaTest {

    private val estudiantes3A = MockData.estudiantes.filter { it.cursoId == "C3A" }

    private fun todosPresentes() = estudiantes3A.associate { it.id to EstadoAsistencia.PRESENTE }

    @Test
    fun cadaEstudianteTieneUnSoloIdPorFecha() {
        val registros = construirRegistrosAsistencia("C3A", "2026-10-01", todosPresentes(), "Docente", "2026-10-01 08:00:00")
        assertEquals(estudiantes3A.size, registros.size)
        assertEquals(registros.size, registros.map { it.id }.toSet().size)
        assertEquals(registros.size, registros.map { it.estudianteId to it.fecha }.toSet().size)
    }

    @Test
    fun guardarDosVecesLaMismaFecha_producenLosMismosIds_yElSegundoReemplazaAlPrimero() {
        val primera = construirRegistrosAsistencia("C3A", "2026-10-01", todosPresentes(), "Docente", "2026-10-01 08:00:00")
        val corregido = todosPresentes() + ("E3A-2" to EstadoAsistencia.AUSENTE)
        val segunda = construirRegistrosAsistencia("C3A", "2026-10-01", corregido, "Docente", "2026-10-01 09:00:00")

        assertEquals(primera.map { it.id }.toSet(), segunda.map { it.id }.toSet())

        // Simula la tabla (clave = id, como REPLACE): al guardar de nuevo queda UNA fila por estudiante.
        val tabla = LinkedHashMap<String, com.librompaz.app.model.RegistroAsistencia>()
        (primera + segunda).forEach { tabla[it.id] = it }
        assertEquals(estudiantes3A.size, tabla.size)
        assertEquals(EstadoAsistencia.AUSENTE, tabla[idAsistencia("E3A-2", "2026-10-01")]?.estado)
    }

    @Test
    fun otraFecha_generaOtroRegistro() {
        assertTrue(idAsistencia("E3A-1", "2026-10-01") != idAsistencia("E3A-1", "2026-10-02"))
    }

    @Test
    fun laFirmaLaPoneElSistema() {
        val registro = construirRegistrosAsistencia("C3A", "2026-10-01", mapOf("E3A-1" to EstadoAsistencia.ATRASADO), "Docente Matemática (demo)", "2026-10-01 08:05:00").first()
        assertEquals("Docente Matemática (demo)", registro.registradoPor)
        assertEquals("2026-10-01 08:05:00", registro.fechaHoraRegistro)
    }

    @Test
    fun estudiantesAjenos_detectaIdsQueNoSonDelCurso() {
        val ids = estudiantes3A.map { it.id }.toSet()
        assertTrue(estudiantesAjenos(ids, todosPresentes()).isEmpty())
        assertEquals(listOf("E4A-1"), estudiantesAjenos(ids, mapOf("E3A-1" to EstadoAsistencia.PRESENTE, "E4A-1" to EstadoAsistencia.AUSENTE)))
    }

    @Test
    fun datosDeEjemplo_tienenUnRegistroPorEstudianteYFecha() {
        val claves = MockData.asistencias.map { it.estudianteId to it.fecha }
        assertEquals(claves.size, claves.toSet().size)
    }

    @Test
    fun idCalificacion_esUnoPorEstudianteYEvaluacion() {
        assertEquals("EV1_E3A-1", idCalificacion("EV1", "E3A-1"))
        assertNull(MockData.calificaciones.groupBy { it.id }.values.firstOrNull { it.size > 1 })
    }
}
