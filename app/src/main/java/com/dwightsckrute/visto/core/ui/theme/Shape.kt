package com.dwightsckrute.visto.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Escala Material construida sobre [ShapeRadius], de forma que `MaterialTheme.shapes` y los
 * radios usados a mano compartan una única fuente de verdad. Ver `docs/DESIGN_SYSTEM.md`.
 *
 * Los valores coinciden con los defaults de Material 3, así que declararla no cambia el aspecto
 * actual: solo hace explícito el punto donde ajustar la geometría del producto.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(ShapeRadius.ExtraSmall),
    small = RoundedCornerShape(ShapeRadius.Small),
    medium = RoundedCornerShape(ShapeRadius.Medium),
    large = RoundedCornerShape(ShapeRadius.Large),
    extraLarge = RoundedCornerShape(ShapeRadius.ExtraLarge),
)
