package com.librompaz.app.data.remote

// =====================================================================================
// TODO(equipo): CONSUMO DE API REST (Retrofit ya configurado en Gradle)
// =====================================================================================
// El caso pide consumir APIs REST de un backend en Spring Boot (microservicios). Está
// pendiente con el CITT si cada equipo construye ese backend o solo simula su consumo.
//
// Sugerencia de estructura en esta carpeta:
//   1) DTOs con los datos que se envían/reciben (por ejemplo AsistenciaDto, NotaDto).
//   2) Interfaz Retrofit, por ejemplo:
//          interface LibroApi {
//              @GET("cursos/{id}/estudiantes")
//              suspend fun estudiantes(@Path("id") cursoId: String): List<EstudianteDto>
//          }
//   3) Instancia de Retrofit (Retrofit.Builder + GsonConverterFactory) y, mientras no
//      exista el backend, una implementación simulada que lea desde Room.
//
// Restricciones del caso: sin integración con plataformas reales del MINEDUC ni con
// sistemas del sostenedor, y la app NO envía notificaciones, correos, SMS ni WhatsApp.
// =====================================================================================
