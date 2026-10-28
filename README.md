# Libro MPAZ — Proyecto base (Sección DSY1105-005, Maipú)

Proyecto Android base para el caso de Vinculación con el Medio **"Libro Digital de Clases Interactivo 'Libro MPAZ' - Mpazbook"** de la Escuela Escritora Marcela Paz. Es el punto de partida común de todos los equipos de la sección: trae la estructura, las pantallas de cada módulo y perfil, la navegación y la persistencia y la API configuradas. **La lógica de negocio del caso no está implementada**: esa es la parte que construye cada equipo.

Documentación del caso (carpeta `docs/`):

- `Requerimientos_DSY1105-005.pdf`: requerimientos funcionales, reglas de negocio, RNF y supuestos.
- `Consultas_Pendientes_DSY1105-005.pdf`: preguntas abiertas al CITT (Christian Lazcano). No resolver con supuestos propios.

## Cómo abrir y ejecutar

1. Descomprimir el ZIP en una carpeta limpia.
2. Android Studio → **File > Open…** → seleccionar la carpeta que contiene `settings.gradle.kts`.
3. Esperar el Gradle Sync (la primera vez necesita internet).
4. Ejecutar ▶ **Run 'app'**.
5. En el login, usar **"Modo desarrollo"** para entrar con cualquiera de los cuatro perfiles (el login real es tarea del equipo).

Si Android Studio sugiere actualizar Gradle o el Android Gradle Plugin, **no aceptar**: las versiones están fijadas para que todos los equipos usen exactamente las mismas.

Usuarios de prueba (en `data/MockData.kt`, para cuando implementen el login):

| Perfil | Usuario | Clave |
|---|---|---|
| Docente (Matemática 3°A y 4°A) | docente.mat@libromp.cl | docente123 |
| Docente (Lenguaje 4°A) | docente.len@libromp.cl | docente123 |
| UTP | utp@libromp.cl | utp123 |
| Inspectoría | inspectoria@libromp.cl | inspectoria123 |
| Dirección | direccion@libromp.cl | direccion123 |

## Stack (exigido por el caso)

Kotlin 2.0 · Jetpack Compose · Material 3 · MVVM (ViewModel + StateFlow) · Navigation Compose · **Room obligatorio** (con KSP) y DataStore · Retrofit + Gson. Todas las dependencias están configuradas en Gradle.

El caso tiene además estas restricciones:

- **Recursos del teléfono:** no permite usar GPS, cámara, grabadora de voz ni biometría.
- **Acceso:** no existe ningún acceso para estudiantes ni apoderados.
- **Envíos:** la app no envía notificaciones, correos, SMS ni WhatsApp.

## Flujo de pantallas

```
LOGIN ──(según perfil)──┬─> DOCENTE: Mis cursos ──> Curso ──┬─> Asistencia del día
                        │                                   ├─> Registro de clase (OA y contenidos)
                        │                                   ├─> Calificaciones
                        │                                   └─> Hoja de vida
                        ├─> UTP: Panel ──> Cobertura curricular | Revisión de calificaciones
                        ├─> INSPECTORÍA: Panel ──> Asistencia (cualquier curso) | Alertas
                        └─> DIRECCIÓN: Panel ──> Auditoría
Cerrar sesión (cualquier perfil) ──> LOGIN
```

El botón atrás nativo de Android funciona solo (lo maneja el NavHost). La flecha "←" de la barra superior hace lo mismo. Al ingresar, el login sale de la pila; "Cerrar sesión" la limpia completa.

## Estructura del código (`app/src/main/java/com/librompaz/app/`)

| Carpeta | Contenido |
|---|---|
| `model/` | `Curso`, `Asignatura`, `Estudiante`, `CargaDocente`, `RegistroAsistencia`, `ObjetivoAprendizaje`, `SesionClase`, `Evaluacion`, `Calificacion`, `Anotacion`, `RegistroAuditoria`, `ResumenCurso`, `Usuario`, `Perfil` y sus enums. Corresponden a las entidades del caso. |
| `data/` | `MockData.kt`: datos 100 % ficticios. |
| `data/local/` | `PersistenciaPendiente.kt`: guía de qué entidades, DAOs y base de datos crear con Room. |
| `data/remote/` | `ApiPendiente.kt`: guía para el consumo de la API REST con Retrofit. |
| `viewmodel/` | `LibroMpazViewModel`: un solo ViewModel compartido. Todas las funciones pendientes están marcadas con `TODO(equipo)`. |
| `navigation/` | `AppNavigation`: rutas, NavHost, ingreso por perfil y cierre de sesión. |
| `ui/components/` | `BarraSuperior` (flecha atrás y cerrar sesión) y `SelectorDesplegable` (lista desplegable reutilizable). |
| `ui/screen/` | `LoginScreen` y una carpeta por perfil: `docente/`, `utp/`, `inspectoria/`, `direccion/`, más `comun/` con la asistencia, compartida por Docente e Inspectoría. |
| `util/` | `Fechas.kt`: fecha de hoy para los formularios. |
| `ui/theme/` y `res/` | Tema, colores, logo e ícono **provisionales**. |

## Trazabilidad: requerimiento → pantalla → función a implementar

| Módulo | Requerimiento | Pantalla (ya construida) | Función en el ViewModel (TODO del equipo) |
|---|---|---|---|
| Autenticación y roles | Autenticar al usuario | `LoginScreen` | `login()` |
| Autenticación y roles | El docente solo ve sus cursos y asignaturas (RBAC) | `MisCursosScreen` | `misCargas()` |
| Autenticación y roles | Capacidades distintas por rol | `AppNavigation` (cada perfil tiene su propio flujo) | `login()` |
| Asistencia | Registrar la asistencia diaria (P / A / T / J) | `AsistenciaScreen` | `asistenciaGuardada()`, `guardarAsistencia()` |
| Asistencia | Registro en menos de un minuto | `AsistenciaScreen` (todos parten "presente") | — |
| Asistencia | Inspectoría consulta y gestiona la asistencia general | `PanelInspectoriaScreen`, `AsistenciaScreen` | `porcentajeAsistencia()`, `inasistenciasReiteradas()` |
| Cobertura curricular | Asociar OA y contenidos a cada sesión | `RegistroClaseScreen` | `guardarSesion()` |
| Cobertura curricular | UTP ve la cobertura por curso | `CoberturaCurricularScreen` | `oasTrabajados()` |
| Calificaciones | Registrar notas con instrumento y fecha | `CalificacionesScreen` | `crearEvaluacion()`, `validarNota()`, `guardarCalificaciones()` |
| Calificaciones | UTP detecta desfases u omisiones | `RevisionCalificacionesScreen` | `desfasesCalificaciones()` |
| Hoja de vida | Anotaciones con marca temporal y firma del responsable | `HojaDeVidaScreen` | `registrarAnotacion()` |
| Hoja de vida | Inspectoría identifica riesgo e intervenciones | `AlertasInspectoriaScreen` | `anotacionesQueRequierenIntervencion()`, `inasistenciasReiteradas()` |
| Panel administrativo | Dirección ve el estado global por curso | `PanelDireccionScreen` | `resumenPorCurso()` |
| Auditoría | Registro automático de usuario, fecha y hora de cada cambio | `AuditoriaScreen` | En cada función que guarda |
| Persistencia | Room (obligatorio) | — | ver `data/local/PersistenciaPendiente.kt` |

**Reglas de negocio que el equipo debe hacer cumplir:**

- Ningún estudiante ni apoderado tiene acceso de ningún tipo a la aplicación.
- Toda creación o modificación queda auditada (usuario, fecha y hora) de forma automática e inalterable.
- Se pide confirmación antes de cualquier acción que sobrescriba o borre un registro. Las pantallas de asistencia, notas y hoja de vida ya muestran el diálogo.

Para ver todo lo pendiente: **Edit > Find > Find in Files** (Ctrl+Shift+F / Cmd+Shift+F) y buscar `TODO(equipo)`.

## Qué pasa hoy al ejecutar la app (es intencional)

- **Ingresar:** el login siempre rechaza. Hay que entrar con "Modo desarrollo".
- **Mis cursos:** el docente ve también los cursos de otros docentes.
- **Guardar:** asistencia, clases, evaluaciones, notas y anotaciones muestran la confirmación, pero no guardan nada.
- **Vistas de UTP, Inspectoría y Dirección:** las métricas aparecen como "—", la cobertura aparece toda pendiente y las alertas aparecen vacías.
- **Auditoría:** solo muestra los registros de ejemplo.

## Orden sugerido de trabajo

1. **Login y RBAC:** validar campos vacíos y credenciales, y que cada docente vea solo sus cargas.
2. **Asistencia:** guardar y cargar la asistencia del día, con auditoría.
3. **Registro de clase:** guardar los OA y contenidos de cada sesión.
4. **Calificaciones:** crear evaluaciones y validar y guardar notas.
5. **Hoja de vida:** registrar anotaciones con firma.
6. **Vistas de supervisión:**
   - UTP: cobertura y desfases.
   - Inspectoría: porcentaje de asistencia y alertas.
   - Dirección: métricas por curso.
7. **Persistencia:** reemplazar `MockData` por Room (obligatorio), con auditoría en la misma transacción.
8. **Cierre:**
   - Quitar el "Modo desarrollo".
   - Revisar que no quede ningún `TODO(equipo)`.
   - Reemplazar el logo y los colores provisionales.

## Reglas del proyecto

- **Datos:** solo ficticios y sintéticos, sin nombres, RUT, antecedentes reales ni diagnósticos PIE (Ley 19.628 y Ley 21.719).
- **Repositorio:**
  - Repositorio **PRIVADO** en GitHub y tablero Trello.
  - Un integrante sube esta base como commit inicial.
  - Después, una rama por funcionalidad (por ejemplo `feature/asistencia`).
  - Debe haber commits de **ambos** integrantes.
- **Entrega:** APK firmado en modo release.
- **Consultas sobre el caso:** van por el canal Estudiante → Docente → CITT → organización.

## Logo e ícono

Son **provisionales** (un libro de clases con un check). Para usar el logo oficial:

- **Logo:** copiar el PNG a `res/drawable/logo_app.png` y borrar `logo_app.xml`.
- **Ícono de la app:** clic derecho en `res` > **New > Image Asset**.
