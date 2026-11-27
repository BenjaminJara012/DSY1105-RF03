package com.librompaz.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.librompaz.app.model.Perfil
import com.librompaz.app.ui.components.BannerSincronizacion
import com.librompaz.app.ui.components.FranjaModo
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
 * El curso y la asignatura elegidos se comparten a través del ViewModel.
 * Regla de negocio: no existe ningún acceso para estudiantes ni apoderados.
 *
 * Botón atrás: el atrás nativo de Android funciona solo (lo maneja el NavHost). Al ingresar,
 * LOGIN sale de la pila; "Cerrar sesión" limpia la pila completa.
 *
 * RBAC (RF-02): cada ruta indica qué perfiles pueden verla (función protegida). Si no hay sesión
 * o el perfil no corresponde, se vuelve al login.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: LibroMpazViewModel = viewModel()
) {
    val usuario by viewModel.usuarioActivo.collectAsState()

    val irAlLogin: () -> Unit = {
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    val cerrarSesion: () -> Unit = {
        viewModel.logout()
        irAlLogin()
    }

    /** Muestra la pantalla solo si el usuario activo tiene uno de los perfiles permitidos. */
    @Composable
    fun protegida(vararg permitidos: Perfil, contenido: @Composable () -> Unit) {
        if (usuario?.perfil in permitidos) {
            contenido()
        } else {
            // Al cerrar sesión ya se navegó al login: solo se redirige si todavía no estamos ahí.
            LaunchedEffect(Unit) {
                if (navController.currentDestination?.route != Rutas.LOGIN) irAlLogin()
            }
        }
    }

    // Banner de conexión (sincronizando, sin conexión, reintentar): va bajo todas las pantallas y solo aparece
    // en modo servidor cuando hay algo que avisar; en modo local no se muestra. La franja "Modo local / Modo servidor"
    // va justo debajo y siempre está visible.
    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Rutas.LOGIN,
            modifier = Modifier.weight(1f),
            // Transiciones: la pantalla nueva entra deslizándose y la anterior se desvanece.
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
            }
        ) {
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
            composable(Rutas.DOC_CURSOS) { protegida(Perfil.DOCENTE) { MisCursosScreen(navController, viewModel, onCerrarSesion = cerrarSesion) } }
            composable(Rutas.DOC_CURSO) { protegida(Perfil.DOCENTE) { CursoDocenteScreen(navController, viewModel) } }
            composable(Rutas.DOC_CLASE) { protegida(Perfil.DOCENTE) { RegistroClaseScreen(navController, viewModel) } }
            composable(Rutas.DOC_CALIFICACIONES) { protegida(Perfil.DOCENTE) { CalificacionesScreen(navController, viewModel) } }
            composable(Rutas.DOC_HOJA_VIDA) { protegida(Perfil.DOCENTE) { HojaDeVidaScreen(navController, viewModel) } }
            composable(Rutas.ASISTENCIA) { protegida(Perfil.DOCENTE, Perfil.INSPECTORIA) { AsistenciaScreen(navController, viewModel) } }

            // ---- UTP ----
            composable(Rutas.UTP_PANEL) { protegida(Perfil.UTP) { PanelUtpScreen(navController, viewModel, onCerrarSesion = cerrarSesion) } }
            composable(Rutas.UTP_COBERTURA) { protegida(Perfil.UTP) { CoberturaCurricularScreen(navController, viewModel) } }
            composable(Rutas.UTP_CALIFICACIONES) { protegida(Perfil.UTP) { RevisionCalificacionesScreen(navController, viewModel) } }

            // ---- Inspectoría ----
            composable(Rutas.INSP_PANEL) { protegida(Perfil.INSPECTORIA) { PanelInspectoriaScreen(navController, viewModel, onCerrarSesion = cerrarSesion) } }
            composable(Rutas.INSP_ALERTAS) { protegida(Perfil.INSPECTORIA) { AlertasInspectoriaScreen(navController, viewModel) } }

            // ---- Dirección ----
            composable(Rutas.DIR_PANEL) { protegida(Perfil.DIRECCION) { PanelDireccionScreen(navController, viewModel, onCerrarSesion = cerrarSesion) } }
            composable(Rutas.DIR_AUDITORIA) { protegida(Perfil.DIRECCION) { AuditoriaScreen(navController, viewModel) } }
        }
        BannerSincronizacion(viewModel)
        FranjaModo(viewModel)   // "Modo local" / "Modo servidor": siempre visible
    }
}
