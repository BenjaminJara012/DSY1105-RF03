package com.librompaz.app.ui.screen.direccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
 * Registro de auditoría: quién creó o modificó qué, y cuándo. Regla de negocio: toda creación o
 * modificación queda auditada de forma automática e inalterable (por eso esta pantalla es solo
 * de lectura).
 *
 * TODO(equipo): hoy solo se ven los registros de ejemplo de MockData; las funciones que guardan
 * datos en el ViewModel deben agregar aquí su propio registro.
 */
@Composable
fun AuditoriaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val registros by viewModel.auditoria.collectAsState()

    Scaffold(topBar = { BarraSuperior("Auditoría", navController) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(registros) { registro ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${registro.accion} · ${registro.detalle}", style = MaterialTheme.typography.titleSmall)
                        Text("${registro.usuario} (${registro.perfil.etiqueta})", style = MaterialTheme.typography.bodySmall)
                        Text(registro.fechaHora, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
