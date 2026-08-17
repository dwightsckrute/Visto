# Referencia visual: Essentials

## Alcance de la inspección

Referencia: [sameerasw/essentials](https://github.com/sameerasw/essentials), inspeccionada en modo de solo lectura en el commit [`827d414`](https://github.com/sameerasw/essentials/tree/827d414cb8bda786142e70b1b60ed404a91c1c88) el 17 de agosto de 2026.

Se revisaron README/capturas, CONTRIBUTING, catálogo de versiones, tema, componentes UI, toolbar flotante, app bars, contenedores, controles segmentados, pantallas principales/ajustes, animación, blur, insets y documentación de estructura. No se copió código. Root, Shizuku, permisos privilegiados, automatización, GenAI y servicios del sistema quedan explícitamente fuera del alcance de Visto.

## Archivos relevantes observados

- [README y capturas](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/README.md)
- [CONTRIBUTING.md](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/CONTRIBUTING.md)
- [Catálogo de versiones](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/gradle/libs.versions.toml)
- [Tema y color dinámico](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/theme/Theme.kt)
- [Escala de shapes](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/theme/Shapes.kt)
- [Tipografía Google Sans Flex](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/theme/Type.kt)
- [Toolbar flotante](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/components/EssentialsFloatingToolbar.kt)
- [Top app bar reutilizable](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/components/ReusableTopAppBar.kt)
- [Contenedor redondeado agrupado](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/core/containers/RoundedCardContainer.kt)
- [Selector segmentado](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/core/pickers/SegmentedPicker.kt)
- [MainActivity: edge-to-edge, pager y motion](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/MainActivity.kt)
- [SettingsActivity](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/app/src/main/java/com/sameerasw/essentials/ui/activities/SettingsActivity.kt)
- [Organización documentada](https://github.com/sameerasw/essentials/blob/827d414cb8bda786142e70b1b60ed404a91c1c88/docs/STRUCTURE.md)

## Lo observado

Essentials combina:

- Google Sans Flex, shapes 12/16/20 dp y superficies Material tonales.
- Color dinámico por defecto, modo oscuro y variante pitch black.
- Contenido edge-to-edge con insets explícitos.
- Una toolbar inferior flotante que alterna modo de tabs y modo contextual.
- Selección activa con contenedor lleno y etiqueta expandida.
- FAB desacoplado para la acción principal.
- App bars flexibles con iconos en contenedores tonales.
- Ajustes agrupados en contenedores redondeados con separación mínima entre filas.
- Controles segmentados basados en `ToggleButton` y button groups Expressive.
- Adaptación puntual de etiqueta según ancho y font scale.
- Motion con springs para cambios de anchura y tweens/visibility para contenido.
- Blur progresivo opcional cerca de bordes y bastante UI/state directamente en Activities/composables.

Essentials usa Kotlin 2.4.10, AGP 9.3.1, Compose BOM 2026.06.01 y Material 3 1.5.0-alpha24. Esas versiones no son un requisito visual para Visto.

## Patrones que merece la pena adoptar

### 1. Jerarquía de superficie

Agrupar ajustes relacionados dentro de una silueta compartida y separar filas por tono/espacio crea orden sin añadir bordes a todo. En Visto se adapta a filtros, metadatos y secciones de detalle, evitando tarjetas dentro de tarjetas.

### 2. Navegación con selección clara

El patrón visual de contenedor exterior flotante + selección interior + etiqueta activa es apropiado para Inicio/Películas/Series. La acción Buscar separada refuerza que no es un destino equivalente.

La implementación no se copiará: Visto debe usar semántica de navegación y conservar estado por destino, no modelar la barra como una toolbar de acciones.

### 3. Formas por jerarquía

Las esquinas amplias en superficies protagonistas y las formas conectadas en selectores ayudan a expresar relación. Visto añadirá variedad funcional: carátulas, estados, filtros y navegación no compartirán automáticamente la misma forma.

### 4. Etiqueta adaptativa

Essentials decide si mostrar la etiqueta según font scale, ancho y número de items. Visto debe adoptar el principio mediante `WindowSizeClass`/constraints y medición estable, no lectura global dispersa de `LocalConfiguration`.

### 5. Color dinámico y pitch black

La coexistencia de dynamic color y negro puro coincide con Visto. La adaptación cinematográfica mantendrá superficies OLED ligeramente diferenciadas para que números, chips y controles sigan siendo legibles.

### 6. Componentes reutilizables por función

La separación de containers, cards, pickers, sheets y features es una referencia útil. Visto ya tiene `core/ui/components`; debe consolidarlo en lugar de duplicar la jerarquía de Essentials.

### 7. Alcance táctil y app bars

Controles grandes, 48 dp de interacción y acciones superiores con superficie tonal mejoran accesibilidad. En Visto se aplicará con moderación para no competir con carátulas.

## Adaptación cinematográfica para Visto

| Principio Essentials | Adaptación Visto |
| --- | --- |
| Feature cards y grupos de ajustes | Secciones de biblioteca/detalle con carátula dominante y metadatos secundarios |
| Toolbar flotante con tab activo | Navegación semántica flotante con Inicio/Películas/Series |
| FAB adjunto | Buscar como acción global independiente |
| Iconos grandes y shapes expresivos | Filled/outlined para selección y shapes que no oculten la relación de aspecto del póster |
| Contenedores redondeados | Paneles tonales editoriales, sin anidar tarjetas innecesariamente |
| Selector segmentado | Estados Pendiente/Viendo/Vista y filtros excluyentes |
| Etiqueta activa expandida | Etiqueta del destino activo solo cuando cabe y mejora comprensión |
| Dynamic/pitch black | Dynamic/OLED con roles de contenido verificados para puntuaciones y cifras |

## Patrones descartados o limitados

- **Toolbar como navegación:** su apariencia inspira, su semántica no. `FloatingToolbar` queda para acciones contextuales.
- **Anchos animados por propiedad con springs bouncy:** puede descoordinar, rebotar y generar valores problemáticos. Visto coordinará la selección con una transición común y clamps donde sea necesario.
- **Blur progresivo permanente:** no es requisito y puede penalizar GPU/legibilidad. Tonos, elevación y borde son el fallback por defecto.
- **Marquee como solución general:** títulos de medios se truncan o disponen en dos líneas; marquee solo en un contexto donde leer texto largo en espacio fijo sea realmente útil.
- **Haptics en cada control:** solo se añadirán donde la plataforma/componente y la interacción lo justifiquen.
- **Estado/repositorios dentro de UI:** la referencia contiene Activities y composables grandes con mucha coordinación. Visto conserva ViewModels, Hilt, StateFlow y repositorios.
- **Strings hardcodeados o traducción interna propia:** Visto mantiene `localized(es, en)` y su selector actual.
- **Dependencias de funciones avanzadas:** no se incorporan Root, Shizuku, Hidden API, GenAI, Lottie, Sentry, services ni permisos de Essentials.
- **Copiar sus versiones:** alpha24, SDK 37 y Java 21 no son necesarios para el objetivo visual actual.

## Componentes oficiales aprovechables

Disponibles ya o compatibles con el stack actual:

- `NavigationBar` y `NavigationBarItem` para semántica base de destinos.
- `HorizontalFloatingToolbar` para acciones contextuales, ya usado en Visto.
- `TopAppBar` y variantes flexibles disponibles en Material 3 instalado, verificando opt-in.
- `ToggleButton` y button group Expressive para controles segmentados, con shapes explícitos.
- `FilterChip`, `AssistChip` y botones Material 3 según función.
- `Surface`, `Card`, `Scaffold`, `ModalBottomSheet` y roles tonales.
- `AnimatedContent`, `AnimatedVisibility`, `updateTransition`, `animate*AsState`, `spring` y `tween`.
- Shared element APIs de Compose con Navigation Compose 2.9.7, en una fase posterior.
- Predictive back, compatible con Navigation Compose 2.9.7 y target 36.

`NavigationSuiteScaffold` y las suites adaptativas pertenecen a un artefacto que Visto no declara hoy. Se evaluarán únicamente si la fase adaptativa justifica esa dependencia.

## Elementos que requieren implementación propia

- `AppFloatingNavigationBar`: wrapper/composición específica de Visto si la `NavigationBar` oficial no permite el aspecto flotante deseado.
- Indicador interior que se desplaza o transforma como una única transición.
- Lógica compacta para mostrar/ocultar etiqueta activa sin romper español o font scale.
- Tarjeta cinematográfica común, solo cuando Movie/TV compartan realmente API y comportamiento.
- Píldora de estado y puntuación que reutilicen semántica del dominio Visto.

Esos nombres son internos; no se presentarán como APIs oficiales de Material.

## Decisiones que son solo inspiración visual

- Proporción del contenedor flotante y ritmo entre iconos.
- Contraste tonal entre fondo, contenedor exterior e indicador.
- Selección con icono filled y etiqueta activa.
- Agrupación de ajustes en siluetas grandes.
- Mezcla controlada de shapes suaves y componentes táctiles amplios.
- Animación espacial de selección.

No se trasladan literales de código, colores, dimensiones, recursos, contenido ni arquitectura.

## Conclusión

La referencia confirma la dirección visual solicitada, pero Visto debe ser más rigurosa en semántica de navegación, flujo de estado, coordinación de motion, accesibilidad y rendimiento. La adopción se hará por componentes piloto y con pruebas, empezando por tokens y navegación compacta tras aprobación del plan de `docs/ARCHITECTURE.md`.
