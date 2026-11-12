# Diseño implementado: del Figma a Jetpack Compose

Cómo se llevó al código el diseño del Examen 2 hecho en Figma: [IDNP-2 en Figma](https://www.figma.com/design/6NYZzFku0Sk34sPGks41Ik/IDNP-2?node-id=0-1&t=KaXpnx9oZRcCZbzC-1) (sección «4 · Pantallas finales»).

## 1. Tokens tomados de Figma

### Colores (colección `AlertaUNI` de Figma)

| Token | Hex | Uso en la app |
|---|---|---|
| `color/primary` | `#1E3A8A` | Cabecera, botones y FAB (`MyPrimaryColor`) |
| `color/secondary` | `#E0E7FF` | Fondo del muro (`MySecondaryColor`) |
| `color/selected` | `#B8C4F5` | Chip o elemento seleccionado |
| `color/surface` | `#FFFFFF` | Tarjetas |
| `color/background` | `#F8FAFC` | Fondo del formulario |
| `color/foreground` | `#0F172A` | Texto principal |
| `color/muted` / `muted-foreground` | `#F1F5F9` / `#64748B` | Chip de curso y metadatos |
| `color/border` | `#E2E8F0` | Bordes y divisores |
| `color/public-bg` / `public-fg` | `#DBEAFE` / `#1E40AF` | Chip «Público» |
| `color/private-bg` / `private-fg` | `#F3E8FF` / `#6B21A8` | Chip «Privado» y «Para: …» |
| `color/prio-high` / `-bg` / `-fg` | `#DC2626` / `#FEE2E2` / `#B91C1C` | Prioridad Alta |
| `color/prio-med` / `-bg` / `-fg` | `#D97706` / `#FEF3C7` / `#B45309` | Prioridad Media |
| `color/prio-low` / `-bg` / `-fg` | `#16A34A` / `#DCFCE7` / `#15803D` | Prioridad Baja |

### Tipografía

| Estilo | Fuente | Tamaño / interlineado | Uso |
|---|---|---|---|
| `AU/Headline` | PT Serif Bold | 22 / 28 | Título «Anuncios» |
| `AU/Title` | PT Serif Bold | 17 / 22 | Título de la tarjeta |
| `AU/Body` | PT Serif Regular | 14 / 20 | Resumen del anuncio |
| `AU/Input` | Roboto Regular | 15 / 20 | Campos de texto |
| `AU/LabelLarge` | Roboto Medium | 14 / 20 | Botones |
| `AU/Label` | Roboto Medium | 12 / 16 | Chips |
| `AU/Caption` | Roboto Regular | 12 / 16 | Fecha y comentarios |
| `AU/Small` | Roboto Medium | 11 / 14 | Nombre del docente en la cabecera |

La app ya usaba PT Serif como fuente descargable; Roboto es la fuente por defecto de Android.

### Medidas principales
- Pantalla de 360 × 800 dp con márgenes laterales de 16 dp.
- Buscador de 48 dp en la cabecera; chips de filtro de 32 dp separados 8 dp.
- Tarjeta con borde de 1 dp y **franja de prioridad de 5 dp** a la izquierda; resumen limitado a 2 líneas.
- 10 dp entre tarjetas y **88 dp de margen inferior** en la lista, para que el FAB extendido nunca tape la última tarjeta.

## 2. Pantallas de Figma y su implementación

| Figma | Composable (`screen/post/…`) | Estado / ViewModel |
|---|---|---|
| Muro rediseñado | `PostScreen` → `MuroHeader` (con `CampoBusqueda` y `BotonBorradores`) + `FiltrosChips` + `LazyColumn` con `stickyHeader` por fecha | `PostViewModel`, `MuroUiState.Contenido` |
| Tarjeta de anuncio | `AnuncioCard` con `CursoChip`, `TipoChip` y `PrioridadChip` | Prioridad, archivado y destinatario desde la capa local `post_extra` |
| Búsqueda activa con resaltado | `CampoBusqueda` + `resaltar()` (`AnnotatedString`) + línea «N resultados…» | `FiltroMuro.texto` con `debounce(250)` |
| Sin resultados / muro vacío | `EstadoMuro` («Quitar filtros», «Buscar también en archivados») | `MuroUiState.SinResultados` / `MuroVacio` |
| Archivados | `AnuncioCard` atenuada + «Restaurar al muro» | `FiltroMuro.archivados` |
| Menú de acciones | `MenuAnuncio` (`DropdownMenu`) | `archivar`, `restaurar` y `alternarComentarios` |
| Archivado con «Deshacer» | `Snackbar` con acción | Actualización optimista + `MuroEvento.Archivado` |
| Formulario público / privado | `NuevoAnuncioScreen`: `SelectorTipo` (`SingleChoiceSegmentedButtonRow`), `SelectorCurso` (`ExposedDropdownMenuBox`), `CampoEstudiante`, `SelectorPrioridad`, campos con contador y `BarraEnvio` | `NuevoAnuncioViewModel`, `FormUiState` |
| Selector de estudiante | `SelectorEstudianteSheet` (`ModalBottomSheet` con búsqueda y `RadioButton`) | Estudiantes del curso elegido |
| Confirmación de privado | `ConfirmarPrivadoDialog` (`AlertDialog`) | Solo para anuncios privados |
| Errores de validación | `OutlinedTextField` con `isError` y `supportingText`; `BarraEnvio` en rojo | Se muestran tras el primer intento de publicar |
| Publicado | `Snackbar` + tarjeta nueva resaltada | `FormEvento.Publicado` → `onPublicado` |
| Comentarios cerrados | `PostCommentScreen` con `AvisoComentariosCerrados` y `EntradaComentario` bloqueada | `PostCommentViewModel` |
| (no estaba en Figma) Borradores | `BorradoresScreen` + `BorradorCard`; «Guardar borrador» y diálogo de salida en el formulario | `BorradoresViewModel`, `DraftRepository` (Room) |

## 3. Comparación final con Figma

Comprobada con capturas de la app en el emulador frente a las maquetas exportadas.

**Igual que en Figma:**
- La cabecera azul con el buscador y la fila de chips.
- La tarjeta: franja, chips, menú ⋮, título, resumen de 2 líneas, y pie con hora y comentarios.
- Los grupos por fecha y el FAB extendido.
- El formulario completo, la hoja de estudiantes y el diálogo de confirmación.
- Los estados vacío y sin resultados, los archivados y los comentarios cerrados.

**Diferencias, con su razón:**

| Diferencia | Razón |
|---|---|
| Botón «Borradores» con texto y contador en la cabecera | Requisito nuevo de la Parte 2; con texto es más fácil de descubrir que un ícono |
| «Guardar borrador» en la barra del formulario, cuyo título es más pequeño | La acción no estaba en Figma y el texto debía caber en 360 dp |
| Diálogo «¿Guardar como borrador?» al salir y pantalla de Borradores | No existían en Figma; siguen el mismo sistema visual (borde discontinuo y etiqueta BORRADOR) |
| La barra de Comentarios no tiene el menú ⋮ | Habilitar o deshabilitar ya está en el aviso y en el menú de la tarjeta |
| El aviso de «publicado» no tiene la acción «VER» | El anuncio nuevo ya aparece arriba y resaltado |
| En horizontal: navegación en un riel lateral y cabecera del muro que se oculta al desplazar | Figma solo diseñó la versión vertical; con poca altura no se veía ningún anuncio (prueba de tamaños PT-01) |
| El nombre del docente y los datos son los del modo demo | No hubo acceso al backend real (ver «Real vs. simulado» en el README) |
| Prioridad, archivado, comentarios cerrados y destinatario se guardan en el teléfono | El backend no tiene esas columnas (SIMULADO, tabla `post_extra`) |
