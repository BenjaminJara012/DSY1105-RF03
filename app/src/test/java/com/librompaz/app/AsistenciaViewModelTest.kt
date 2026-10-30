
package com.librompaz.app

import com.librompaz.app.viewmodel.LibroMpazViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de las validaciones del RF-03.
 */
class AsistenciaViewModelTest {

    private val viewModel = LibroMpazViewModel()

    @Test
    fun usuarioSinSesionNoPuedeRegistrar() {
        // Sin usuario autenticado, se rechaza la operación.
        assertFalse(
            viewModel.puedeRegistrarAsistencia("curso-1")
        )
    }

    @Test
    fun cursoSinEstudiantesEsRechazado() {
        // Un curso inexistente no tiene estudiantes asociados.
        assertFalse(
            viewModel.validarEstudiantesAsistencia(
                "curso-inexistente",
                emptyMap()
            )
        )
    }

    @Test
    fun estudiantesIncompletosSonRechazados() {
        // Buscamos un curso con estudiantes en los datos ficticios.
        val curso = viewModel.cursos.firstOrNull {
            viewModel.estudiantesDelCurso(it.id).isNotEmpty()
        } ?: error("No existe un curso con estudiantes para realizar la prueba")

        // Un mapa vacío no incluye a los estudiantes esperados.
        assertFalse(
            viewModel.validarEstudiantesAsistencia(
                curso.id,
                emptyMap()
            )
        )
    }

    @Test
    fun estudiantesCompletosSonAceptados() {
        // Seleccionamos un curso con estudiantes.
        val curso = viewModel.cursos.firstOrNull {
            viewModel.estudiantesDelCurso(it.id).isNotEmpty()
        } ?: error("No existe un curso con estudiantes para realizar la prueba")

        // Todos los estudiantes reciben un estado válido.
        val estados = viewModel.estudiantesDelCurso(curso.id)
            .associate { it.id to com.librompaz.app.model.EstadoAsistencia.PRESENTE }

        assertTrue(
            viewModel.validarEstudiantesAsistencia(
                curso.id,
                estados
            )
        )
    }
}
