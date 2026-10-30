# Libro MPAZ — Libro Digital de Clases Interactivo

**Proyecto Android | DSY1105-005 | Duoc UC, sede Maipú**

## Descripción del proyecto

Libro MPAZ (Mpazbook) es una aplicación Android desarrollada para el caso de Vinculación con el Medio de la Escuela Escritora Marcela Paz.

Su objetivo es apoyar la gestión académica mediante un libro de clases digital, con funcionalidades diferenciadas según los perfiles de usuario: Docente, UTP, Inspectoría y Dirección.

El proyecto utiliza una plantilla académica común, sobre la cual el equipo ha desarrollado funcionalidades de negocio, validaciones, persistencia local e integración entre las distintas capas de la aplicación.

## Tecnologías utilizadas

- **Lenguaje:** Kotlin 2.0.
- **Interfaz:** Jetpack Compose y Material 3.
- **Arquitectura:** MVVM.
- **Gestión de estado:** ViewModel y StateFlow.
- **Navegación:** Navigation Compose.
- **Persistencia local:** Room con KSP.
- **Otras tecnologías configuradas:** DataStore, Retrofit y Gson.
- **Control de versiones:** Git y GitHub.
- **Entorno de desarrollo:** Android Studio.
- **Compilación:** Gradle.

## Arquitectura

La aplicación utiliza el patrón MVVM, separando las responsabilidades de presentación, lógica y acceso a datos.

**Flujo del módulo de asistencia:**

`AsistenciaScreen → LibroMpazViewModel → AsistenciaRepository → Room`

- **View:** presenta los estudiantes, estados de asistencia y mensajes al usuario.
- **ViewModel:** administra el estado mediante StateFlow, valida las operaciones y coordina las solicitudes.
- **Repository:** centraliza las operaciones de lectura y escritura de asistencia.
- **Room:** almacena los registros de asistencia y auditoría de forma persistente.

## Funcionalidad implementada: RF-03 — Registro de asistencia

El módulo permite al docente registrar la asistencia diaria de los estudiantes de sus cursos.

### Estados disponibles

| Estado | Abreviatura |
|---|---|
| Presente | P |
| Ausente | A |
| Atrasado | T |
| Justificado | J |

### Características

- Selección de curso y fecha.
- Calendario nativo para seleccionar la fecha.
- Registro individual de asistencia.
- Contadores de estudiantes según su estado.
- Confirmación antes de guardar.
- Validación de fechas.
- Validación de permisos del docente.
- Validación de estudiantes pertenecientes al curso.
- Mensajes de éxito y error.
- Bloqueo de controles durante las operaciones.
- Recuperación de registros previamente guardados.
- Modificación de asistencia existente.
- Persistencia mediante Room.
- Registro de auditoría asociado a las operaciones de guardado.

### Persistencia y auditoría

El módulo utiliza Room para almacenar la asistencia localmente.

Las operaciones de guardado se realizan mediante el Repository y contemplan el registro de auditoría dentro de una transacción de base de datos.

La auditoría permite conservar información sobre el usuario responsable, la acción realizada y la fecha y hora correspondiente.

Además, la base de datos incorpora registros iniciales ficticios para facilitar la demostración y las pruebas.

## Instalación y ejecución

### Requisitos

- Android Studio.
- JDK compatible con el proyecto.
- Android SDK configurado.
- Conexión a internet para descargar las dependencias iniciales.
- Emulador Android o dispositivo físico compatible.

### Pasos

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/BenjaminJara012/DSY1105-RF03.git
   ```

2. Abrir Android Studio.
3. Seleccionar **File → Open**.
4. Abrir la carpeta del repositorio que contiene `settings.gradle.kts`.
5. Esperar a que termine la sincronización de Gradle.
6. Seleccionar un emulador o dispositivo Android.
7. Ejecutar la configuración `app`.

**Importante:** no actualizar automáticamente Gradle ni Android Gradle Plugin, ya que el proyecto utiliza versiones definidas en la plantilla académica.

### Compilación y pruebas

Desde la terminal, en la raíz del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
```

Ejecutar las pruebas unitarias:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Ambos comandos se ejecutaron correctamente durante la integración del RF-03.

## Pruebas realizadas

### Pruebas automatizadas

- Compilación de la aplicación: **BUILD SUCCESSFUL**.
- Pruebas unitarias: **BUILD SUCCESSFUL**.
- Pruebas de persistencia y transacciones Room realizadas durante el desarrollo del Repository.

### Pruebas funcionales

El módulo de asistencia fue probado en un dispositivo físico Samsung.

Se verificó:

- Registro de asistencia con los cuatro estados.
- Guardado de los registros.
- Recuperación de asistencia al cambiar de fecha.
- Persistencia de los datos al cerrar y abrir la aplicación.
- Modificación de asistencia previamente registrada.
- Visualización de mensajes y comportamiento de los controles.

## Estructura principal del proyecto

```text
app/src/main/java/com/librompaz/app/
├── data/
│   ├── MockData.kt
│   ├── AsistenciaRepository.kt
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── AsistenciaDao.kt
│   │   ├── AsistenciaEntity.kt
│   │   ├── AuditoriaDao.kt
│   │   └── AuditoriaEntity.kt
│   └── remote/
├── model/
├── navigation/
│   └── AppNavigation.kt
├── ui/
│   ├── components/
│   ├── screen/
│   │   ├── comun/
│   │   │   └── AsistenciaScreen.kt
│   │   ├── docente/
│   │   ├── utp/
│   │   ├── inspectoria/
│   │   └── direccion/
│   └── theme/
├── util/
│   └── Fechas.kt
├── viewmodel/
│   └── LibroMpazViewModel.kt
└── MainActivity.kt
```

## Perfiles y navegación

El proyecto contempla cuatro perfiles:

| Perfil | Funcionalidades previstas |
|---|---|
| Docente | Cursos, asistencia, registro de clases, calificaciones y hoja de vida. |
| UTP | Cobertura curricular y revisión de calificaciones. |
| Inspectoría | Consulta de asistencia y alertas. |
| Dirección | Panel administrativo y auditoría. |

La disponibilidad definitiva de cada funcionalidad depende del alcance implementado por el equipo.

## Reglas de negocio y restricciones

- Los estudiantes y apoderados no tienen acceso a la aplicación.
- Las operaciones de creación y modificación deben registrar auditoría.
- Las acciones que sobrescriben registros requieren confirmación.
- No se utilizan GPS, cámara, grabadora de voz ni biometría.
- La aplicación no envía notificaciones, correos, SMS ni mensajes de WhatsApp.
- Solo se utilizan datos ficticios o sintéticos, respetando las restricciones de privacidad establecidas en el caso.

## Organización del equipo

El trabajo se distribuyó en ramas de Git según las responsabilidades de cada integrante.

| Integrante | Responsabilidad |
|---|---|
| Integrante 1 | Autenticación, navegación y permisos. |
| Integrante 2 | Persistencia local con Room, Repository y datos iniciales. |
| Integrante 3 | Lógica RF-03, ViewModel, StateFlow y validaciones. |
| Integrante 4 | Interfaz RF-03 con Jetpack Compose e integración visual. |

Los cambios se integran mediante Pull Requests, revisión de código y pruebas de compilación.

## Documentación del caso

La carpeta `docs/` contiene:

- `Requerimientos_DSY1105-005.pdf`: requerimientos funcionales, reglas de negocio, requisitos no funcionales y supuestos.
- `Consultas_Pendientes_DSY1105-005.pdf`: consultas pendientes de resolución por el canal oficial.

Las consultas abiertas no deben resolverse mediante supuestos propios.

## Estado del proyecto

**RF-03 — Registro de asistencia:** implementado, integrado en `main` y probado.

**Autenticación y permisos:** pendiente de confirmación de integración final.

Las demás funcionalidades deben verificarse individualmente antes de declararlas terminadas.

## Entrega

La entrega debe cumplir los requisitos establecidos por el docente, incluido el APK firmado en modo release cuando corresponda.

El repositorio contiene el código fuente y el historial de trabajo colaborativo del equipo.