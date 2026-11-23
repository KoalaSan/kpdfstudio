# Cola de tareas — PDF Studio

Prioridades: P0 pérdida de datos / roto, P1 simulado o no cableado, P2 UI-accesibilidad, P3 rendimiento, P4 documentación.

| ID | Título | Prioridad | Criterio de aceptación verificable | Estado |
| :--- | :--- | :--- | :--- | :--- |
| T-01 | Corregir fuga de memoria en el visor (bitmaps) | P3 | Navegar 20 páginas y hacer zoom repetido sin crash; la app sigue respondiendo y las páginas se renderizan | implementado, sin verificar en dispositivo |
| T-02 | No asignar overlay de búsqueda cuando no hay coincidencias | P3 | Visor sin cambios visuales; al buscar y resaltar sigue funcionando | implementado, sin verificar en dispositivo |
| T-03 | Importar todos los PDFs seleccionados al unir (ClipData) | P1 | El selector multiselección importa cada URI; al menos la ruta de un solo archivo sigue funcionando | implementado, sin verificar en dispositivo |
| T-04 | Resaltado de búsqueda tras rotar la página | P1 | Buscar "ejemplo", girar 90°, el resaltado sigue visible sobre la palabra | implementado, sin verificar en dispositivo |
| T-05 | Textos obsoletos v3.1 → v3.3 | P4 | El documento de prueba y la guía muestran v3.3 en pantalla | implementado como v3.4, sin verificar |
| T-06 | Re-verificación completa en dispositivo (regresiones) | P0 | Abrir, buscar, comprimir, unir, escanear y anotar sin errores nuevos | bloqueado: instalar v3.4 con la firma correcta |
| T-07 | Instalar v3.4 sin destruir la v3.3 | P0 | `com.buddyapps.pdfstudio34` (PDF Studio 3.4) aparece instalado junto a `com.buddyapps.pdfstudio` | ✅ instalado: versionCode 10, confirmado por el usuario |
| T-08 | Verificar la app nueva en dispositivo | P0 | Abrir, buscar, comprimir, unir, escanear y anotar en `com.buddyapps.pdfstudio34` sin errores nuevos | en curso: se recorrieron Inicio, Biblioteca, Visor, Buscar, Unir, Creador y Anotar en v3.6 sin errores; compresión/escaneo/union real siguen sin re-ejecutarse en este lote |
| T-09 | StatBlock del dashboard: valor partido en 3 columnas | P2 | `String.valueOf(n)` y valor en una línea en Inicio y Biblioteca | ✅ hecho y verificado con read_screen en v3.6 |
| T-10 | D-04/05/06 targets táctiles y contentDescription | P2 | Iconos del visor, unión y anotación con descripción y 48 dp | ✅ implementado y verificado estructuralmente con read_screen en v3.6 |
| T-11 | D-09/10/11/12/13/14 estados, títulos y microinteracciones | P2 | Buscador con estados, Unir con empty state, títulos 22 sp, error tematizado, creador alineado, escala al pulsar | implementado y compilado en v3.6; verificado en dispositivo salvo empty state de Unir, pantalla de error de arranque y animación (no provocables/visibles con read_screen) |
| T-12 | Sistema iconográfico profesional (Lucide ISC) | P2 | 51 SVG descargados y convertidos a VectorDrawable; launcher propio; aliases, estados y TalkBack; sin emoji; compila y se verifica en dispositivo | ✅ implementado y compilado en v15; pendiente de verificación final en dispositivo por pausa del usuario; informe en docs/ICON_SYSTEM.md |
