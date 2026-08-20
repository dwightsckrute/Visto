# Sistema de diseño de Visto

## Identidad

Visto debe sentirse contemporánea, cinematográfica y claramente Android. Essentials es una referencia de ritmo, formas expresivas, agrupación, navegación flotante y alcance táctil; no es una plantilla ni una fuente de código.

Principios:

- La carátula es el elemento visual principal; los contenedores la acompañan sin competir.
- El título, año, género, estado y puntuación tienen una jerarquía editorial clara.
- El color se expresa mediante roles tonales Material, no mediante colores aislados.
- Las formas varían por función. No toda superficie debe ser una tarjeta ni toda acción una píldora.
- El movimiento explica selección, continuidad y cambio de estado.
- Claro, oscuro, dinámico y OLED son variantes de la misma interfaz, no diseños separados.

Antes de cualquier cambio visual, consultar este documento y `docs/DESIGN_REFERENCE_ESSENTIALS.md`.

## Base existente

El sistema vive actualmente en `core/ui`, por lo que no se creará un segundo árbol `core/designsystem`:

- `core/ui/theme/Theme.kt`: `MaterialExpressiveTheme`, MaterialKolor, color dinámico y OLED.
- `core/ui/theme/Type.kt`: Google Sans Flex y escala Material.
- `core/ui/theme/ShapeRadius.kt`: radios compartidos.
- `core/ui/theme/Shape.kt`: `AppShapes`, la escala `Shapes` de Material derivada de `ShapeRadius` y pasada al tema.
- `core/ui/theme/Spacing.kt`: escala de espaciado y objetivo táctil mínimo.
- `core/ui/theme/Elevation.kt`: elevaciones de contenedor flotante y modal.
- `core/ui/theme/Motion.kt`: `AppMotion`, duraciones de respaldo para transiciones con `tween` explícito.
- `core/ui/theme/MediaStatusColors.kt`: colores semánticos de estado.
- `core/ui/components/`: diálogos, app bars, estados, tiles y piezas de medios.
- `feature/shared/media/`: piezas compartidas específicas del dominio.

Los tokens existen desde 1.9.0 y la migración de literales es progresiva: cada pantalla que se toca adopta el token correspondiente. No deben existir tokens nuevos sin al menos un consumidor real.

## Color

- Usar `MaterialTheme.colorScheme` y parejas semánticas: `primaryContainer/onPrimaryContainer`, `secondaryContainer/onSecondaryContainer`, `surfaceContainerHigh/onSurface`, etc.
- Reservar `primary` para acciones o selección de alta importancia. Usar `secondary` para filtros y `tertiary` para acentos limitados, como puntuaciones, si el contraste funciona.
- Preferir `surfaceContainer`, `surfaceContainerHigh` y `surfaceContainerHighest` para profundidad antes que sombras fuertes.
- El contenido sobre una carátula puede usar blanco/negro fijo solo con scrim funcional que garantice contraste.
- No deducir `contentColor` de un contenedor con `copy(alpha)`: especificar el rol legible.
- Los estados Pendiente, Viendo, Interrumpida y Vista deben combinar texto/icono con color; nunca solo color.

### Temas

- **Claro:** superficies tonales suaves, carátulas con contraste y sin bordes constantes.
- **Oscuro:** evitar negro uniforme salvo OLED; conservar separación por tono.
- **OLED:** fondo/superficie principal negros, pero contenedores interactivos pueden usar `#080808`/`#101010` mediante el tema para seguir siendo distinguibles.
- **Dinámico:** disponible desde Android 12; validar combinaciones extremas de wallpaper y no hardcodear el color de contenido.
- **Paleta generada:** MaterialKolor con `ColorSpec.SpecVersion.SPEC_2025`; cualquier cambio debe comprobar compatibilidad del grafo Compose.

## Tipografía

Google Sans Flex ya aporta personalidad Android. La personalidad cinematográfica proviene de la jerarquía, no de añadir otra fuente:

- `headline*`: títulos de pantalla y detalles, con una línea cuando sea posible.
- `titleLarge/titleMedium`: títulos de medios y secciones.
- `bodyMedium/bodyLarge`: sinopsis y notas.
- `label*`: año, género, estado, filtros y metadatos compactos.
- Evitar tamaños `sp` locales cuando existe un estilo Material equivalente.
- Probar títulos largos, español/inglés, font scale 1.3 y pantallas de 320 dp.

## Formas

Escala base actual (`ShapeRadius`, expuesta a Material como `AppShapes`):

| Token | Radio | Uso orientativo |
| --- | ---: | --- |
| None | 0 dp | imágenes dentro de un contenedor continuo |
| ExtraSmall | 4 dp | detalles internos pequeños |
| Small | 8 dp | controles compactos |
| Medium | 12 dp | chips y elementos de lista |
| Large | 16 dp | tarjetas y botones |
| ExtraLarge | 28 dp | paneles héroe, sheets y navegación exterior |
| Full | 999 dp | píldoras funcionales |

No se aplica el mismo radio a todo. Las carátulas conservan una geometría reconocible; los contenedores editoriales pueden usar esquinas grandes; los grupos conectados necesitan formas diferenciadas en extremos.

Cuando un control debe seguir siendo píldora, fijar explícitamente formas normal, pulsada y seleccionada. Los defaults Expressive pueden transformar la selección en otra geometría.

## Espaciado y tamaño

Escala preferida, disponible como `Spacing` en `core/ui/theme/Spacing.kt`: 4, 8, 12, 16, 24 y 32 dp.

- 4: separación interna mínima.
- 8: icono-etiqueta y elementos compactos.
- 12: filas, chips y tarjetas densas.
- 16: margen principal y padding de contenedores.
- 24: separación de secciones.
- 32: respiración de estados vacíos o hero.

Otros valores solo cuando responden a una especificación real: proporción de póster, altura oficial del componente, stroke, alineación óptica o inset. Los targets interactivos son de al menos 48 dp.

## Elevación

Tokens en `core/ui/theme/Elevation.kt`.

- Base: 0 dp; diferenciar por tono.
- Flotante (`Elevation.floating`, 1 dp): sombra suave, suficiente para separar de carátulas o listas.
- Modal: preferir los defaults oficiales de sheet/dialog; para superficies modales propias, `Elevation.modal` (6 dp).
- Un borde `outlineVariant` sutil es opcional cuando una paleta dinámica no separa dos superficies adyacentes.
- Evitar sombras oscuras fuertes y bordes en cada tarjeta.

## Iconografía

- Reutilizar los drawables Material Symbols del proyecto.
- Navegación: outlined inactivo y filled activo cuando exista pareja coherente.
- Toda acción solo-icono tiene `contentDescription` o `desc`; iconos decorativos usan `null` de forma consciente.
- No mezclar grosores y estilos en una misma barra.
- La puntuación puede usar estrella más número; los estados deben tener etiqueta además del icono cuando el significado no sea obvio.

## Píldoras, chips y controles segmentados

Usar píldoras para:

- Estado de seguimiento: Pendiente, Viendo, Interrumpida, Vista.
- Filtros compactos y géneros seleccionables.
- Indicador activo de navegación.
- Acciones compactas que se entienden como una unidad.

No usar píldoras para párrafos, tarjetas completas, iconos inactivos individuales ni decoración. Un grupo segmentado puede usar `ToggleButton`/button group cuando la versión instalada lo soporte, con semántica de selección única y texto que no se corte.

## Navegación principal flotante

La navegación compacta tendrá tres destinos: Inicio, Películas y Series. Buscar seguirá siendo una acción global separada.

Especificación:

- Un único contenedor exterior en píldora, separado 16 dp de los laterales y respetando `navigationBars`/gestos.
- Superficie tonal (`surfaceContainerHigh` o equivalente), elevación baja y borde opcional.
- Un indicador seleccionado interior, no una píldora por cada destino inactivo.
- Icono filled activo y outlined inactivo; etiqueta activa solo si cabe y mejora comprensión.
- Animación coordinada de indicador, icono, color y etiqueta mediante una transición de selección.
- De tres a cinco destinos; no contiene acciones contextuales.
- En tamaños mayores, evaluar `NavigationRail`/`NavigationSuiteScaffold` en lugar de ensancharla.

Semántica:

- `NavigationBar`: destinos principales persistentes.
- `BottomAppBar`: conjunto de acciones inferiores.
- `FloatingToolbar`: acciones contextuales que pueden expandirse/ocultarse.
- `FloatingActionButton`: acción principal, en Visto habitualmente Buscar o Añadir según contexto.

Si `NavigationBar` no permite el contenedor flotante exacto, `AppFloatingNavigationBar` será una composición propia sobre `Surface` y primitives accesibles, conservando roles/estado de navegación. No se inventarán APIs llamadas `LiquidNavigationBar`, `ExpressiveNavigationBar` o `PillNavigationBar`.

## Componentes objetivo

Buscar primero componentes existentes. Nombres orientativos, no obligación de crear todos:

| Necesidad | Base actual / objetivo |
| --- | --- |
| Navegación compacta | futuro `AppFloatingNavigationBar` |
| Tarjeta de medio | `PosterBox`, `HomeMediaCard`, filas Movie/TV; unificar solo con API real compartida |
| Estado | `WatchlistMediaStatusPill`, `WatchListItemStatusUiPill` |
| Género/filtro | `MediaChips`, `FilterChip` o button group oficial |
| Puntuación | extraer un componente compacto si aparece en dos pantallas |
| Vacío/error/carga | `EmptyContainerPlaceholder`, `ErrorContainer`, `Loading*` |
| App bars | `LargeTopBarScaffold`, `TopAppBar` y variantes oficiales |
| Botones | `ExpressiveButtons` y botones Material 3 |
| Detalle | `MediaDetailsScreenHeader`, héroes Movie/TV y secciones compartidas |

Cada componente reutilizable importante debe aceptar `modifier`, estado habilitado, callbacks mínimos, semántica y preview con datos de muestra cuando sea aislable.

## Motion

El tema ya proporciona `MotionScheme.expressive()`. Usar primero `MaterialTheme.motionScheme`:

- **Efecto rápido:** feedback de icono, color o visibilidad breve.
- **Espacial por defecto:** selección, indicador, cambio de tamaño o contenedor.
- **Espacial lento:** transición de pantalla o hero cuando la continuidad lo justifica.

Cuando una transición necesite un `tween` explícito, usar `AppMotion` (`core/ui/theme/Motion.kt`) en lugar de escribir una duración nueva por pantalla. Hoy define `DurationShort` (200 ms) y `DurationMedium` (350 ms), que es la de los cambios de pantalla; añadir otra solo con un consumidor real.

Reglas:

- Spring para transformaciones físicas interrumpibles; tween para secuencias temporales controladas.
- Coordinar propiedades relacionadas con `updateTransition` o estado compartido.
- No usar infinite transitions salvo actividad real (carga, reproducción o proceso activo).
- Nunca enviar overshoot de spring a padding, tamaño, alpha o progress sin `coerceIn`/clamp.
- `animateContentSize()` precede a modifiers de tamaño que deba animar.
- Las listas usan keys estables antes de `animateItem`.
- Las APIs estándar respetan la escala de animación del sistema; no usar loops o timers manuales.
- Probar una interacción repetida antes de que termine; la animación debe ser interrumpible y estable.

### Continuidad carátula → detalle

Activa. Abrir una película, una temporada o un libro no es un fundido entre dos pantallas: la carátula que se toca crece hasta su sitio en la ficha, y el resto entra a su alrededor.

- Los dos ámbitos (`SharedTransitionScope`, `AnimatedVisibilityScope`) llegan por CompositionLocal desde `core/ui/navigation/SharedPoster.kt`; el `NavHost` los publica y `animatedComposable` evita repetirlo por destino.
- La clave la da `posterSharedKey(id)` y **nunca** se escribe a mano: el origen y el destino tienen que coincidir, y el identificador es el mismo con el que la fila navega (`seasonId` para temporadas).
- Los dos extremos piden `SharedPosterSize`. Con tamaños distintos son dos entradas de caché y la carátula enseña un cargador en mitad del vuelo.
- Sin ámbitos —una vista previa, un test— el modificador no hace nada y queda el fundido. No falla.
- Solo lo lleva quien navega. La fila de una serie se despliega en vez de abrir, así que su carátula no viaja: la que viaja es la de la temporada.

## Edge-to-edge e insets

- `enableEdgeToEdge()` ya es el punto de partida de la Activity.
- Aplicar insets donde existen elementos interactivos; permitir que carátulas/scrims decorativos dibujen detrás del sistema cuando sea legible.
- Preferir `Scaffold` y modifiers de inset que consumen valores, evitando sumar manualmente el mismo inset varias veces.
- La navegación flotante debe respetar `WindowInsets.navigationBars`; búsqueda también debe reaccionar al IME (`adjustResize` ya está declarado).
- Probar navegación por gestos, tres botones, cutout y teclado.

## Diseño adaptativo

- **Compacto:** navegación flotante inferior y contenido de una columna.
- **Medio:** considerar rail y grids; mantener anchura legible de texto.
- **Expandido:** list-detail para biblioteca/detalle cuando aporte valor; no escalar tarjetas sin límite.
- No añadir Material 3 Adaptive hasta que la navegación compacta esté estable y exista aprobación de dependencia.

## Accesibilidad y rendimiento

- Targets de 48 dp, descripciones útiles, semántica de seleccionado y orden de foco.
- Contraste en todas las paletas y estados; no depender solo de color.
- Texto sin corte a font scale alto y etiquetas españolas largas.
- Colecciones grandes en `LazyColumn`, `LazyRow` o grid con keys estables.
- Evitar listas mutables o parámetros inestables en composables de alta frecuencia.
- No usar blur permanente. Profundidad con tono, elevación y borde consume menos y es más legible.
- Previews no sustituyen pruebas en dispositivo, especialmente para dynamic color, OLED e insets.

## Referencias oficiales

- [Material 3 en Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Navigation bar en Compose](https://developer.android.com/develop/ui/compose/components/navigation-bar)
- [Navigation bar — Material Design 3](https://m3.material.io/components/navigation-bar/overview)
- [Toolbars — Material Design 3](https://m3.material.io/components/toolbars/overview)
- [Navegación adaptativa](https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation)
- [Insets Material 3](https://developer.android.com/develop/ui/compose/system/material-insets)
- [Configuración edge-to-edge](https://developer.android.com/develop/ui/compose/system/setup-e2e)
- [Shared elements en Compose](https://developer.android.com/develop/ui/compose/animation/shared-elements)
- [Shared elements con Navigation](https://developer.android.com/develop/ui/compose/animation/shared-elements/navigation)
- [Predictive back](https://developer.android.com/develop/ui/compose/system/predictive-back-setup)
- [Estabilidad y rendimiento Compose](https://developer.android.com/develop/ui/compose/performance/stability)
- [Documentación Kotlin](https://kotlinlang.org/docs/home.html)
- [Fuentes oficiales AndroidX](https://github.com/androidx/androidx)

## Checklist visual mínima

- Español e inglés.
- Claro, oscuro, dinámico y OLED.
- 320 dp, ancho compacto habitual y landscape/tablet si afecta layout.
- Font scale 1.0 y al menos 1.3.
- Navegación por gestos e IME cuando corresponda.
- Loading, empty, content y error.
- TalkBack/semántica básica, contraste y touch targets.
- Animación interrumpida repetidamente, sin overshoot inválido.
