package com.dwightsckrute.visto.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing

/**
 * La píldora de Visto.
 *
 * Había ocho maneras distintas de dibujar lo mismo: la de las cifras de inicio, el `MediaChip` con
 * su 12sp a mano, las de género de la cabecera, las del diario, la del progreso de una temporada.
 * Cada una con su radio, su relleno y su tipografía. Esta es una sola, con la forma de la de
 * inicio, que es la que mejor funcionaba: superficie tonal, esquina muy redondeada, el icono
 * metido en un círculo traslúcido del propio color de contenido y el texto en dos piezas —una
 * cifra y su etiqueta— cuando hace falta.
 *
 * Dos tamaños y no uno. [PillSize.Large] es la de inicio tal cual. [PillSize.Small] es la misma
 * píldora encogida, para donde una de 48dp no cabe sin desarmar lo que la rodea: tres géneros y
 * una nota bajo el título de una ficha, o una chapa dentro de la cabecera de una sección. Mismo
 * diseño, mismas proporciones, dos escalas; no ocho.
 */
enum class PillSize { Large, Small }

@Composable
fun AppPill(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    icon: Int? = null,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    onContainer: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: PillSize = PillSize.Large,
    contentDescription: String? = null,
) {
    val metrics = size.metrics()

    Surface(
        shape = RoundedCornerShape(metrics.radius),
        color = container,
        contentColor = onContainer,
        // La cifra y su etiqueta se anuncian como una sola frase; leerlas por separado no dice
        // nada ("38", "Guardado").
        modifier = modifier.clearAndSetSemantics {
            this.contentDescription = contentDescription
                ?: listOfNotNull(value, label).joinToString(" ")
        },
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = metrics.minHeight)
                .padding(horizontal = metrics.horizontal, vertical = metrics.vertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(metrics.gap),
        ) {
            if (icon != null) {
                if (metrics.iconWell) {
                    Surface(shape = CircleShape, color = onContainer.copy(alpha = IconWellAlpha)) {
                        Symbol(
                            icon = icon,
                            desc = null,
                            color = onContainer,
                            size = metrics.icon,
                            modifier = Modifier.padding(6.dp),
                        )
                    }
                } else {
                    Symbol(icon = icon, desc = null, color = onContainer, size = metrics.icon)
                }
            }
            if (value != null) {
                Text(text = value, style = metrics.valueStyle())
            }
            Text(
                text = label,
                style = metrics.labelStyle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** El círculo tras el icono. Sale del color de contenido para no añadir un color más al tema. */
private const val IconWellAlpha = .12f

private class PillMetrics(
    val radius: Dp,
    val minHeight: Dp,
    val horizontal: Dp,
    val vertical: Dp,
    val gap: Dp,
    val icon: Dp,
    /** El círculo tras el icono. Solo en la grande: en la pequeña era casi toda su altura. */
    val iconWell: Boolean,
    val large: Boolean,
) {
    @Composable
    fun valueStyle() = if (large) {
        MaterialTheme.typography.headlineSmall
    } else {
        MaterialTheme.typography.titleSmall
    }

    @Composable
    fun labelStyle() = if (large) {
        MaterialTheme.typography.labelLarge
    } else {
        MaterialTheme.typography.labelSmall
    }
}

private fun PillSize.metrics(): PillMetrics = when (this) {
    PillSize.Large -> PillMetrics(
        radius = ShapeRadius.ExtraLarge,
        minHeight = Spacing.minTouchTarget,
        horizontal = Spacing.lg,
        vertical = Spacing.md,
        gap = Spacing.sm,
        icon = 18.dp,
        iconWell = true,
        large = true,
    )
    // Sin el círculo del icono y con menos aire vertical. Con ellos, tres géneros y una nota
    // bajo el título de una ficha pesaban más que el propio título.
    PillSize.Small -> PillMetrics(
        radius = ShapeRadius.ExtraLarge,
        minHeight = 24.dp,
        horizontal = 10.dp,
        vertical = 2.dp,
        gap = 3.dp,
        icon = 14.dp,
        iconWell = false,
        large = false,
    )
}
