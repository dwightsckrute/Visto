package com.dwightsckrute.visto.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.localization.localized

/**
 * Un texto largo que se puede terminar de leer.
 *
 * Recortar a un número de líneas y poner puntos suspensivos deja el texto a medias sin ninguna
 * salida: una biografía o una sinopsis largas simplemente no se podían leer enteras. Recortar
 * está bien —una ficha no debería abrirse con una pared de texto—, lo que faltaba era la puerta.
 *
 * El enlace solo aparece si el texto **de verdad** no cabe. Preguntárselo al resultado de la
 * medición y no a la longitud en caracteres es la diferencia entre acertar siempre y acertar
 * casi siempre: cuántas líneas ocupa algo depende del ancho, del tamaño de letra del sistema y
 * del idioma, y ninguna de las tres se sabe desde aquí.
 */
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 6,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    var expanded by remember(text) { mutableStateOf(false) }
    var overflows by remember(text) { mutableStateOf(false) }

    Column(modifier = modifier.animateContentSize()) {
        Text(
            text = text,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            // Solo cuenta la medición del estado plegado: una vez desplegado nada sobra, y sin
            // esta guarda el enlace desaparecería justo al pulsarlo.
            onTextLayout = { result ->
                if (!expanded) overflows = result.hasVisualOverflow
            },
        )

        if (overflows) {
            Text(
                text = if (expanded) localized("Leer menos", "Read less")
                else localized("Leer más", "Read more"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { expanded = !expanded },
            )
        }
    }
}
