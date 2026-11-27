package com.librompaz.app.domain

import java.security.MessageDigest

/**
 * Hash de claves (RNF-08): la base de datos nunca guarda la clave en texto plano.
 *
 * Se usa SHA-256 con una sal fija de demostración. En un sistema real se usaría un algoritmo
 * pensado para claves (bcrypt, scrypt o Argon2) con una sal distinta por usuario.
 */
object ClaveSegura {

    private const val SAL = "LibroMpaz-demo-sal-v1:"

    /** SHA-256 (hexadecimal en minúsculas) de la sal más la clave. */
    fun hash(clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((SAL + clave).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    /** Compara la clave escrita con el hash guardado, en tiempo constante. */
    fun coincide(clave: String, hashGuardado: String): Boolean =
        MessageDigest.isEqual(hash(clave).toByteArray(Charsets.UTF_8), hashGuardado.toByteArray(Charsets.UTF_8))
}
