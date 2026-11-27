package com.librompaz.app.ui.screen.direccion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Registro de auditoría (RF-13, RN-02): quién creó, modificó o eliminó qué, y cuándo.
 * Es solo de lectura: la tabla "auditoria" no tiene operaciones de actualizar ni borrar.
 * Los más recientes aparecen primero.
 */
@Composable
fun AuditoriaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val registros by viewModel.auditoria.collectAsState()

    Scaffold(topBar = { BarraSuperior("Auditoría (${registros.size})", navController) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(registros, key = { it.id }) { registro ->
                Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${registro.accion} · ${registro.detalle}", style = MaterialTheme.typography.titleSmall)
                        Text("${registro.usuario} · ${registro.perfil.etiqueta}", style = MaterialTheme.typography.bodySmall)
                        Text(registro.fechaHora, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
