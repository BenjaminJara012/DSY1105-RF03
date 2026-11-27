package com.librompaz.app.data.remote

import org.junit.Ignore
import org.junit.Test

/**
 * ESQUELETO (E3-4 y E3-5): pruebas de ConfigServidor, la regla de aislamiento RN-01 / RNF-07 (la app solo
 * habla con la red local). Son funciones puras: no necesitan Android ni red.
 *
 * Para completarlas: quita el @Ignore de la clase, escribe el cuerpo de cada prueba (reemplaza el TODO) con
 * assertEquals / assertTrue / assertFalse de org.junit.Assert y ejecútalas con ./gradlew testDebugUnitTest.
 *
 * Casos que deben cubrir (cada uno es una prueba de abajo):
 *  C01 hosts locales aceptados (localhost, 127.0.0.1, 10.0.2.2, 172.16.0.1, 172.31.255.255, 192.168.1.10, aula-pc)
 *  C02 hosts públicos o mal formados rechazados (8.8.8.8, 172.32.0.1, 192.169.0.1, 300.1.1.1, 192.168.1, www.google.com)
 *  C03 IPv6: ::1 y fe80::1 son locales; 2001:db8::1 no
 *  C04 validarUrl normaliza: "10.0.2.2:3000" -> Valida("http://10.0.2.2:3000/"); "HTTP://Aula-PC" -> Valida("http://aula-pc/")
 *  C05 validarUrl rechaza una URL pública con MENSAJE_NO_LOCAL (https://api.ejemplo.com, http://8.8.8.8:3000)
 *  C06 validarUrl: vacía -> MENSAJE_VACIA; ftp:// -> MENSAJE_ESQUEMA; puerto 99999 -> MENSAJE_PUERTO; con ruta -> MENSAJE_RUTA
 */
@Ignore("E3: completar")
class ConfigServidorTest {

    @Test
    fun `C01 acepta hosts de la red local`() {
        TODO("E3: completar")
    }

    @Test
    fun `C02 rechaza hosts publicos o mal formados`() {
        TODO("E3: completar")
    }

    @Test
    fun `C03 reconoce direcciones IPv6 locales`() {
        TODO("E3: completar")
    }

    @Test
    fun `C04 validarUrl normaliza la direccion`() {
        TODO("E3: completar")
    }

    @Test
    fun `C05 validarUrl rechaza una direccion publica`() {
        TODO("E3: completar")
    }

    @Test
    fun `C06 validarUrl informa cada error de formato`() {
        TODO("E3: completar")
    }
}
