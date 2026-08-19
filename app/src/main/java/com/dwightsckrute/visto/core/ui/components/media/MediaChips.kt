package com.dwightsckrute.visto.core.ui.components.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.PillSize
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius

/**
 * Chip de una ficha: género, año, nota, progreso.
 *
 * Ya no dibuja nada por su cuenta. Tenía su propio 12sp en negrita, su relleno asimétrico y su
 * radio configurable, y el resultado era que dos chips contiguos de sitios distintos no se
 * parecían. Ahora es [AppPill] en pequeño, así que hereda forma, alturas y tipografía del resto.
 *
 * Sigue existiendo con esta firma porque lo llaman ocho sitios y cambiarlos todos por algo
 * idéntico solo habría hecho el cambio más difícil de revisar.
 */
@Composable
fun MediaChip(
    text: String,
    icon: Int? = null,
    contentColor: Color,
    containerColor: Color,
    @Suppress("UNUSED_PARAMETER") shapeRadius: Dp = ShapeRadius.Full,
) {
    AppPill(
        label = text,
        icon = icon,
        container = containerColor,
        onContainer = contentColor,
        size = PillSize.Small,
    )
}
