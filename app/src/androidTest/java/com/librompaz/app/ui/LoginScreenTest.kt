package com.librompaz.app.ui

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

/**
 * ESQUELETO (Parte 4 del enunciado): pruebas de interfaz con Compose UI Test. Se ejecutan en un emulador o
 * dispositivo (./gradlew connectedDebugAndroidTest o el botón "Run" de Android Studio sobre esta clase).
 *
 * Armado típico:
 *   val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
 *   val viewModel = com.librompaz.app.viewmodel.LibroMpazViewModel(app)
 *   reglaCompose.setContent { LibroMpazTheme { LoginScreen(viewModel = viewModel, onIngresar = {}) } }
 * y se busca con reglaCompose.onNodeWithText("Ingresar").performClick() / .assertIsDisplayed() (imports de
 * androidx.compose.ui.test). Para ApplicationProvider agrega androidTestImplementation("androidx.test:core:1.6.1") si
 * Android Studio no lo resuelve.
 *
 * Entrega mínima: dejar UNA de estas pruebas funcionando (sin @Ignore) y las otras dos descritas.
 * Casos:
 *  U01 con el correo y la clave vacíos, al tocar "Ingresar" aparecen los mensajes de campo obligatorio junto a cada campo
 *  U02 la clave está oculta por defecto y el botón de la clave la muestra y la vuelve a ocultar (RNF-08)
 *  U03 con USAR_API = true: el botón "Servidor" abre el cuadro y una dirección pública (https://api.ejemplo.com) muestra
 *      el mensaje de "red local" sin guardar nada (RN-01, RNF-07)
 */
@Ignore("E3: completar")
class LoginScreenTest {

    @get:Rule
    val reglaCompose = createComposeRule()

    @Test
    fun camposVaciosMuestranErrorJuntoAlCampo() {
        TODO("E3: completar")
    }

    @Test
    fun laClaveEstaOcultaPorDefecto() {
        TODO("E3: completar")
    }

    @Test
    fun elCuadroServidorRechazaUnaDireccionPublica() {
        TODO("E3: completar")
    }
}
