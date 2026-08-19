package com.dwightsckrute.visto.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.dwightsckrute.visto.core.ui.theme.AppMotion

/**
 * La entrada del contenido que no estaba listo cuando empezó la navegación.
 *
 * El problema que resuelve se ve grabando la pantalla: al tocar una carátula, la ficha se compone
 * vacía mientras llegan los datos, así que la animación de navegación se gasta sobre una pantalla
 * en blanco y el contenido aparece de un tirón cuando esa animación ya ha terminado. Da igual la
 * curva o la duración que se le ponga a la navegación: lo que se ve saltar no es ella.
 *
 * Aquí el contenido se anima cuando de verdad existe, con la misma forma que la navegación —crece
 * desde algo más lejos y aparece—, de modo que se leen como un único movimiento.
 *
 * Si los datos ya estaban en el primer frame no hace nada: en ese caso la navegación sí tiene algo
 * que animar y añadir otro movimiento encima solo lo enturbia.
 */
@Composable
fun rememberEntranceSettle(ready: Boolean): Float {
    val readyOnEntry = remember { ready }
    val progress = remember { Animatable(if (readyOnEntry) 1f else 0f) }
    val startedAt = remember { System.nanoTime() }

    LaunchedEffect(ready) {
        if (!ready || progress.value >= 1f) return@LaunchedEffect

        // Si los datos llegaron enseguida, no hubo espera que disimular y esto sobra.
        //
        // Hace falta porque `ready` casi nunca es cierto en la primera composición aunque los
        // datos estén en la base: se leen de un flujo que empieza en nulo y emite uno o dos
        // frames después. Sin esta ventana, una ficha instantánea se animaba igual que una que
        // tarda, y encima de la transición de navegación: grabando la apertura de un libro se
        // veía el resultado, un hueco casi en blanco entre que la lista se va y la ficha llega.
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        if (elapsedMs <= INSTANT_GRACE_MS) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }

        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = AppMotion.DurationMedium,
                easing = AppMotion.EmphasizedDecelerate,
            ),
        )
    }

    return progress.value
}

/**
 * Lo que puede tardar el contenido en llegar sin que cuente como espera.
 *
 * Room responde en uno o dos frames; la red, en cientos de milisegundos. Este umbral separa las
 * dos cosas, que es lo que el estado inicial nulo de un flujo impedía distinguir.
 */
private const val INSTANT_GRACE_MS = 120L

/** Desde dónde crece el contenido. Poco: es un asentamiento, no una segunda transición. */
private const val SETTLE_SCALE = 0.94f

/** Aplica el asentamiento de [rememberEntranceSettle]. Con `progress` a 1 no cuesta nada. */
fun Modifier.entranceSettle(progress: Float): Modifier =
    if (progress >= 1f) {
        this
    } else {
        graphicsLayer {
            alpha = progress
            val scale = SETTLE_SCALE + (1f - SETTLE_SCALE) * progress
            scaleX = scale
            scaleY = scale
        }
    }
