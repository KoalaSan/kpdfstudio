# Sistema de iconos — PDF Studio 3.6

## 1. Decisión

**Colección seleccionada: Lucide** (`https://lucide.dev`), licencia **ISC**.

Los 51 glifos de interfaz se descargaron como SVG originales desde
`https://raw.githubusercontent.com/lucide-icons/lucide/main/icons/` y se
convirtieron a **Android VectorDrawable XML** con
`tools/build_vectordrawables.py`. Las fuentes originales están en
`docs/icon-system/lucide-svg/` y las licencias en `docs/icon-system/`.

El icono de lanzador es propio (`res/drawable/ic_launcher_foreground.xml`),
dibujado con el mismo lenguaje visual (documento con chaflán + acento azul).

### Por qué Lucide y no otra biblioteca

| Colección | Licencia | Grid / trazo | Resultado |
|---|---|---|---|
| **Lucide** | **ISC** (comercial OK, sin atribución obligatoria) | 24×24, trazo 2, caps redondos | **Elegida**: coincide exactamente con el renderer previo, es la más uniforme y cubre todas las funciones |
| Tabler Icons | MIT | 24×24, trazo 2 | Muy buena alternativa; descartada porque Lucide tiene mejor cobertura de glifos específicos (firma, resaltador, combinación) |
| Phosphor | MIT | 24×24, trazo variable | Descartada: sus seis pesos romperían la uniformidad de trazo |
| Material Symbols | Apache 2.0 | 24×24, variable (FILL/GRAD/wght) | Descartada: estilo Google reconocible, no encaja con la identidad Graphite; requiere fuente descargable |
| Material Icons (clásicos) | Apache 2.0 | 24×24, relleno | Descartada: relleno sólido, contraste distinto al resto de la app |
| Compose Icons | Apache 2.0 | — | No aplicable: el proyecto es framework-only Java/XML, sin Compose ni AndroidX |
| Iconify | Depende del set | Depende del set | Descartada como fuente directa: es un agregador, añade una dependencia de runtime y su licencia depende de cada paquete |
| Iconfinder / Icons8 / Flaticon | Mixtas (muchas requieren atribución o son de pago) | Variables | Descartadas: licencias incompatibles o inciertas para uso comercial sin atribución |
| Recraft / generadores IA | Variable | No vectorial determinista | Descartados: no producen paths editables reproducibles ni coherencia garantizada entre glifos |
| Figma / Illustrator / Inkscape | Herramientas (no fuentes) | — | Inkscape es la herramienta recomendada para editar los SVG descargados; no sustituye a la colección |

**Licencia ISC verificada** en `docs/icon-system/LICENSE-lucide.txt`:
permite uso, copia, modificación y distribución con o sin cargo; solo exige
conservar el aviso de copyright. No requiere atribución visible en la app.

## 2. Inventario y sustituciones

Todos los nombres semánticos usados por la app (`MainActivity`, `DesignSystem`)
se resolvían antes a paths dibujados a mano en `Icons.java`. Ahora apuntan a
recursos VectorDrawable reales.

### Navegación y accesos
| Nombre | Lucide | Uso |
|---|---|---|
| `home` | `house` | Nav Inicio |
| `folder` (`files`) | `folder` | Nav Biblioteca, acceso Abrir |
| `tools` | `wrench` | Nav Herramientas |
| `settings` | `settings-2` | Nav Ajustes |
| `scan` | `scan-line` | Acceso Escanear |
| `pen` (`edit`) | `pen-line` | Acceso Crear, Anotar, chip Bolígrafo |
| `merge` (`link`) | `combine` | Acceso Unir |

### Documentos y acciones
| Nombre | Lucide | Uso |
|---|---|---|
| `doc` | `file-text` | Tarjetas, badges, cabecera Acerca de |
| `star` / `starfill` | `star` / variante rellena | Favoritos y marcador de página |
| `book` | `book-open` | Herramienta |
| `scissors` | `scissors` | Extraer páginas |
| `compress` | `minimize-2` | Comprimir PDF |
| `image` | `image` | Herramienta de imagen |
| `storage` | `hard-drive` | Ajustes / almacenamiento |
| `camera` | `camera` | Escaneo con cámara |
| `palette` | `palette` | Ajustes / apariencia |

### Visor
| Nombre | Lucide | Uso |
|---|---|---|
| `back` (`prev`) | `chevron-left` | Volver, Anterior, resultados de búsqueda |
| `chevron` (`next`) | `chevron-right` | Siguiente |
| `moon` / `sun` | `moon` / `sun` | Modo noche / lectura nocturna |
| `search` | `search` | Buscar, lupa del campo |
| `hash` | `hash` | Ir a página |
| `share` | `share-2` | Compartir |
| `zoomin` / `zoomout` | `zoom-in` / `zoom-out` | Zoom |
| `rotate` | `rotate-cw` | Girar |
| `trash` | `trash` | Borrar página, Limpiar anotación |
| `highlighter` | `highlighter` | Chip Resaltador |
| `signature` | `signature` | Chip Firma |

### Unir y estados
| Nombre | Lucide | Uso |
|---|---|---|
| `select` / `unselect` | `square-check` / `square` | Selección de documentos |
| `up` / `down` | `arrow-up` / `arrow-down` | Reordenar páginas |
| `plus` / `minus` / `close` / `check` | `plus` / `minus` / `x` / `check` | Acciones secundarias |
| `alert` (`warning`) | `triangle-exclamation-point` | Pantalla de error de arranque |
| `info` / `shield` / `bolt` | `info` / `shield` / `zap` | Informativos |
| `more` / `grid` | `ellipsis` / `grid-2x2` | Menú y vista de cuadrícula |

### Recursos eliminados
- `createIconBadge(Context, String, int, int)` en `DesignSystem.java`: aceptaba
  emojis como texto y no tenía llamadas. Eliminado.
- Renderer de Canvas, parser de paths y caché de `Path` en `Icons.java`:
  sustituidos por resources (`getIdentifier` + caché de IDs).
- `☑` del Toast en `MainActivity.java` (antes: «Primero marca este documento
  con ☑»): reemplazado por texto accesible («Primero selecciona este
  documento en la lista»).
- `@android:drawable/sym_def_app_icon`: reemplazado por launcher propio.

## 3. Integración técnica

- `Icons.view()` devuelve un `ImageView` con `setImageResource(id)` y
  `setColorFilter(color, SRC_ATOP)`; el tint sigue el tema (claro/oscuro).
- `Icons.drawable()` devuelve un `Drawable` mutado y teñido para compound
  drawables (lupa del buscador).
- `Icons.setColor()` actualiza el tint en caliente cuando un chip pasa a
  seleccionado; `ToolChip` también cambia fondo, peso de texto y
  `contentDescription` con «, seleccionada».
- Los IDs se resuelven una sola vez con `Resources.getIdentifier()` y se
  cachean en un `HashMap`, sin dependencias de AndroidX ni Compose.
- Tamaños normalizados: `SIZE_SM=18`, `SIZE_MD=22`, `SIZE_LG=24`,
  `SIZE_NAV=21` dp.
- Los iconos son decorativos
  (`IMPORTANT_FOR_ACCESSIBILITY_NO`); el control padre lleva la etiqueta
  TalkBack en español.

## 4. Problemas encontrados y corregidos

1. **Icono de lanzador genérico del sistema** → launcher vectorial propio.
2. **Mezcla de estilos** (glyphs dibujados a mano con grosores dispares) →
   colección única con grid y trazo idénticos.
3. **Símbolo `☑` en un mensaje** (no es un icono, no escala, no se tiñe) →
   texto accesible.
4. **Helper de emoji sin usar** → eliminado.
5. **Duplicidades semánticas** (`files`/`folder`, `edit`/`pen`, `prev`/`back`,
   `zoom_in`/`zoomin`, `warning`/`alert`) → aliases a un único recurso.
6. **Herramientas de anotación sin estado seleccionado** → chips con estado
   activo, color primario, fondo tintado y descripción hablada.
7. **`Resaltador` y `Firma` usaban iconos incorrectos** (`edit`, `select`) →
   glifos específicos de Lucide.
8. **Engranaje de ajustes con dientes que cruzaban el anillo** → reemplazado
   por `settings-2`.
9. **Nombres upstream cambiados en Lucide** (`triangle-alert` →
   `triangle-exclamation-point`, `trash-2` → `trash`, `layout-grid` →
   `grid-2x2`) → resueltos contra el árbol Git real del repositorio.
10. **`getIdentifier` con nombres inexistentes** → fallback a `ic_doc` y log
    de error, para que ningún icono faltante provoque un crash.

## 5. Verificación

- Compilación con el script requerido: correcta.
- Firma SHA-256 del certificado: `C8:07:52:35:E0:A8:7E:81:AA:06:2F:62:72:B6:E1:FE:CA:29:0D:17:2D:D6:2C:C9:A8:8F:C2:3B:4B:F8:AB:3A` (idéntica a la versión instalada previamente).
- 53 vectores dentro del APK (52 genéricos + launcher).
- Verificación en dispositivo mediante `read_screen` (no capturas, por
  petición del usuario): Inicio, Biblioteca, Herramientas, Ajustes, visor,
  búsqueda, Unir y anotación.
