# Registro de progreso — PDF Studio

## 2026-11-22 — Checkpoint inicial
- Estado: APK `build/app.apk`, `com.buddyapps.pdfstudio`, versionCode 9, versionName 3.3, SHA-256 `82015c9f2d86177272b6f95a2e86f737e7e6598fb9b5160214a75453af1f2925`.
- Repositorio Git creado en la carpeta del proyecto; commit `b54f520 checkpoint: PDF Studio v3.3 baseline before improvement batch`. No existía control de versiones previo.
- Auditoría inicial de código: se detectan fugas de bitmaps en el visor, selector de unión con `EXTRA_ALLOW_MULTIPLE` que sólo procesa el primer URI, resaltado de búsqueda desactivado al rotar y textos "v3.1" obsoletos.
- No se han modificado fuentes todavía.

## 2026-11-22 — Lote de mejoras v3.4 (compilado, pendiente de verificación)
- Cambios en `MainActivity.java`: reciclado del bitmap de página anterior y del overlay de resaltado al navegar/salir del visor; no se asigna overlay vacío; importación de todos los URIs del `ClipData` al unir; `resultRect()` transforma coordenadas para 90/180/270°; textos de la guía actualizados a v3.4; versión 10 / 3.4 en `app.properties` y pantalla Acerca de.
- Cambio en `PdfRendererCore.java`: se libera el bitmap base tras rotar y se maneja `OutOfMemoryError`.
- Compilación correcta con `build-apk.sh` (Java, dex, package, sign). Primer APK firmado con una clave nueva generada por error al pasar `HOME` con session `agy-home`; el instalador rechazó la actualización con "firmas no coinciden".
- Recompilado con `HOME=/root` para usar `/root/.buddy/keys/apps.jks` (SHA-256 `c8075235e0a87e81aa062f6272b6e1feca290d172dd62cc9a88fc23b4bf8ab3a`). SHA-256 del APK: `0b1e95926106c5619e8c0174079d1d9afd770c45536fba60d93f2ca79f7fba50`.
- Las herramientas de teléfono quedaron bloqueadas porque el usuario estaba usando la pantalla; no se pudo abrir el instalador ni copiar el APK a Downloads. Queda pendiente instalar y ejecutar T-06.

## 2026-11-22 — Instalación como app nueva
- El instalador siguió rechazando la actualización con "conflicto con un paquete": la firma de la v3.3 instalada (`c844d3e9…`) no corresponde a ninguna clave disponible en este entorno.
- El usuario eligió conservar la v3.3 e instalar la v3.4 como aplicación independiente.
- Se cambió el `package` del manifest a `com.buddyapps.pdfstudio34`, se fully-qualificaron `.MainActivity` y `.PdfStudioFileProvider`, se cambió la autoridad del provider a `com.buddyapps.pdfstudio34.fileprovider` y el label a "PDF Studio 3.4"; las clases Java siguen en `com.buddyapps.pdfstudio` y compilan sin cambios.
- APK final: `com.buddyapps.pdfstudio34`, versionCode 10, versionName 3.4, firmado con `c8075235…`, SHA-256 `441b5b0834ba39e932557ad57576bd74847784634f022d61bd84b341dc714b9d`.
- La instalación sigue pendiente: las herramientas de teléfono informan "SCREEN CHANGED" de forma repetida.

## 2026-11-22 — Instalación completada
- Tras varios reintentos, `phone_install_apk` abrió el instalador y `phone_wait_for_install` confirmó: `Installed com.buddyapps.pdfstudio34 (versionCode 10). The user confirmed the install.`
- `pm path` devuelve `/data/app/~~uWXaE8NQJPwrS3GmNGlG_Q==/com.buddyapps.pdfstudio34-9W4ZYV-d74AosMLfItX5UA==/base.apk`; `dumpsys package` confirma versionName 3.4.
- La app se abrió en la pantalla de fondo de Buddy y el proceso sigue vivo (`pidof` devolvió 21348), así que arranca sin crash inmediato. Buddy no puede leer esa ventana (la app oculta su contenido), por lo que la verificación funcional completa (T-08) sigue pendiente.
- La v3.3 original (`com.buddyapps.pdfstudio`) permanece instalada con sus documentos intactos.

## 2026-11-23 — Lote: accesibilidad y estados visuales v3.6
- Tareas: T-09 (statBlock), D-04, D-05, D-06, D-09, D-10, D-11, D-12, D-13, D-14.
- Cambios: `MainActivity.java` pasa `String.valueOf(n)` a `statBlock` en Inicio/Biblioteca; error de arranque tematizado con Reintentar/Cerrar; títulos de visor/Unión/creador a 22 sp con elipsis; dock con 48 dp y etiquetas 11 sp; IconToggle y dock con descripción; buscador con ProgressBar, estados y colores semánticos; Unir usa `DesignSystem.emptyState` con CTA; flechas de orden y chips de anotación a 48 dp; creador con márgenes uniformes. `DesignSystem.java`: `TOUCH_MIN=48`, `iconLabel()`, overload de `iconButton` con descripción, `addPressScale()` que respeta "reducir movimiento", `statBlock` de una sola línea, press scale en tarjetas/chips/botones de icono. `Icons.java`: iconos decorativos ocultos a TalkBack. Versión 12 / 3.6, etiqueta "PDF Studio 3.6" y textos "v3.6" coherentes.
- Compilación: `HOME=/root SDK_JAR=/root/android-sdk/android-36/android.jar bash /root/.buddy/work/.claude/skills/android-app/build-apk.sh /root/.buddy/work/pdfstudio` → éxito; APK SHA-256 `0944073ed772217cc7fa70a0db70ba8d08e4217d9e85df0481cc4625af32321e`; package `com.buddyapps.pdfstudio34`, versionCode 12, versionName 3.6; certificado `c8075235…` (idéntico al instalado).
- Pruebas: no hay suite de tests en este proyecto Java/XML; se ejecutó el recorrido manual en dispositivo.
- Verificación en dispositivo: el usuario confirmó la instalación; `wait_for_install` devolvió `Installed com.buddyapps.pdfstudio34 (versionCode 12)`. Con `read_screen` se comprobó: contador "3" + etiqueta separada en Inicio y Biblioteca; descripciones en todos los iconos del visor, filas de Unir y chips de Anotar; estado inicial y "Sin coincidencias" del buscador con flechas desactivadas; espaciado uniforme del creador; sin crashes al recorrer esas pantallas y cancelar la hoja de anotación.
- Auditoría visual: se revisaron Inicio, Biblioteca, Visor, hoja Buscar, Unir, Creador y hoja Anotar. Sin solapes ni textos partidos en los elementos legibles por read_screen. El usuario declaró que no puede ver capturas, así que no se usaron como evidencia.
- Regresiones: ninguna observada en las pantallas recorridas; los 3 documentos siguen listados con sus tamaños.
- Pendiente de evidencia: empty state de Unir (hay documentos, no se borraron), pantalla de error de arranque (no se provocó) y animación de escala (read_screen no la muestra): implementados y compilados, no marcados como vistos en pantalla.
- Siguiente tarea: T-08, completar el recorrido funcional de compresión, unión real y escaneo sobre v3.6.

## 2026-11-23 — Lote: sistema iconográfico profesional v3.6 (v15)
- Requerimiento del usuario: renovar TODOS los iconos usando recursos profesionales buscados y descargados de Internet, no dibujados desde cero ni limitados a los existentes.
- Investigación: se compararon Lucide (ISC), Tabler (MIT), Phosphor (MIT), Material Symbols/Material Icons (Apache 2.0), Compose Icons (no aplica), Iconify (agregador), Iconfinder/Icons8/Flaticon (licencias mixtas/de pago) y herramientas IA (Recraft) / editores (Inkscape, Figma, Illustrator). Se eligió **Lucide** por licencia ISC comercial, grid 24×24, trazo 2 uniforme y cobertura completa.
- Descarga real: 51 SVG originales desde `raw.githubusercontent.com/lucide-icons/lucide/main/icons/`, guardados en `docs/icon-system/lucide-svg/`. Licencias ISC/MIT guardadas en `docs/icon-system/`.
- Conversión: `tools/build_vectordrawables.py` traduce los SVG a VectorDrawable XML en `res/drawable/ic_*.xml` (52 recursos + launcher). Nombres upstream corregidos contra el árbol Git real (`triangle-exclamation-point`, `trash`, `grid-2x2`). `ic_starfill.xml` es la variante rellena del mismo `star` para el estado seleccionado.
- Integración: `Icons.java` reescrito como fachada de recursos (ImageView + tint `SRC_ATOP`, caché de `getIdentifier`, fallback a `ic_doc`). Se eliminaron el renderer Canvas, el parser de paths y la caché de `Path`. Aliases semánticos (`files`, `edit`, `prev`, `next`, `zoom_in`, `zoom_out`, `warning`, `link`) apuntan a un único recurso. `ToolChip` tiene estado activo con color primario, fondo, peso de texto y `contentDescription` «, seleccionada».
- Limpieza: eliminado `createIconBadge` (emoji, sin usos); reemplazado `☑` del Toast por «Primero selecciona este documento en la lista»; launcher genérico `@android:drawable/sym_def_app_icon` sustituido por `ic_launcher` adaptativo propio.
- Compilación: misma orden exacta, `HOME=/root`, sin cambios en la firma (`c8075235…`). versionCode 15, versionName 3.6, SHA-256 `7b7d3de754b3e05334317de2d41f981823f81bfaa61dbd5a6ee854e76efd7ef2`, 53 vectores dentro del APK.
- Instalación: pendiente de que el usuario devuelva el teléfono (las herramientas informaron PAUSED mientras lo usaba). La verificación final con `read_screen` queda registrada en T-12.
- Informe completo: `docs/ICON_SYSTEM.md`.
