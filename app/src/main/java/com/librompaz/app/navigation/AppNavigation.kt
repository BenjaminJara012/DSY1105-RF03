package com.librompaz.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.librompaz.app.model.Perfil
import com.librompaz.app.ui.screen.LoginScreen
import com.librompaz.app.ui.screen.comun.AsistenciaScreen
import com.librompaz.app.ui.screen.direccion.AuditoriaScreen
import com.librompaz.app.ui.screen.direccion.PanelDireccionScreen
import com.librompaz.app.ui.screen.docente.CalificacionesScreen
import com.librompaz.app.ui.screen.docente.CursoDocenteScreen
import com.librompaz.app.ui.screen.docente.HojaDeVidaScreen
import com.librompaz.app.ui.screen.docente.MisCursosScreen
import com.librompaz.app.ui.screen.docente.RegistroClaseScreen
import com.librompaz.app.ui.screen.inspectoria.AlertasInspectoriaScreen
import com.librompaz.app.ui.screen.inspectoria.PanelInspectoriaScreen
import com.librompaz.app.ui.screen.utp.CoberturaCurricularScreen
import com.librompaz.app.ui.screen.utp.PanelUtpScreen
import com.librompaz.app.ui.screen.utp.RevisionCalificacionesScreen
import com.librompaz.app.viewmodel.LibroMpazViewModel

object Rutas {
    const val LOGIN = "login"

    const val DOC_CURSOS = "docente_cursos"
    const val DOC_CURSO = "docente_curso"
    const val DOC_CLASE = "docente_clase"
    const val DOC_CALIFICACIONES = "docente_calificaciones"
    const val DOC_HOJA_VIDA = "docente_hoja_vida"

    /** Compartida por Docente (su curso) e Inspectoría (cualquier curso). */
    const val ASISTENCIA = "asistencia"

    const val UTP_PANEL = "utp_panel"
    const val UTP_COBERTURA = "utp_cobertura"
    const val UTP_CALIFICACIONES = "utp_calificaciones"

    const val INSP_PANEL = "inspectoria_panel"
    const val INSP_ALERTAS = "inspectoria_alertas"

    const val DIR_PANEL = "direccion_panel"
    const val DIR_AUDITORIA = "direccion_auditoria"
}

/**
 * Flujo de pantallas (un solo NavHost y un único LibroMpazViewModel compartido):
 *
 *   LOGIN ──(según perfil)──> DOCENTE: Mis cursos ──> Curso ──> Asistencia | Clase (OA) |
 *                                                               Calificaciones | Hoja de vida
 *                        ──> UTP: Panel ──> Cobertura curricular | Revisión de calificaciones
 *                        ──> INSPECTORÍA: Panel ──> Asistencia (cualquier curso) | Alertas
 *                        ──> DIRECCIÓN: Panel ──> Auditoría
 *
 * El curso y la asignatura elegidos se comparten a través del ViewModel (como en la Guía 11).
 * Regla del caso: no existe ningún acceso para estudiantes ni apoderados.
 *
 * Botón atrás: el atrás nativo de Android funciona solo (lo maneja el NavHost). Al ingresar,
 * LOGIN sale de la pila; "Cerrar sesión" limpia la pila completa.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: LibroMpazViewModel = viewModel()
) {
    val cerrarSesion: () -> Unit = {
        viewModel.logout()
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onIngresar = { perfil ->
                    val destino = when (perfil) {
                        Perfil.DOCENTE -> Rutas.DOC_CURSOS
                        Perfil.UTP -> Rutas.UTP_PANEL
                        Perfil.INSPECTORIA -> Rutas.INSP_PANEL
                        Perfil.DIRECCION -> Rutas.DIR_PANEL
                    }
                    navController.navigate(destino) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ---- Docente ----
        composable(Rutas.DOC_CURSOS) { MisCursosScreen(navController, viewModel, onCerrarSesion = cerrarSesion) }
        composable(Rutas.DOC_CURSO) { CursoDocenteScreen(navController, viewModel) }
        composable(Rutas.DOC_CLASE) { RegistroClaseScreen(navController, viewModel) }
        composable(Rutas.DOC_CALIFICACIONES) { CalificacionesScreen(navController, viewModel) }
        composable(Rutas.DOC_HOJA_VIDA) { HojaDeVidaScreen(navController, viewModel) }
        composable(Rutas.ASISTENCIA) { AsistenciaScreen(navController, viewModel) }

        // ---- UTP ----
        composable(Rutas.UTP_PANEL) { PanelUtpScreen(navController, viewModel, onCerrarSesion = cerrarSesion) }
        composable(Rutas.UTP_COBERTURA) { CoberturaCurricularScreen(navController, viewModel) }
        composable(Rutas.UTP_CALIFICACIONES) { RevisionCalificacionesScreen(navController, viewModel) }

        // ---- Inspectoría ----
        composable(Rutas.INSP_PANEL) { PanelInspectoriaScreen(navController, viewModel, onCerrarSesion = cerrarSesion) }
        composable(Rutas.INSP_ALERTAS) { AlertasInspectoriaScreen(navController, viewModel) }

        // ---- Dirección ----
        composable(Rutas.DIR_PANEL) { PanelDireccionScreen(navController, viewModel, onCerrarSesion = cerrarSesion) }
        composable(Rutas.DIR_AUDITORIA) { AuditoriaScreen(navController, viewModel) }
    }
}
