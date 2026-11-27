\# Testing y cobertura — Libro MPAZ



\## 1. Objetivo



Validar el funcionamiento del requerimiento RF-03 (Registro de

asistencia), verificando las reglas de negocio, los permisos de

usuario, la recuperación de registros y el manejo de errores

desde el ViewModel.



Las pruebas buscan garantizar que las operaciones válidas sean

procesadas correctamente y que las operaciones inválidas sean

rechazadas antes de afectar la persistencia de los datos.



\## 2. Tecnologías utilizadas



\- JUnit 4: ejecución de pruebas unitarias.

\- MockK: simulación de dependencias de Android.

\- Kotlin Coroutines Test: control de operaciones asíncronas.

\- StateFlow: verificación de los estados del ViewModel.

\- FuenteFalsa: simulación de la fuente de datos.

\- JaCoCo: generación de reportes de cobertura.

\- Gradle: automatización de la ejecución de pruebas.



\## 3. Estrategia de testing



Se realizaron pruebas unitarias sobre LibroMpazViewModel,

utilizando una fuente de datos falsa para aislar la lógica

de negocio de las dependencias externas.



Esta estrategia permite comprobar el comportamiento del

ViewModel sin depender de una base de datos Room real

ni de un servidor activo.



Se consideran escenarios exitosos, validaciones incorrectas,

errores del repositorio, pérdida de conexión, expiración

de sesión y control de operaciones simultáneas.



\## 4. Casos de prueba del RF-03



Archivo: AsistenciaViewModelTest.kt



| ID | Caso de prueba | Resultado esperado | Estado |

|---|---|---|---|

| CP-01 | Guardar asistencia válida | Guardado exitoso y aviso de confirmación | Aprobado |

| CP-02 | Guardar sin sesión activa | Rechazar la operación | Aprobado |

| CP-03 | Guardar con fecha futura | Rechazar la fecha | Aprobado |

| CP-04 | Guardar lista vacía | Impedir el registro vacío | Aprobado |

| CP-05 | Guardar curso no autorizado | Rechazar por falta de permisos | Aprobado |

| CP-06 | Guardar estudiante ajeno al curso | Rechazar los datos incorrectos | Aprobado |

| CP-07 | Guardar fecha inexistente | Rechazar la fecha inválida | Aprobado |

| CP-08 | Error del repositorio | Informar el error al usuario | Aprobado |

| CP-09 | Guardar sin conexión | Actualizar el estado de sincronización | Aprobado |

| CP-10 | Sesión expirada | Reiniciar la sesión local | Aprobado |

| CP-11 | Recuperar asistencia guardada | Devolver los estados correspondientes | Aprobado |

| CP-12 | Excepción durante el guardado | Informar el error y liberar el estado de guardado | Aprobado |

| CP-13 | Dos solicitudes de guardado | Ejecutar una sola operación | Aprobado |



\## 5. Resultados de ejecución



Fecha de ejecución: 27/11/2026.



\### Pruebas específicas del RF-03



| Indicador | Resultado |

|---|---:|

| Total de pruebas | 13 |

| Pruebas aprobadas | 13 |

| Fallos | 0 |

| Errores | 0 |

| Pruebas omitidas | 0 |

| Tasa de aprobación | 100% |



\### Pruebas unitarias globales



| Indicador | Resultado |

|---|---:|

| Total de pruebas identificadas | 168 |

| Pruebas aprobadas | 117 |

| Fallos | 0 |

| Errores | 0 |

| Pruebas omitidas | 51 |



Las 51 pruebas omitidas corresponden a clases relacionadas

con la integración remota que mantienen la anotación

@Ignore("E3: completar").



| Clase | Pruebas omitidas |

|---|---:|

| ApiServiceMockWebServerTest | 15 |

| ConfigServidorTest | 6 |

| ErroresApiTest | 11 |

| MapeadoresTest | 7 |

| LibroMpazApiRepositoryTest | 12 |

| Total | 51 |



Estos resultados representan el estado del proyecto al

momento de realizar las pruebas y deberán actualizarse

después de completar la integración remota.



\## 6. Cobertura de código



Se utilizó JaCoCo para medir la cobertura de instrucciones

y ramas alcanzada mediante las pruebas unitarias.



\### Cobertura por componente



| Componente | Instrucciones | Ramas |

|---|---:|---:|

| Proyecto completo | 11% | 12% |

| Paquete domain | 96% | 86% |

| Paquete viewmodel | 43% | 21% |

| Clase LibroMpazViewModel | 37% | 15% |

| ValidacionesKt | 98% | 95% |

| PoliticaAcceso | 100% | 96% |

| RegistrosAsistenciaKt | 100% | 100% |



\### Cobertura de métodos asociados al RF-03



| Método | Instrucciones | Ramas |

|---|---:|---:|

| guardarAsistencia | 94% | 90% |

| asistenciaGuardada | 93% | 50% |

| validarFechaAsistencia | 100% | No aplica |

| persistir | 100% | 100% |

| expirarSesion | 100% | No aplica |



La cobertura global incluye módulos de interfaz gráfica,

persistencia, navegación y funcionalidades ajenas al RF-03.



Por esta razón, se presenta también la cobertura específica

de los métodos relacionados con el registro de asistencia.



La cobertura no implica ausencia total de defectos ni

reemplaza las pruebas de integración o de interfaz.



\## 7. Reproducción de las pruebas



Ejecutar desde la raíz del repositorio.



\### Pruebas específicas de asistencia



```powershell

.\\\\gradlew.bat :app:testDebugUnitTest --tests "com.librompaz.app.viewmodel.AsistenciaViewModelTest"

```



\### Todas las pruebas unitarias



```powershell

.\\\\gradlew.bat :app:testDebugUnitTest

```



\### Generar reporte de cobertura



```powershell

.\\\\gradlew.bat :app:createDebugUnitTestCoverageReport

```



\### Abrir reporte de pruebas



```powershell

Invoke-Item app/build/reports/tests/testDebugUnitTest/index.html

```



\### Abrir reporte de cobertura



```powershell

Invoke-Item app/build/reports/coverage/test/debug/index.html

```



\## 8. Evidencias



\- \[Pruebas específicas de asistencia](evidencias/01-pruebas-asistencia.png)

\- \[Resultados globales](evidencias/02-pruebas-globales.png)

\- \[Cobertura global](evidencias/03-cobertura-global.png)

\- \[Cobertura del dominio](evidencias/04-cobertura-dominio.png)

\- \[Cobertura del ViewModel](evidencias/05-cobertura-viewmodel.png)

\- \[Cobertura de métodos del RF-03](evidencias/06-cobertura-metodos-rf03.png)



\## 9. Limitaciones y trabajo pendiente



\- Completar y ejecutar las pruebas de integración remota.

\- Validar el funcionamiento con el servidor real.

\- Actualizar los resultados globales después de la integración.

\- Realizar las verificaciones finales del APK firmado.



\## 10. Conclusión



Las 13 pruebas unitarias específicas del RF-03 fueron

ejecutadas satisfactoriamente, sin fallos ni errores.



Los resultados permiten respaldar el comportamiento del

ViewModel frente a operaciones válidas, datos incorrectos,

restricciones de acceso y errores de persistencia.



La cobertura obtenida en los métodos principales del RF-03

evidencia que las pruebas ejercitan gran parte de su lógica.



La validación integral del sistema deberá completarse una

vez finalizada la integración con el servidor.

