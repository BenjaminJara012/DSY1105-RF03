package com.librompaz.app.data.remote

import org.junit.Ignore
import org.junit.Test

/**
 * ESQUELETO (E3-9): pruebas de los mapeadores DTO <-> entidad. Usa los datos de com.librompaz.app.data.MockData
 * (los mismos de la semilla del servidor) como entradas.
 *
 * Casos que deben cubrir:
 *  C18 ida y vuelta de las entidades de referencia: Estudiante, CargaDocente y ObjetivoAprendizaje (entidad.aDto().aEntidad() == entidad)
 *  C19 ida y vuelta de Asistencia, Sesión (con su lista de OA), Evaluación, Calificación (la nota Double no cambia), Anotación y Auditoría
 *  C20 un enum desconocido descarta la fila: AsistenciaDto con estado "XYZ", AnotacionDto con tipo "XYZ", AuditoriaDto con perfil "XYZ"
 *      y UsuarioDto con perfil "ESTUDIANTE" (RN-01) devuelven null
 *  C21 Map<String, EstadoAsistencia>.aRegistrosDto() envía el nombre del estado ("AUSENTE") y conserva todos los estudiantes
 *  C22 armarNotasDto separa notas (Double) y sinNota
 *  C23 UsuarioDto.aEntidad(claveHash) conserva el hash recibido y nunca copia una clave (el DTO no la trae)
 *  C24 contrato: un JSON de ejemplo copiado de docs/API.md (backend) se deserializa con Gson en cada DTO sin campos nulos
 */
@Ignore("E3: completar")
class MapeadoresTest {

    @Test
    fun `C18 ida y vuelta de las entidades de referencia`() {
        TODO("E3: completar")
    }

    @Test
    fun `C19 ida y vuelta de las entidades con datos`() {
        TODO("E3: completar")
    }

    @Test
    fun `C20 un enum desconocido descarta la fila`() {
        TODO("E3: completar")
    }

    @Test
    fun `C21 aRegistrosDto envia el nombre del estado`() {
        TODO("E3: completar")
    }

    @Test
    fun `C22 armarNotasDto separa notas y sinNota`() {
        TODO("E3: completar")
    }

    @Test
    fun `C23 el usuario guarda el hash de la clave`() {
        TODO("E3: completar")
    }

    @Test
    fun `C24 los DTO coinciden con el JSON de la API`() {
        TODO("E3: completar")
    }
}
