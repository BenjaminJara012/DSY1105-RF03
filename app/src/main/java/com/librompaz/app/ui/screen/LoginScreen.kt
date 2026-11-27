package com.librompaz.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.librompaz.app.R
import com.librompaz.app.model.Perfil
import com.librompaz.app.ui.components.CampoTexto
import com.librompaz.app.ui.components.ServidorDialog
import com.librompaz.app.util.vibrar
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Autenticación y alcance por rol" (RF-01): inicio de sesión contra la tabla "usuarios" de
 * Room (modo local) o contra POST /auth/login (modo servidor). El botón "Servidor" abre el cuadro donde se
 * cambia entre modo local y modo servidor (BuildConfig.USAR_API solo define el valor inicial) y, en modo
 * servidor, la dirección de la API. Según el perfil (Docente, UTP, Inspectoría o Dirección), AppNavigation lleva a la pantalla
 * principal de cada uno. RN-01: no existe acceso para estudiantes ni apoderados, ni registro, ni
 * recuperación pública de clave.
 *
 * El estado del formulario (correo, clave y errores) vive en el ViewModel (LoginUiState); esta
 * pantalla solo lo dibuja y avisa los cambios. La clave se oculta por defecto (RNF-08).
 */
@Composable
fun LoginScreen(
    viewModel: LibroMpazViewModel,
    onIngresar: (Perfil) -> Unit
) {
    val estado by viewModel.login.collectAsState()
    val datosListos by viewModel.datosListos.collectAsState()
    val urlServidor by viewModel.servidorVigente.collectAsState()
    val modoLocal by viewModel.modoLocal.collectAsState()
    val contexto = LocalContext.current
    var mostrarServidor by rememberSaveable { mutableStateOf(false) }
    // rememberSaveable: se mantiene aunque se gire la pantalla.
    var claveVisible by rememberSaveable { mutableStateOf(false) }
    var encabezadoVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { encabezadoVisible = true }

    // Respuesta háptica cuando las credenciales son rechazadas.
    LaunchedEffect(estado.errores.general) {
        if (estado.errores.general != null) vibrar(contexto, 150L)
    }

    if (mostrarServidor) {
        ServidorDialog(viewModel = viewModel, onCerrar = { mostrarServidor = false })
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // El encabezado entra con un fundido y un deslizamiento corto al abrir la app.
            AnimatedVisibility(
                visible = encabezadoVisible,
                enter = fadeIn() + slideInVertically { -it / 4 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_app),
                        contentDescription = "Logo Libro MPAZ",
                        modifier = Modifier.size(110.dp)
                    )
                    Text("Libro MPAZ", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Libro digital de clases · Escuela Escritora Marcela Paz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CampoTexto(
                valor = estado.usuario,
                onValorChange = viewModel::onUsuarioChange,
                etiqueta = "Correo institucional",
                icono = Icons.Filled.Email,
                error = estado.errores.usuario,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = estado.clave,
                onValorChange = viewModel::onClaveChange,
                etiqueta = "Clave",
                icono = Icons.Filled.Lock,
                error = estado.errores.clave,
                iconoDeError = false,
                visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.ingresar(onIngresar) }),
                trailingIcon = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (claveVisible) "Ocultar clave" else "Mostrar clave"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Error general (credenciales incorrectas), con animación de aparición.
            AnimatedVisibility(visible = estado.errores.general != null, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(estado.errores.general.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = datosListos && !estado.ingresando,
                onClick = { viewModel.ingresar(onIngresar) }
            ) {
                if (!datosListos || estado.ingresando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Ingresar")
                }
            }

            // Cuadro "Servidor": interruptor de modo local / servidor y, en modo servidor, la dirección de la API
            // (se cambia sin recompilar, p. ej. la IP del computador del aula).
            TextButton(onClick = {
                viewModel.abrirServidor()
                mostrarServidor = true
            }) {
                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Servidor")
            }
            Text(
                if (modoLocal) "Modo local: sin servidor" else "Servidor: $urlServidor",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "Acceso exclusivo para el personal del establecimiento.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // RNF-11: la app no tiene validez oficial ante el MINEDUC ni el sostenedor.
            Text(
                "Prototipo académico de uso interno",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
