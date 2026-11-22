# PDF Studio — Sistema de diseño v4 ("Graphite")

Documento de referencia para el rediseño. Deriva de las skills instaladas
(`visual-design-foundations`, `design-system-patterns`, `interaction-design`,
`accessibility-compliance`, `mobile-android-design`, `design`) y de las reglas
del proyecto: Java + Views del framework, sin AndroidX, sin Gradle, sin recursos
de terceros.

## 1. Principios

1. **Contenido primero.** El documento es el héroe; el cromo (barras, badges) se retira.
2. **Una sola voz de color.** Un acento; el resto es neutro. El color comunica estado, no decora.
3. **Rejilla de 8.** Todo espaciado y tamaño de icono múltiplo de 4/8.
4. **Iconos dibujados, no emoji.** Trazo monocromo 1.8 dp, 24 dp de caja: coherente y nítido.
5. **Jerarquía por peso y espacio**, no por cajas dentro de cajas.
6. **Movimiento corto y funcional**: 120–220 ms, sin rebotes decorativos.

## 2. Tokens

### 2.1 Color — tema oscuro (por defecto)

| Token | Valor | Uso |
|---|---|---|
| `bg` | `#0B0C0F` | Fondo de app |
| `surface` | `#13151A` | Tarjetas, hojas |
| `surfaceRaised` | `#191C23` | Elementos elevados, campos |
| `surfaceSunken` | `#0E1014` | Barra inferior, cabecera |
| `border` | `#23262F` | Separadores 1 dp |
| `borderStrong` | `#333845` | Foco, selección |
| `text` | `#F4F5F7` | Texto primario |
| `textSecondary` | `#A0A6B4` | Metadatos |
| `textTertiary` | `#6C7280` | Placeholder, deshabilitado |
| `accent` | `#5B8CFF` | Acción primaria, selección |
| `accentPressed` | `#4773E0` | Estado pulsado |
| `accentTint` | `#16213D` | Fondo de badge/selección suave |
| `success` | `#34D399` | Confirmaciones |
| `warning` | `#F5B544` | Avisos |
| `danger` | `#F26D6D` | Borrar, errores |

### 2.2 Color — tema claro

`bg #F6F7F9` · `surface #FFFFFF` · `surfaceRaised #FFFFFF` · `surfaceSunken #EEF0F4`
· `border #E3E6EC` · `borderStrong #C9CED8` · `text #14161B` · `textSecondary #5B6270`
· `textTertiary #8A909C` · `accent #2F6BFF` · `accentTint #E8EEFF`.

Regla: el acento se aclara en oscuro y se oscurece en claro para mantener ≥ 4.5:1
sobre su fondo. Nunca texto `textTertiary` sobre `surfaceSunken` para información necesaria.

### 2.3 Tipografía

Familia: `sans-serif` del sistema (Roboto). Números tabulares donde haya métricas.

| Rol | Tamaño / interlineado | Peso | Uso |
|---|---|---|---|
| Display | 28 / 34 | 600 | Título de pantalla, cifra grande |
| Title | 20 / 26 | 600 | Título de sección |
| Subtitle | 16 / 22 | 600 | Nombre de documento, botón |
| Body | 14 / 20 | 400 | Texto, descripciones |
| Caption | 12 / 16 | 500 | Metadatos, chips |
| Micro | 11 / 14 | 600 | Etiquetas de nav, badges |

### 2.4 Espaciado (8 pt)

`2, 4, 8, 12, 16, 20, 24, 32, 40, 48` → tokens `xs…3xl`.
Margen lateral de pantalla: **20 dp**. Separación entre secciones: **28 dp**.
Gap entre tarjetas: **12 dp**.

### 2.5 Radios y elevación

`sm 8` · `md 12` · `lg 16` · `xl 20` · `pill 999`.
Sin sombras duras: elevación = borde 1 dp + diferencia de superficie (+ `elevation 1 dp`
solo en elementos flotantes).

### 2.6 Iconografía

Set propio dibujado con `Canvas` (24 dp, trazo 1.8, remates redondos):
home, files, tools, settings, search, plus, scan, merge, compress, rotate,
edit, sign, scissors, trash, star, chevron, close, back, more, check, alert, empty-doc.
Un icono = una idea; nunca dos glifos para la misma acción.

## 3. Componentes

| Componente | Especificación |
|---|---|
| **Botón primario** | Alto 48, radio 12, `accent`, texto 15/600 blanco; pulsado `accentPressed`; deshabilitado 40 % alpha |
| **Botón secundario** | Alto 44, borde `border`, fondo transparente, texto `text` |
| **Botón de icono** | 44×44 mínimo, radio 12, ripple sutil |
| **Campo de búsqueda** | Alto 48, radio 12, fondo `surfaceRaised`, icono 20 + placeholder `textTertiary`; foco borde `accent` |
| **Tarjeta de documento** | Radio 16, padding 14, fila: badge PDF 44×44, textos, chevron; subtítulo `páginas · tamaño · fecha`; pulsado `surfaceRaised` |
| **Fila de herramienta** | 56 dp, icono 40 en badge tintado, título 15/600, descripción 12, chevron |
| **Chip** | Alto 32, radio pill, seleccionado `accentTint` + texto `accent` + borde `accent` 40 % |
| **Barra inferior** | Alto 64 + inset, fondo `surfaceSunken` con borde superior, 4 ítems: icono 24 + micro etiqueta; activo `accent` + píldora `accentTint` detrás del icono |
| **Cabecera** | 56 dp, título 20/600, acción a la derecha; se contrae al hacer scroll (título 16) |
| **Diálogo** | Hoja centrada radio 20, fondo `surface`, título 18/600, cuerpo 14, acciones alineadas a la derecha; scrim 60 % |
| **Menú contextual** | Hoja inferior radio 20 superior, asas 36×4, ítems 52 dp |
| **Estado vacío** | Icono 56 en círculo `surfaceRaised`, título 16/600, texto 13 `textSecondary`, CTA primario |
| **Carga** | Esqueleto con bloques `surfaceRaised` y pulso 1.2 s; nunca spinner a pantalla completa |
| **Error** | Icono `danger`, mensaje claro, acción "Reintentar" |

## 4. Pantallas y jerarquía

1. **Inicio** — saludo/contexto, buscador, acceso rápido (Escanear, Crear, Unir, Comprimir), "Recientes" (3), "Favoritos".
2. **Biblioteca** — buscador, chips (Todos / Recientes / Favoritos / Creados), lista de documentos con metadatos; acciones por long-press.
3. **Herramientas** — grid 2×N de acciones con descripción de una línea; sección "Del dispositivo" (cámara, galería, texto).
4. **Ajustes** — grupos: Apariencia, Almacenamiento, Acerca de; filas con valor a la derecha.
5. **Visor** — cabecera compacta (atrás, título, más), página centrada, píldora "3 / 12", dock inferior de 2 filas con acciones primarias y secundarias.
6. **Unir** — lista seleccionable con orden, barra inferior fija "Unir N documentos".
7. **Creador** — formulario (título, texto), vista previa de página, CTA fija.
8. **Diálogos** — buscar, extraer, borrar, anotar, salto de página, con el mismo componente de hoja.

Cada pantalla: **un** objetivo, **una** acción primaria, estados vacío/carga/error definidos.

## 5. Movimiento

- Cambio de pestaña: fundido + desplazamiento 8 dp, 160 ms.
- Apertura de hoja: 200 ms, ease-out; cierre 140 ms.
- Pulsación: escala 0.98 en 90 ms (solo tarjetas y botones).
- Respetar "reducir movimiento" del sistema: sin desplazamientos, solo fundido.

## 6. Accesibilidad

Objetivo táctil ≥ 44 dp (48 recomendado), contraste ≥ 4.5:1 en texto y ≥ 3:1 en iconos
y bordes de control, `contentDescription` en todo icono accionable, texto escalable
(`sp`), foco visible con borde `accent` 2 dp.

## 7. Implementación

- `ui/DesignSystem.java`: tokens (`Theme` oscuro/claro), escala tipográfica, espaciados,
  fábricas de botón/campo/tarjeta/fila/chip/hoja/estado, y `Icons.java` con el set dibujado.
- `MainActivity.java`: las pantallas se reconstruyen sobre esas fábricas; la lógica
  (motor, almacenamiento, insets, cámara, búsqueda) no se toca.
- Verificación: capturas reales del dispositivo por pantalla + comprobación de contraste
  y de objetivos táctiles; sin Roborazzi (requiere Gradle/Robolectric, incompatible aquí).
