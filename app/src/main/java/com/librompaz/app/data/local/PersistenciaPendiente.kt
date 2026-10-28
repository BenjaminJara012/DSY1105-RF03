package com.librompaz.app.data.local

// =====================================================================================
// TODO(equipo): PERSISTENCIA LOCAL CON ROOM (OBLIGATORIA en este caso; Semana 9)
// =====================================================================================
// Las dependencias de Room, KSP y DataStore YA están configuradas en Gradle. Falta crear
// las clases en esta carpeta y reemplazar las listas de MockData que usa el ViewModel.
//
// 1) ENTIDADES (@Entity) — una por tabla del Modelo Relacional del equipo (sin duplicidad):
//      usuarios, cursos, asignaturas, estudiantes, cargas_docentes (docente-curso-asignatura),
//      asistencias, objetivos (OA), sesiones (+ tabla intermedia sesion_oa),
//      evaluaciones, calificaciones, anotaciones y auditoria.
//    Los enums se pueden guardar como String (enum.name); las listas (oaIds) en su propia
//    tabla intermedia o con un @TypeConverter.
//
// 2) DAOs (@Dao) — consultas que necesita cada pantalla, por ejemplo:
//      - cargas de un docente (RBAC)
//      - asistencia de un curso en una fecha; ausencias por estudiante
//      - sesiones de un curso y asignatura; OA trabajados (cobertura)
//      - evaluaciones y notas de un curso; evaluaciones con notas faltantes
//      - anotaciones de un estudiante; anotaciones negativas recientes
//      - auditoría ordenada por fecha
//    Regla: la auditoría es INALTERABLE -> su DAO solo necesita @Insert y consultas.
//    Guardar un registro y su auditoría juntos conviene hacerlo en una @Transaction.
//
// 3) BASE DE DATOS (@Database) — AppDatabase con las entidades, version = 1,
//    exportSchema = false, e instancia única.
//
// 4) DATASTORE (opcional) — recordar el último usuario que inició sesión.
//
// 5) VIEWMODEL — reemplazar MutableStateFlow(MockData...) por los Flow del DAO con
//    stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()).
//    Para tener Context, el ViewModel puede extender AndroidViewModel(application).
// =====================================================================================
