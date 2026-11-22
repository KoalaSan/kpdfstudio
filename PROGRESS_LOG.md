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
