# TicketTI

App móvil Android (Kotlin + Jetpack Compose) para **registrar y dar seguimiento a solicitudes de clientes** de una pequeña empresa de servicios informáticos y digitales.

- **Asignatura:** DSY1105 · Desarrollo de Aplicaciones Móviles (Duoc UC, CITT Puente Alto)
- **Entrega:** primera entrega (módulo de Solicitudes). El caso completo se desarrolla en el examen transversal.
- **Grupo:** 10
- **Integrantes:** Diego _[apellido]_ y Abraham _[apellido]_
- **Base del proyecto:** ejemplo del profesor (login + navegación), adaptado al caso. No se trabaja sobre el repo del profe.

---

## 1. El caso en breve

La empresa recibe hoy sus solicitudes por WhatsApp (texto, audios, imágenes, documentos) y pierde trazabilidad: no hay identificador único, estado, prioridad ni responsable. La app busca registrar, clasificar, asignar, actualizar y consultar cada solicitud durante todo su ciclo.

Perfiles del caso completo: **cliente**, **administrador/coordinador** y **técnico**.

## 2. Alcance de ESTA entrega

Solo el módulo de **Solicitudes**. Lo demás (asignación, historial, WhatsApp, resumen, reapertura, etc.) queda para el examen transversal.

| Pieza | Qué debe hacer |
|---|---|
| `ListaSolicitudesScreen` | Lista las solicitudes guardadas en Room: código, título, categoría y estado. Botón para agregar y abrir una solicitud para ver/editar. |
| `SolicitudFormScreen` | Crea y edita una solicitud: título, descripción, categoría, servicio relacionado y **foto de evidencia con la cámara**. |
| Login con roles | Usuarios ficticios: `admin`, `cliente`, `tecnico`. |
| Menú | Mis solicitudes · Nueva solicitud · Cerrar sesión. |

**Flujo de navegación:** `MainActivity` → `AppNav` → Login → Menú → ListaSolicitudesScreen → SolicitudFormScreen.

### Validaciones mínimas
- Título, descripción y categoría: obligatorios.
- Servicio relacionado: obligatorio cuando corresponda.
- Identificador único generado automáticamente (ej. `TI-0001`).
- Una solicitud nueva inicia en estado **"Recibida"**.
- Registrar fecha y hora de creación.
- Errores visibles en los campos, **controlados desde el ViewModel**:
  ```kotlin
  isError = state.tituloError != null
  supportingText = { Text(state.tituloError ?: "") }
  ```

### Recurso nativo: cámara
- **Decisión:** captura de fotografía como **evidencia ficticia**, vinculada a la solicitud (`evidenciaUri`).
- Implementación: cámara del sistema (`ActivityResultContracts.TakePicture` + `FileProvider`). _(Pendiente de confirmar vs. CameraX.)_
- Opcional: botón "Elegir de galería" para pantallazos.
- Evidencias de ejemplo (todas **ficticias**): boceto en papel de un cambio web, foto de un equipo con falla, factura de hosting inventada.
- **Prohibido** usar datos, imágenes, conversaciones o códigos reales de clientes.

## 3. Arquitectura (MVVM real)

```
app/src/main/java/com/example/ticketti/
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── SolicitudEntity.kt
│   │   ├── SolicitudDao.kt
│   │   └── AppDatabase.kt
│   └── repository/
│       ├── SolicitudRepository.kt
│       └── AuthRepository.kt
├── model/
│   └── Credential.kt            (usuario + rol)
├── ui/
│   ├── login/                   (LoginScreen, LoginViewModel, LoginUiState)
│   ├── menu/                    (DrawerMenu)
│   └── solicitud/
│       ├── ListaSolicitudesScreen.kt
│       ├── SolicitudFormScreen.kt
│       ├── SolicitudViewModel.kt
│       └── SolicitudUiState.kt
└── navigation/
    └── AppNav.kt
```

> Esta es la estructura **objetivo**. Al partir, el login está en `ui/theme/login` y el menú en `view/`; se reubican solo si el equipo lo decide, para no generar conflictos.

Reglas de la arquitectura:
- La **UI** no accede al DAO. Solo observa el `StateFlow` del ViewModel.
- El **ViewModel** no conoce Room. Habla con el Repository.
- El **Repository** es el único que usa el DAO.
- **Room desde el inicio**: nada de listas mutables en memoria. Los datos deben sobrevivir al reiniciar la app.
- La lista es **reactiva**: el DAO devuelve `Flow<List<SolicitudEntity>>`.

### UiState (definido por el profesor)
```kotlin
data class SolicitudUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val evidenciaUri: String? = null,
    val estado: String = "Recibida",
    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
)
```

## 4. Versiones y dependencias

Se **mantienen las versiones de la base del login** (ya compilan en nuestros equipos). Solo se agrega lo necesario para Room.

| Elemento | Versión |
|---|---|
| AGP | 8.12.3 |
| Kotlin | 2.0.21 |
| KSP | `2.0.21-1.0.28` |
| Room | 2.6.1 (probar 2.8.4 solo si hay problemas) |
| Navigation Compose | 2.7.7 |
| Compose BOM | 2024.09.00 |

**Ojo:** los ejemplos del profe usan otras versiones (AGP 9.x). La propiedad `android.disallowKotlinSourceSets=false` y la ausencia del plugin `kotlin-android` son ajustes de AGP 9; **no copiarlos** con AGP 8.12.3.

## 5. Reparto del trabajo

Se divide por capas para que nunca editen los mismos archivos y los dos tengan commits visibles.

### Primer commit (base del proyecto, lo hace Diego)
- Proyecto renombrado a TicketTI (nombre, package, tema).
- Login adaptado al caso y usuarios ficticios con rol (`admin`, `cliente`, `tecnico`).
- Menú adaptado (Mis solicitudes · Nueva solicitud · Cerrar sesión) y rutas registradas en `AppNav` con pantallas provisorias.
- Este README.

### Integrante A (_[tu nombre]_) — Datos, estado y lógica
- Dependencias Room/KSP en Gradle.
- `SolicitudEntity` (id único, código, título, descripción, categoría, servicio, evidenciaUri, estado, fecha y hora).
- `SolicitudDao` (lista con `Flow`, buscar por id, insertar, actualizar).
- `AppDatabase` (singleton).
- `SolicitudRepository`.
- `SolicitudUiState` y `SolicitudViewModel` (StateFlow, validaciones, generación del código, guardar y editar).

### Integrante B (Abraham) — Interfaz de Solicitudes
- `ListaSolicitudesScreen` (lista reactiva, botón agregar, abrir una solicitud para editar).
- `SolicitudFormScreen` (campos, errores visibles, selector de categoría).
- Reemplazar las pantallas provisorias de `AppNav` por las reales y pasar el id de la solicitud al editar.

### En pareja
- **Cámara:** A deja en el ViewModel el manejo de `evidenciaUri`; B hace el botón, el permiso, el `FileProvider` y la vista previa de la foto.
- **Informe** con al menos 8 hitos: cada uno documenta los hitos de su parte.
- **Pruebas finales** de registro, edición, consulta y recuperación tras reiniciar la app.

### Punto de encuentro (acordar antes de empezar)
Para trabajar en paralelo sin bloquearse, A publica primero (aunque esté a medias) la **firma del ViewModel y del Repository**, y B programa la UI contra esa firma. El nombre de las rutas y del `SolicitudUiState` ya está fijado en este README.

## 6. Flujo de trabajo con Git

- Rama `main`: siempre debe compilar. No se commitea directo.
- Ramas de trabajo: `feature/datos-room` (A) y `feature/ui-navegacion` (B).
- Commits pequeños y frecuentes, con mensaje claro (`feat: agrega SolicitudDao con Flow`).
- Se integra a `main` con Pull Request, revisado por el otro integrante.
- El **primer commit** (proyecto renombrado y adaptado) lo hace una sola persona. Desde el segundo trabajan los dos.
- No subir `local.properties`, `.idea/` ni la carpeta `build/`.

## 7. Checklist de la rúbrica

- [ ] Navegación: MainActivity → AppNav, secuencia Login → Menú → Lista → Formulario
- [ ] Lista de solicitudes almacenadas
- [ ] Crear y editar solicitud
- [ ] Formulario bien estructurado
- [ ] Errores visibles, controlados desde el ViewModel
- [ ] UiState + StateFlow
- [ ] MVVM: ViewModel + Repository + DAO + Room
- [ ] La solicitud se guarda realmente (sobrevive al reinicio)
- [ ] La lista se actualiza al registrar o modificar
- [ ] Cámara integrada al flujo, con la foto vinculada a la solicitud
- [ ] Commits de ambos integrantes
- [ ] Paquetes y responsabilidades claras
- [ ] Informe con 8 o más hitos (nombre, descripción, captura, resultado)

## 8. Hitos sugeridos para el informe

1. Estructura del proyecto
2. Arquitectura MVVM
3. Navegación
4. Listado de solicitudes
5. Formulario de solicitudes
6. Validaciones
7. Gestión de estado (UiState y StateFlow)
8. Persistencia local con Room
9. Integración de la cámara
10. Pruebas de funcionamiento

> La guía del profe menciona "productos" en algunos hitos porque es una plantilla reutilizada. En nuestro informe se adaptan a **solicitudes**.

## 9. Fuera de alcance (examen transversal)

Asignación de responsables, prioridades, historial de cambios, compartir por WhatsApp, confirmación y reapertura, notificaciones, resumen general y gestión administrativa completa.

## 10. Notas útiles

- Los ejemplos del profe están en `DSY1105-009V-2026` (base), `GUIA11Formulariosvalidaciones` (formulario + Room) y `camara2026` (lectura de QR con CameraX y ML Kit, disponible como opción extra).
- En el `.txt` del `RegistroScreen` del profe hay una errata: usa `onAceptaTerminosChange`, pero el ViewModel define `onAceptarTerminosChange`. En el repo en GitHub está correcto.
- El ejemplo de Room del profe llama al DAO desde el ViewModel. **Nosotros debemos pasar por Repository**, porque así lo exige la rúbrica.