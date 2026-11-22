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
