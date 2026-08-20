package com.dwightsckrute.visto.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * El contenido cede el sitio cuando sube una hoja.
 *
 * Una hoja inferior vive en su propia ventana por encima de la pantalla, así que sin esto lo que
 * hay detrás se queda exactamente igual, atenuado por el velo y nada más: la hoja parece pegada
 * sobre una captura.
 *
 * Encogiendo lo de detrás un poco, la hoja pasa a estar *delante de algo*. La pantalla se retira
 * para dejarla pasar, que es lo que hace el sistema al abrir una aplicación desde el launcher y lo
 * que ya hace esta aplicación al abrir una ficha: un mismo gesto contado en dos sitios.
 *
 * Poco recorrido, por la misma razón que en la navegación: encoger más levanta la pantalla de sus
 * bordes y el hueco delata el truco en vez de sugerir profundidad.
 */
@Composable
fun Modifier.sheetRecede(active: Boolean): Modifier {
    val progress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "sheet-recede",
    )

    // Quieto no se toca nada. Una capa gráfica y un rectángulo a pantalla completa por sección son
    // gratis de escribir y no de dibujar, y aquí solo hacen falta mientras algo se mueve.
    if (progress == 0f) return this

    // El hueco que deja la pantalla al encogerse. Sin esto asomaría el fondo de la ventana del
    // sistema, que es claro siempre y en modo oscuro se vería como un marco blanco.
    val backdrop = MaterialTheme.colorScheme.surfaceContainerLowest

    return this
        .background(backdrop)
        .graphicsLayer {
            val scale = 1f - (1f - RECEDE_SCALE) * progress
            scaleX = scale
            scaleY = scale
            // El redondeo entra con el encogido para que lo que se retira se lea como una tarjeta
            // y no como la misma pantalla mal encajada.
            shape = RoundedCornerShape((MAX_CORNER * progress).dp)
            clip = true
        }
}

private const val RECEDE_SCALE = 0.955f

/** Redondeo de la pantalla retirada en su punto más encogido. */
private const val MAX_CORNER = 28f
