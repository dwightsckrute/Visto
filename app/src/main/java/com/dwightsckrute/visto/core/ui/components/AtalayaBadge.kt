package com.dwightsckrute.visto.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * La marca, dentro de una galleta que gira.
 *
 * Sustituye a la tarjeta que ponía "ATALAYA SOFTWARE" a lo ancho de los ajustes. Una tarjeta con el
 * nombre completo arriba del todo es una presentación, y nadie abre los ajustes de una aplicación
 * para que le presenten a quien la hace: el logotipo solo es una firma, que es lo que quiere ser.
 * El nombre completo sigue estando donde se va a buscar, en el aviso legal.
 *
 * Tres movimientos, cada uno con su motivo:
 *
 * - **Entra creciendo**, con el muelle lento del tema. Es lo primero de la pantalla y da el pie a
 *   lo que viene detrás.
 * - **Al tocarla gira una vuelta entera** con un pulso de tamaño. El pulso es un seno, que vale
 *   cero en los dos extremos: empieza y acaba exactamente en su sitio aunque el muelle rebase.
 * - **La letra deshace el giro del envase y hace el suyo con retraso.** Si girara con él no se
 *   notaría que gira nada: una galleta de nueve puntas vuelve a ser ella misma cada cuarenta
 *   grados, así que sin ese desfase el giro sería invisible.
 *
 * Y al mantener pulsado, la ficha de quién la hace. Es lo que la marca de una aplicación suele
 * esconder: el toque corto es el guiño y la pulsación larga es la respuesta.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AtalayaBadge(
    modifier: Modifier = Modifier,
    size: Dp = BADGE_SIZE,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }

    val entrance = remember { Animatable(0f) }
    val spin = remember { Animatable(0f) }
    val markSpin = remember { Animatable(0f) }
    val entranceSpec = motionScheme.slowSpatialSpec<Float>()
    val spinSpec = remember { tween<Float>(SPIN_DURATION_MS, easing = FastOutSlowInEasing) }

    var sheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { entrance.animateTo(1f, entranceSpec) }

    if (sheet) {
        AtalayaSheet(onDismiss = { sheet = false })
    }

    Box(
        modifier = modifier
            // Como lambda: el giro vive en la capa gráfica y no recompone nada.
            .graphicsLayer {
                val pulse = 1f + SPIN_PULSE * sin(PI.toFloat() * spin.value.coerceIn(0f, 1f))
                scaleX = entrance.value * pulse
                scaleY = entrance.value * pulse
                rotationZ = spin.value * FULL_TURN
            }
            .size(size)
            .clip(MaterialShapes.Cookie9Sided.toShape())
            .background(MaterialTheme.colorScheme.primaryContainer)
            .combinedClickable(
                interactionSource = interaction,
                // Sin destello: la forma va recortada y el de serie sale cuadrado por las puntas.
                indication = null,
                onClickLabel = "Atalaya Software",
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sheet = true
                },
                onLongClickLabel = "Atalaya Software",
            ) {
                scope.launch {
                    spin.snapTo(0f)
                    spin.animateTo(1f, spinSpec)
                }
                scope.launch {
                    markSpin.snapTo(0f)
                    delay(MARK_LAG_MS)
                    markSpin.animateTo(1f, spinSpec)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AtalayaMark(
            size = size * MARK_RATIO,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.graphicsLayer {
                rotationZ = (markSpin.value - spin.value) * FULL_TURN
            },
        )
    }
}

private val BADGE_SIZE = 112.dp

/** Cuánto de la galleta ocupa la letra. */
private const val MARK_RATIO = 0.42f

private const val FULL_TURN = 360f

/** Lo que crece la galleta a mitad del giro. */
private const val SPIN_PULSE = 0.26f

/** El retraso de la letra respecto al envase, que es lo que hace visible el giro. */
private const val MARK_LAG_MS = 140L

private const val SPIN_DURATION_MS = 900
