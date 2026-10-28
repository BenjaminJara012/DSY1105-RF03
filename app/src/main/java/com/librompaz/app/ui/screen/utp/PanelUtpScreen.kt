package com.librompaz.app.ui.screen.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.navigation.Rutas
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/** Pantalla principal de UTP: acceso a la cobertura curricular y a la revisión de notas. */
@Composable
fun PanelUtpScreen(
    navController: NavHostController,
    viewModel: LibroMpazViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()

    Scaffold(topBar = { BarraSuperior("Panel UTP", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)
            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = { navController.navigate(Rutas.UTP_COBERTURA) }
            ) { Text("Cobertura curricular por curso") }
            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = { navController.navigate(Rutas.UTP_CALIFICACIONES) }
            ) { Text("Revisión de calificaciones") }
        }
    }
}
