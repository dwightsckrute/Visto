package com.dwightsckrute.visto.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Elevaciones compartidas de Visto. Ver `docs/DESIGN_SYSTEM.md`.
 *
 * La profundidad se expresa primero mediante tono (`surfaceContainer*`) y solo después
 * mediante sombra. Un valor fuera de esta escala necesita una razón concreta.
 */
object Elevation {
    /** Sombra suave suficiente para despegar un contenedor flotante de listas y carátulas. */
    val floating = 1.dp

    /** Diálogos y superficies modales propias que no usan el default oficial del componente. */
    val modal = 6.dp
}
