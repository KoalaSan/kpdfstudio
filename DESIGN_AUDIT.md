# Auditoría de diseño — PDF Studio (FASE 1)

Fecha: 2026-11-22. Alcance: Java/XML sin AndroidX; se audita el código de todas las pantallas porque el modelo no puede inspeccionar capturas (limitación declarada). Estado visual final debe confirmarlo el usuario en el dispositivo.

## Pantallas analizadas
1. Inicio (dashboard): cabecera, búsqueda, accesos rápidos, categorías, recientes.
2. Biblioteca: resumen, búsqueda, categorías, lista, importar.
3. Herramientas: grupos Organizar / Crear / Pruebas.
4. Ajustes: apariencia, modo lectura, almacenamiento, acerca de.
5. Visor PDF: cabecera, página, dock de navegación y herramientas.
6. Buscar en el documento (bottom sheet).
7. Ir a página / Extraer páginas / Eliminar página (bottom sheets).
8. Unir PDFs: selección, orden, importación.
9. Anotar y firmar (bottom sheet).
10. Creador de documento.
11. Estado de error de arranque.

## Hallazgos y prioridades

| ID | Prioridad | Pantalla(s) | Hallazgo | Acción |
| :-- | :-- | :-- | :-- | :-- |
| D-01 | P1 | Inicio | Cuatro accesos rápidos en una sola fila: targets estrechos y jerarquía plana | Cabecera tipo dashboard con métricas y rejilla 2×2 |
| D-02 | P1 | Biblioteca | Estadísticas en pills sin jerarquía | Tarjeta resumen con bloques de métrica |
| D-03 | P1 | Todas | Tarjetas sin elevación; superficies planas y difíciles de distinguir | Sombra por capas + estados pulsados centralizados |
| D-04 | P1 | Visor | Iconos del dock con minHeight 38 dp y etiquetas de 9 sp | Targets 48 dp y etiquetas 11 sp |
| D-05 | P1 | Unir / Anotar / Nav | Botones de orden (36 dp) y chips de herramientas (40 dp) por debajo de 48 dp | Subir a 48 dp |
| D-06 | P1 | Todas | Botones de icono sin `contentDescription` (TalkBack) | Añadir descripción en componentes compartidos |
| D-07 | P1 | Todas | Escala tipográfica inconsistente (11/13/14/15/16 sp mezclados) | Escala única: 12/13/15/17/22/32 |
| D-08 | P2 | Herramientas | Títulos de sección sin indicador visual | Barra de acento reutilizable |
| D-09 | P2 | Buscar | Sin indicador de carga ni estados semánticos de resultado | ProgressBar + estados con color |
| D-10 | P2 | Unir | Estado vacío como `TextView` suelto | Usar `DesignSystem.emptyState` |
| D-11 | P2 | Visor / Unir / Creador | Títulos de cabecera de 16 sp frente a 22 sp del resto | Unificar a `TEXT_TITLE` |
| D-12 | P2 | Arranque | Pantalla de error con rojo plano sobre negro, sin tema | Diálogo/color del sistema de diseño |
| D-13 | P2 | Creador | Espaciado irregular entre etiquetas y campos | Márgenes consistentes con tokens |
| D-14 | P2 | Todas | Sin feedback de pulsación animado en tarjetas | Animación de escala al pulsar (liviana) |

## Ya corregido en el lote v3.5
D-01, D-02, D-03, D-07, D-08, parte de D-06 (tarjetas y accesos rápidos).

## Lote v3.6 (2026-11-23) — hallazgos corregidos
- StatBlock del dashboard: `docLabel(n)` devolvía "n documentos" y se partía en tres columnas; ahora se pasa `String.valueOf(n)` y el valor se limita a una línea.
- D-04: dock del visor con targets de 48 dp y etiquetas de 11 sp.
- D-05: flechas de orden y chips de anotación a 48 dp; `TOUCH_MIN` global a 48 dp.
- D-06: `contentDescription` en iconButton, IconToggle, dock, nav, filas de unión y chips; los iconos decorativos se ocultan a TalkBack.
- D-09: buscador con estado inicial, indicador de carga, estados sin coincidencias/error y colores semánticos.
- D-10: estado vacío de Unir con `DesignSystem.emptyState` y CTA para importar.
- D-11: títulos de visor, Unir y creador unificados a 22 sp con elipsis.
- D-12: pantalla de error de arranque con tema Graphite, mensaje claro y acciones Reintentar/Cerrar.
- D-13: espaciado regular entre etiquetas y campos en el creador.
- D-14: animación de escala ligera al pulsar tarjetas, botones de icono, chips y filas.

### Evidencia en dispositivo (`read_screen`, v3.6, versionCode 12)
- Inicio: `TextView "3"` + `TextView "Documentos"` en posiciones separadas (377/439 px), sin "3 documentos" partido.
- Biblioteca: `TextView "3"` + `TextView "En la biblioteca"` (349/411 px).
- Visor: descripciones "Volver", "Añadir a favoritos", "Activar modo noche", "Buscar", "Ir a página", "Compartir", "Anterior", "Siguiente", "Zoom -/+", "Girar", "Anotar", "Extraer", "Borrar".
- Buscar: estado inicial "Busca una palabra o frase del documento."; después de buscar "ejemplo", "Sin coincidencias (o el PDF no tiene capa de texto)" con flechas desactivadas.
- Unir: filas con descripción "…, no seleccionado, 1 página" y flechas "Subir en el orden"/"Bajar en el orden".
- Creador: etiquetas TÍTULO/CONTENIDO y campos alineados al mismo eje horizontal con separaciones regulares.
- Anotar: chips "Bolígrafo", "Resaltador", "Firma", "Limpiar" con descripción y sin diálogo de error.

Sin evidencia visual: el estado vacío de Unir (hay 3 documentos, no se borraron datos), la pantalla de error de arranque (no se provocó a propósito) y la animación de escala (read_screen no muestra animaciones). Quedan implementados en código y verificados por compilación.
