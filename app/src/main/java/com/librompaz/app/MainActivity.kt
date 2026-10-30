package com.librompaz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.librompaz.app.data.AsistenciaRepository
import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.navigation.AppNavigation
import com.librompaz.app.ui.theme.LibroMpazTheme
import com.librompaz.app.viewmodel.LibroMpazViewModel

class MainActivity : ComponentActivity() {

    // Crea un único ViewModel compartido por las pantallas.
    private val libroMpazViewModel: LibroMpazViewModel by viewModels {
        object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                require(
                    modelClass.isAssignableFrom(LibroMpazViewModel::class.java)
                )

                // Obtiene la base de datos Room.
                val database = AppDatabase.obtener(applicationContext)

                // Inyecta el Repository en el ViewModel.
                val repository = AsistenciaRepository(database)

                return LibroMpazViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            LibroMpazTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    // Comparte el mismo ViewModel en toda la navegación.
                    AppNavigation(viewModel = libroMpazViewModel)
                }
            }
        }
    }
}

