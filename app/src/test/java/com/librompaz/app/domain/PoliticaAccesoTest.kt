package com.librompaz.app.domain

import com.librompaz.app.data.MockData
import com.librompaz.app.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** RF-02 (RBAC) y RN-03 (el docente solo opera sobre sus cargas), con los usuarios y cargas de ejemplo. */
class PoliticaAccesoTest {

    private val cargas = MockData.cargas
    private fun usuario(correo: String): Usuario = MockData.usuarios.first { it.usuario == correo }

    private val docenteMat = usuario("docente.mat@libromp.cl")   // Matemática 3°A y 4°A
    private val docenteLen = usuario("docente.len@libromp.cl")   // Lenguaje 4°A
    private val utp = usuario("utp@libromp.cl")
    private val inspectoria = usuario("inspectoria@libromp.cl")
    private val direccion = usuario("direccion@libromp.cl")

    // ---------- RN-03 ----------

    @Test
    fun docente_soloTieneCargaEnSusCursosYAsignaturas() {
        assertTrue(PoliticaAcceso.tieneCarga(docenteMat, cargas, "C3A", "MAT"))
        assertTrue(PoliticaAcceso.tieneCarga(docenteMat, cargas, "C4A", "MAT"))
        assertFalse(PoliticaAcceso.tieneCarga(docenteMat, cargas, "C4A", "LEN"))
        assertTrue(PoliticaAcceso.tieneCarga(docenteLen, cargas, "C4A", "LEN"))
        assertFalse(PoliticaAcceso.tieneCarga(docenteLen, cargas, "C3A", "MAT"))
        assertFalse(PoliticaAcceso.tieneCarga(docenteLen, cargas, "C3A"))
    }

    @Test
    fun docenteDeLenguaje4A_noPuedeGuardarSesionDeMatematica3A() {
        val error = PoliticaAcceso.verificarCarga(docenteLen, "C3A", "MAT", cargas)
        assertEquals("No tienes asignado este curso o asignatura.", error)
    }

    @Test
    fun docente_puedeGuardarSesionEvaluacionYNotasEnSuCarga() {
        assertNull(PoliticaAcceso.verificarCarga(docenteMat, "C3A", "MAT", cargas))
        assertNull(PoliticaAcceso.verificarCarga(docenteLen, "C4A", "LEN", cargas))
    }

    @Test
    fun docente_noPuedeUsarLaAsignaturaDeOtraCarga() {
        // El docente de Matemática tiene 4°A, pero no Lenguaje en 4°A.
        assertEquals(PoliticaAcceso.MENSAJE_SIN_CARGA, PoliticaAcceso.verificarCarga(docenteMat, "C4A", "LEN", cargas))
    }

    // ---------- RF-02: asistencia ----------

    @Test
    fun utpYDireccion_noGuardanAsistencia() {
        assertEquals(PoliticaAcceso.MENSAJE_SIN_PERMISO, PoliticaAcceso.verificarAsistencia(utp, "C3A", cargas))
        assertEquals(PoliticaAcceso.MENSAJE_SIN_PERMISO, PoliticaAcceso.verificarAsistencia(direccion, "C3A", cargas))
    }

    @Test
    fun inspectoria_guardaAsistenciaDeCualquierCurso() {
        assertNull(PoliticaAcceso.verificarAsistencia(inspectoria, "C3A", cargas))
        assertNull(PoliticaAcceso.verificarAsistencia(inspectoria, "C4A", cargas))
    }

    @Test
    fun docente_guardaAsistenciaSoloDeSusCursos() {
        assertNull(PoliticaAcceso.verificarAsistencia(docenteMat, "C3A", cargas))
        assertEquals(PoliticaAcceso.MENSAJE_SIN_CARGA, PoliticaAcceso.verificarAsistencia(docenteLen, "C3A", cargas))
    }

    // ---------- RF-02: resto de las escrituras ----------

    @Test
    fun soloElDocenteEscribeClasesNotasYAnotaciones() {
        listOf(utp, inspectoria, direccion).forEach { perfil ->
            assertEquals(PoliticaAcceso.MENSAJE_SIN_PERMISO, PoliticaAcceso.verificarCarga(perfil, "C3A", "MAT", cargas))
            assertEquals(PoliticaAcceso.MENSAJE_SIN_PERMISO, PoliticaAcceso.verificarAnotacion(perfil, "C3A", cargas))
        }
    }

    @Test
    fun anotacion_soloDeEstudiantesDeCursosDelDocente() {
        assertNull(PoliticaAcceso.verificarAnotacion(docenteMat, "C3A", cargas))
        assertEquals(PoliticaAcceso.MENSAJE_SIN_CARGA, PoliticaAcceso.verificarAnotacion(docenteLen, "C3A", cargas))
    }

    @Test
    fun sinSesion_ningunaFuncionGuarda() {
        assertEquals(PoliticaAcceso.MENSAJE_SIN_SESION, PoliticaAcceso.verificarAsistencia(null, "C3A", cargas))
        assertEquals(PoliticaAcceso.MENSAJE_SIN_SESION, PoliticaAcceso.verificarCarga(null, "C3A", "MAT", cargas))
        assertEquals(PoliticaAcceso.MENSAJE_SIN_SESION, PoliticaAcceso.verificarAnotacion(null, "C3A", cargas))
    }

    @Test
    fun soloExistenLosCuatroPerfilesInternos() {
        // RN-01: no hay perfil de estudiante ni de apoderado.
        assertEquals(
            setOf("DOCENTE", "UTP", "INSPECTORIA", "DIRECCION"),
            com.librompaz.app.model.Perfil.entries.map { it.name }.toSet()
        )
    }
}
