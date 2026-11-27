package com.librompaz.app.domain

import com.librompaz.app.data.MockData
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.SesionClase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas de validación: login, fechas, notas (RN-04), evaluaciones, clases y anotaciones. */
class ValidacionesTest {

    private val hoy = "2026-10-29"

    // ---------- RF-01: login ----------

    @Test
    fun login_camposVacios_sonObligatorios() {
        val errores = validarLogin("", "  ")
        assertEquals("El correo es obligatorio", errores.usuario)
        assertEquals("La clave es obligatoria", errores.clave)
    }

    @Test
    fun login_correoSinFormato_tieneError() {
        assertEquals("El correo no tiene un formato válido", validarLogin("docente.mat", "x").usuario)
    }

    @Test
    fun login_noRevelaReglasDeLaClave() {
        // Una clave corta no es error de formato: se resuelve como "Correo o clave incorrectos".
        assertFalse(validarLogin("docente.mat@libromp.cl", "abc").hayErrores)
        assertFalse(validarLogin("docente.mat@libromp.cl", "docente123").hayErrores)
    }

    // ---------- Fechas ----------

    @Test
    fun fecha_futuraInexistenteOMalFormada_tieneError() {
        assertEquals("La fecha no puede ser futura", validarFecha("2026-12-01", hoy))
        assertEquals("Usa el formato AAAA-MM-DD", validarFecha("2026-02-31", hoy))
        assertEquals("Usa el formato AAAA-MM-DD", validarFecha("29/10/2026", hoy))
        assertEquals("La fecha es obligatoria", validarFecha("", hoy))
    }

    @Test
    fun fecha_deHoyOAnterior_esValida() {
        assertNull(validarFecha(hoy, hoy))
        assertNull(validarFecha("2026-10-01", hoy))
    }

    // ---------- RN-04: notas de 1,0 a 7,0 ----------

    @Test
    fun nota_dentroDeRango_esValida() {
        listOf("1,0", "1", "5.5", "5,8", "6.0", "7", "7,0", " 4,5 ").forEach {
            assertNull("'$it' debería ser válida", validarNota(it))
        }
    }

    @Test
    fun nota_fueraDeRango_muestraElMensajeDelCaso() {
        listOf("7,5", "0,9", "0", "8", "55", "-3", "7,1").forEach {
            assertEquals("'$it'", "La nota debe estar entre 1,0 y 7,0", validarNota(it))
        }
    }

    @Test
    fun nota_conTextoOFormatoRaro_pideUnNumeroValido() {
        listOf("abc", "seis", "5,", ",5", "5,5,5", "NaN", "1e1").forEach {
            assertEquals("'$it'", "Escribe un número válido (ej. 5,8)", validarNota(it))
        }
    }

    @Test
    fun nota_conMasDeUnDecimal_tieneError() {
        assertEquals("Usa un solo decimal (ej. 5,8)", validarNota("5,55"))
        assertEquals("Usa un solo decimal (ej. 5,8)", validarNota("6.25"))
    }

    @Test
    fun nota_enBlanco_noEsError() {
        assertNull(validarNota(""))
        assertNull(validarNota("   "))
    }

    @Test
    fun parsearNota_aceptaComaYPunto_yGuardaElMismoValor() {
        assertEquals(5.8, parsearNota("5,8")!!, 0.0001)
        assertEquals(5.8, parsearNota("5.8")!!, 0.0001)
        assertNull(parsearNota("abc"))
    }

    // ---------- RN-08: integridad nota - evaluación - curso ----------

    private val ev1 = MockData.evaluaciones.first()
    private val estudiantesDe3A = MockData.estudiantes.filter { it.cursoId == ev1.cursoId }.map { it.id }.toSet()

    @Test
    fun lote_evaluacionInexistente_seRechaza() {
        val errores = validarNotasDeEvaluacion(null, estudiantesDe3A, mapOf("E3A-1" to "5,0"))
        assertEquals("La evaluación no existe", errores.general)
    }

    @Test
    fun lote_notaDeEstudianteDeOtroCurso_seRechaza() {
        val errores = validarNotasDeEvaluacion(ev1, estudiantesDe3A, mapOf("E4A-1" to "5,0"))
        assertEquals("El estudiante no pertenece a este curso", errores.porEstudiante["E4A-1"])
        assertTrue(errores.hayErrores)
    }

    @Test
    fun lote_conUnaNotaInvalida_noPasa_yIgnoraLosVacios() {
        val errores = validarNotasDeEvaluacion(
            ev1, estudiantesDe3A, mapOf("E3A-1" to "7,5", "E3A-2" to "5,8", "E3A-3" to "")
        )
        assertEquals(setOf("E3A-1"), errores.porEstudiante.keys)
        assertFalse(validarNotasDeEvaluacion(ev1, estudiantesDe3A, mapOf("E3A-3" to "5,8", "E3A-4" to "")).hayErrores)
    }

    // ---------- RF-08: evaluaciones ----------

    @Test
    fun evaluacion_sinNombre_noSeCrea() {
        val errores = validarEvaluacion("  ", hoy, hoy)
        assertTrue(errores.hayErrores)
        assertNotNull(errores.nombre)
        assertNull(errores.fecha)
    }

    @Test
    fun evaluacion_conFechaFutura_tieneErrorEnLaFecha() {
        assertEquals("La fecha no puede ser futura", validarEvaluacion("Prueba 2", "2026-11-30", hoy).fecha)
    }

    @Test
    fun evaluacion_mismoNombreEnLaMismaFecha_esDuplicada() {
        val existentes = listOf(Evaluacion("EV9", "C3A", "MAT", "Prueba unidad 1", "2026-09-25"))
        assertTrue(evaluacionDuplicada(existentes, "C3A", "MAT", " prueba UNIDAD 1 ", "2026-09-25"))
        assertFalse(evaluacionDuplicada(existentes, "C3A", "MAT", "Prueba unidad 1", "2026-09-26"))
        assertFalse(evaluacionDuplicada(existentes, "C4A", "MAT", "Prueba unidad 1", "2026-09-25"))
        assertFalse(evaluacionDuplicada(existentes, "C3A", "LEN", "Prueba unidad 1", "2026-09-25"))
    }

    // ---------- RF-06: clases con OA ----------

    @Test
    fun sesion_sinOa_noSeGuarda() {
        val errores = validarSesion(hoy, "Bloque 1", emptyList(), "Conteo de 10 en 10", hoy)
        assertEquals("Marca al menos un OA", errores.objetivos)
        assertNull(errores.bloque)
        assertNull(errores.contenido)
    }

    @Test
    fun sesion_sinBloqueNiContenido_tieneErroresPorCampo() {
        val errores = validarSesion(hoy, " ", listOf("OA-MAT-2"), "", hoy)
        assertNotNull(errores.bloque)
        assertNotNull(errores.contenido)
        assertNull(errores.objetivos)
        assertNull(errores.fecha)
    }

    @Test
    fun sesion_completa_esValida() {
        assertFalse(validarSesion(hoy, "Bloque 2", listOf("OA-MAT-2"), "Representación de números", hoy).hayErrores)
    }

    @Test
    fun sesion_mismaCombinacionCursoAsignaturaFechaBloque_esDuplicada() {
        val s1 = MockData.sesiones.first()
        assertTrue(sesionDuplicada(MockData.sesiones, "C3A", "MAT", s1.fecha, " bloque 1 "))
        assertFalse(sesionDuplicada(MockData.sesiones, "C3A", "MAT", s1.fecha, "Bloque 2"))
        assertFalse(sesionDuplicada(MockData.sesiones, "C3A", "MAT", "2026-10-02", "Bloque 1"))
        assertFalse(sesionDuplicada(MockData.sesiones, "C4A", "MAT", s1.fecha, "Bloque 1"))
        val otra: List<SesionClase> = emptyList()
        assertFalse(sesionDuplicada(otra, "C3A", "MAT", s1.fecha, "Bloque 1"))
    }

    // ---------- RF-10: anotaciones ----------

    @Test
    fun anotacion_completa_esValida() {
        assertFalse(validarAnotacion(true, true, "Participa activamente en clases.").hayErrores)
    }

    @Test
    fun anotacion_sinEstudianteTipoODescripcion_tieneErroresPorCampo() {
        val errores = validarAnotacion(false, false, "")
        assertNotNull(errores.estudiante)
        assertNotNull(errores.tipo)
        assertEquals("La descripción es obligatoria", errores.descripcion)
    }

    @Test
    fun anotacion_descripcionMuyCortaOMuyLarga_tieneError() {
        assertEquals("Escribe al menos 10 caracteres", validarAnotacion(true, true, "Corta").descripcion)
        assertEquals("Máximo 300 caracteres", validarAnotacion(true, true, "x".repeat(301)).descripcion)
        assertNull(validarAnotacion(true, true, "x".repeat(300)).descripcion)
    }
}
