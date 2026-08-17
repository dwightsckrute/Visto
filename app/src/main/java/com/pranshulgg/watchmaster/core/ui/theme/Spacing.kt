package com.pranshulgg.watchmaster.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Escala de espaciado semántica de Visto. Ver `docs/DESIGN_SYSTEM.md`.
 *
 * Cualquier valor fuera de esta escala debe responder a una especificación real:
 * proporción de carátula, altura oficial de un componente, stroke o inset del sistema.
 */
object Spacing {
    /** Separación interna mínima entre elementos de una misma unidad. */
    val xs = 4.dp

    /** Icono-etiqueta y elementos compactos. */
    val sm = 8.dp

    /** Filas, chips y tarjetas densas. */
    val md = 12.dp

    /** Margen principal y padding de contenedores. */
    val lg = 16.dp

    /** Separación entre secciones. */
    val xl = 24.dp

    /** Respiración de estados vacíos y bloques héroe. */
    val xxl = 32.dp

    /** Tamaño mínimo de un objetivo táctil accesible. */
    val minTouchTarget = 48.dp
}
