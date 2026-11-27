package com.librompaz.app.domain

import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.SesionClase
import com.librompaz.app.util.millisDesdeFecha

// =====================================================================================
// Reglas de validación de todos los formularios, como funciones puras (sin Android): se
// pueden probar con JUnit sin emulador (ver app/src/test/.../domain/ValidacionesTest.kt).
// Cada clase de errores usa null para decir "este campo está correcto"; así cada mensaje se
// muestra junto al campo que lo causa (isError + supportingText).
// =====================================================================================

const val NOTA_MINIMA = 1.0
const val NOTA_MAXIMA = 7.0

const val MENSAJE_NOTA_FUERA_DE_RANGO = "La nota debe estar entre 1,0 y 7,0"
const val MENSAJE_NOTA_INVALIDA = "Escribe un número válido (ej. 5,8)"
const val MENSAJE_NOTA_DECIMALES = "Usa un solo decimal (ej. 5,8)"
const val MENSAJE_EVALUACION_INEXISTENTE = "La evaluación no existe"
const val MENSAJE_ESTUDIANTE_AJENO = "El estudiante no pertenece a este curso"

private const val ANOTACION_MINIMO = 10
private const val ANOTACION_MAXIMO = 300
private const val CONTENIDO_MAXIMO = 500
private const val BLOQUE_MAXIMO = 30
private const val NOMBRE_EVALUACION_MAXIMO = 60

private val FORMATO_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")
private val FORMATO_FECHA = Regex("^\\d{4}-\\d{2}-\\d{2}$")
private val FORMATO_NUMERO = Regex("^-?\\d+([.,]\\d+)?$")

// ---------- Login (RF-01) ----------

data class ErroresLogin(
    val usuario: String? = null,
    val clave: String? = null,
    val general: String? = null
) {
    val hayErrores: Boolean get() = usuario != null || clave != null || general != null
}

/**
 * Solo valida que los campos estén completos y que el correo tenga forma de correo. No impone
 * largo mínimo a la clave: así el mensaje no revela reglas internas (RN-01, RNF-08).
 */
fun validarLogin(usuario: String, clave: String): ErroresLogin = ErroresLogin(
    usuario = when {
        usuario.isBlank() -> "El correo es obligatorio"
        !FORMATO_CORREO.matches(usuario.trim()) -> "El correo no tiene un formato válido"
        else -> null
    },
    clave = if (clave.isBlank()) "La clave es obligatoria" else null
)

// ---------- Fechas (asistencia, clases, evaluaciones) ----------

/** Fecha obligatoria, con formato AAAA-MM-DD, existente y no futura. [hoy] también va en AAAA-MM-DD. */
fun validarFecha(fecha: String, hoy: String): String? = when {
    fecha.isBlank() -> "La fecha es obligatoria"
    !FORMATO_FECHA.matches(fecha) || millisDesdeFecha(fecha) == null -> "Usa el formato AAAA-MM-DD"
    fecha > hoy -> "La fecha no puede ser futura"   // en AAAA-MM-DD el orden de texto = orden de fecha
    else -> null
}

// ---------- Calificaciones (RF-08, RN-04) ----------

/** Convierte la nota escrita ("5,5" o "5.5") a número. null si no es un número. */
fun parsearNota(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

/**
 * Error de una nota escrita, o null si es válida: número con coma o punto, entre 1,0 y 7,0 y con
 * un decimal como máximo. Una nota en blanco no es error: significa "sin nota".
 */
fun validarNota(texto: String): String? {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return null
    if (!FORMATO_NUMERO.matches(limpio)) return MENSAJE_NOTA_INVALIDA
    val nota = parsearNota(limpio) ?: return MENSAJE_NOTA_INVALIDA
    if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) return MENSAJE_NOTA_FUERA_DE_RANGO
    val decimales = limpio.substringAfter(if ('.' in limpio) '.' else ',', "").length
    return if (decimales > 1) MENSAJE_NOTA_DECIMALES else null
}

/** Errores de un lote de notas: [general] para problemas de la evaluación y uno por estudiante. */
data class ErroresNotas(
    val general: String? = null,
    val porEstudiante: Map<String, String> = emptyMap()
) {
    val hayErrores: Boolean get() = general != null || porEstudiante.isNotEmpty()
}

/**
 * RN-08: la evaluación debe existir y cada nota debe ser de un estudiante del mismo curso de la
 * evaluación. [textos] es estudianteId -> nota escrita; los textos en blanco se ignoran.
 */
fun validarNotasDeEvaluacion(
    evaluacion: Evaluacion?,
    estudiantesDelCurso: Set<String>,
    textos: Map<String, String>
): ErroresNotas {
    if (evaluacion == null) return ErroresNotas(general = MENSAJE_EVALUACION_INEXISTENTE)
    val errores = linkedMapOf<String, String>()
    for ((estudianteId, texto) in textos) {
        if (texto.isBlank()) continue
        val error = if (estudianteId !in estudiantesDelCurso) MENSAJE_ESTUDIANTE_AJENO else validarNota(texto)
        if (error != null) errores[estudianteId] = error
    }
    return ErroresNotas(porEstudiante = errores)
}

// ---------- Evaluaciones (RF-08) ----------

data class ErroresEvaluacion(
    val nombre: String? = null,
    val fecha: String? = null
) {
    val hayErrores: Boolean get() = nombre != null || fecha != null
}

fun validarEvaluacion(nombre: String, fecha: String, hoy: String): ErroresEvaluacion = ErroresEvaluacion(
    nombre = when {
        nombre.isBlank() -> "El nombre del instrumento es obligatorio"
        nombre.trim().length > NOMBRE_EVALUACION_MAXIMO -> "Máximo $NOMBRE_EVALUACION_MAXIMO caracteres"
        else -> null
    },
    fecha = validarFecha(fecha, hoy)
)

/** No repetir el mismo nombre en la misma fecha, curso y asignatura (sin distinguir mayúsculas). */
fun evaluacionDuplicada(
    existentes: List<Evaluacion>,
    cursoId: String,
    asignaturaId: String,
    nombre: String,
    fecha: String
): Boolean = existentes.any {
    it.cursoId == cursoId && it.asignaturaId == asignaturaId && it.fecha == fecha &&
        it.nombreInstrumento.trim().equals(nombre.trim(), ignoreCase = true)
}

// ---------- Registro de clase: OA y contenidos (RF-06) ----------

data class ErroresSesion(
    val fecha: String? = null,
    val bloque: String? = null,
    val objetivos: String? = null,
    val contenido: String? = null
) {
    val hayErrores: Boolean get() = fecha != null || bloque != null || objetivos != null || contenido != null
}

/** Al menos un OA; contenido y bloque obligatorios; fecha válida y no futura. */
fun validarSesion(fecha: String, bloque: String, oaIds: List<String>, contenido: String, hoy: String): ErroresSesion =
    ErroresSesion(
        fecha = validarFecha(fecha, hoy),
        bloque = when {
            bloque.isBlank() -> "Indica el bloque (ej: Bloque 1)"
            bloque.trim().length > BLOQUE_MAXIMO -> "Máximo $BLOQUE_MAXIMO caracteres"
            else -> null
        },
        objetivos = if (oaIds.isEmpty()) "Marca al menos un OA" else null,
        contenido = when {
            contenido.isBlank() -> "Describe los contenidos abordados"
            contenido.trim().length > CONTENIDO_MAXIMO -> "Máximo $CONTENIDO_MAXIMO caracteres"
            else -> null
        }
    )

/** No repetir la combinación curso + asignatura + fecha + bloque (el bloque sin distinguir mayúsculas). */
fun sesionDuplicada(
    existentes: List<SesionClase>,
    cursoId: String,
    asignaturaId: String,
    fecha: String,
    bloque: String
): Boolean = existentes.any {
    it.cursoId == cursoId && it.asignaturaId == asignaturaId && it.fecha == fecha &&
        it.bloque.trim().equals(bloque.trim(), ignoreCase = true)
}

// ---------- Hoja de vida (RF-10) ----------

data class ErroresAnotacion(
    val estudiante: String? = null,
    val tipo: String? = null,
    val descripcion: String? = null
) {
    val hayErrores: Boolean get() = estudiante != null || tipo != null || descripcion != null
}

fun validarAnotacion(hayEstudiante: Boolean, hayTipo: Boolean, descripcion: String): ErroresAnotacion =
    ErroresAnotacion(
        estudiante = if (!hayEstudiante) "Elige un estudiante" else null,
        tipo = if (!hayTipo) "Elige el tipo de anotación" else null,
        descripcion = when {
            descripcion.isBlank() -> "La descripción es obligatoria"
            descripcion.trim().length < ANOTACION_MINIMO -> "Escribe al menos $ANOTACION_MINIMO caracteres"
            descripcion.trim().length > ANOTACION_MAXIMO -> "Máximo $ANOTACION_MAXIMO caracteres"
            else -> null
        }
    )
