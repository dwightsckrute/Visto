package com.dwightsckrute.visto.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * El contenido cede el sitio mientras sube una hoja, y lo recupera mientras baja.
 *
 * Una hoja inferior vive en su propia ventana por encima de la pantalla, así que sin esto lo que
 * hay detrás se queda exactamente igual, atenuado por el velo y nada más: la hoja parece pegada
 * sobre una captura. Encogiendo lo de detrás, la hoja pasa a estar *delante de algo*, igual que
 * hace el sistema al abrir una aplicación desde el launcher y que esta aplicación al abrir una
 * ficha.
 *
 * Lo que importa aquí es de dónde sale el movimiento. Animar contra un booleano —«la hoja está
 * abierta»— daba dos animaciones en paralelo con la misma duración por casualidad: cada una con
 * su curva, y en cuanto una tardaba algo distinto de la otra se veía la pantalla acabar de
 * encogerse con la hoja ya quieta, o volver a su sitio cuando la hoja ya se había ido.
 *
 * Así que el encogido no se anima: se **lee de la hoja**. `requireOffset()` es dónde está su
 * borde superior ahora mismo, y la escala se deriva de cuánta pantalla tapa. Ni sincronía que
 * mantener ni curva que igualar, porque hay un único movimiento; y arrastrando la hoja con el
 * dedo la pantalla de detrás sigue al dedo, que es lo que delata que están conectadas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Modifier.sheetRecede(sheetState: SheetState): Modifier {
    // Ni la capa gráfica ni el rectángulo de fondo existen mientras no hay hoja de por medio.
    // `isVisible` sigue siendo cierto durante toda la bajada y `targetValue` lo es desde el
    // primer frame de la subida, así que entre los dos cubren el trayecto completo.
    val engaged = sheetState.isVisible || sheetState.targetValue != SheetValue.Hidden
    if (!engaged) return this

    // El hueco que deja la pantalla al encogerse. Sin esto asomaría el fondo de la ventana del
    // sistema, que es claro siempre y en modo oscuro se vería como un marco blanco.
    val backdrop = MaterialTheme.colorScheme.surfaceContainerLowest

    return this
        .background(backdrop)
        .graphicsLayer {
            // Antes de la primera medida de la hoja esto lanza: no hay posición todavía porque
            // aún no se ha colocado. Ese frame no hay nada que encoger.
            val offset = runCatching { sheetState.requireOffset() }.getOrNull()
            val height = size.height

            val progress =
                if (offset == null || height <= 0f) 0f
                else (((height - offset) / height) / FULL_RECEDE_COVERAGE).coerceIn(0f, 1f)

            val scale = 1f - (1f - RECEDE_SCALE) * progress
            scaleX = scale
            scaleY = scale

            // El redondeo acompaña al encogido para que lo que se retira se lea como una tarjeta
            // y no como la misma pantalla mal encajada.
            shape = RoundedCornerShape((MAX_CORNER * progress).dp)
            clip = true
        }
}

private const val RECEDE_SCALE = 0.955f

/**
 * Cuánta pantalla tiene que tapar la hoja para que el encogido llegue a su tope.
 *
 * Media pantalla, que es lo que ocupan estas tarjetas cuando se asientan. Una hoja más baja
 * empuja menos, que es lo razonable: el fondo retrocede en proporción a lo que se le pone
 * delante, no a que exista algo delante.
 */
private const val FULL_RECEDE_COVERAGE = 0.5f

/** Redondeo de la pantalla retirada en su punto más encogido. */
private const val MAX_CORNER = 28f
