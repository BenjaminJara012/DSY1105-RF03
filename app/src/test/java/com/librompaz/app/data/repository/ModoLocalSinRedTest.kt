package com.librompaz.app.data.repository

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * RN-01 y RNF-07 (flujo aislado): el código del modo local no puede abrir conexiones. Revisa el
 * texto de sus fuentes: no importan nada de red (OkHttp, Retrofit, java.net) ni el cliente de la API,
 * y no contienen direcciones web.
 */
class ModoLocalSinRedTest {

    private fun fuente(nombre: String): String {
        val candidatos = listOf("src/main/java", "app/src/main/java")
        val raiz = candidatos.map { File(it) }.firstOrNull { it.isDirectory }
            ?: throw AssertionError("No se encontró src/main/java desde ${File(".").absolutePath}")
        val archivo = raiz.walkTopDown().firstOrNull { it.isFile && it.name == nombre }
            ?: throw AssertionError("No se encontró $nombre")
        // Se quitan los comentarios: lo que importa es el código.
        return archivo.readText()
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .lines().joinToString("\n") { it.substringBefore("//") }
    }

    private val prohibidos = listOf(
        "okhttp3", "retrofit2", "java.net", "javax.net", "android.net", "ApiClient", "ApiService", "apiProvider",
        "http://", "https://", "Socket", "URL("
    )

    @Test
    fun lasClasesDelModoLocalNoTocanLaRed() {
        for (nombre in listOf("LibroMpazRepository.kt", "LibroMpazDatos.kt", "PreferenciaModo.kt", "AlmacenModoPreferencias.kt", "ReglasDeEscritura.kt")) {
            val codigo = fuente(nombre)
            for (palabra in prohibidos) {
                assertTrue("$nombre no debe contener \"$palabra\"", !codigo.contains(palabra))
            }
        }
    }

    @Test
    fun elSelectorSoloConoceElServidorPorLaInterfazFuenteDatos() {
        val codigo = fuente("SelectorFuente.kt")
        for (palabra in prohibidos) assertTrue("SelectorFuente.kt no debe contener \"$palabra\"", !codigo.contains(palabra))
    }
}
