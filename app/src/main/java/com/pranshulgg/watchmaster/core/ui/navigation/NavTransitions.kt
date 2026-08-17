package com.pranshulgg.watchmaster.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import com.pranshulgg.watchmaster.core.ui.theme.AppMotion

/**
 * Transiciones entre pantallas.
 *
 * Al abrir, las dos pantallas se cruzan por el eje horizontal recorriendo poco camino. Antes la
 * entrante venía desde el ancho completo y, peor, su deslizamiento no llevaba spec: usaba el
 * spring por defecto mientras el fundido iba con un tween de 350 ms. Dos animaciones sobre la
 * misma pieza con relojes distintos es exactamente lo que se percibe como movimiento torpe, y el
 * recorrido largo lo hacía además lento. Ahora comparten duración y curva.
 *
 * La vuelta es más corta todavía y añade un encogido mínimo, porque con el gesto atrás predictivo
 * Navigation reproduce estas curvas siguiendo el dedo y un desplazamiento grande se siente brusco
 * cuando lo arrastras tú.
 */
object NavTransitions {

    /** Fracción del ancho que recorre cada pantalla al abrir. Poco camino se lee como rápido. */
    private const val OPEN_SLIDE_DIVISOR = 4

    /** Recorrido al volver: el gesto ya aporta el movimiento, la animación solo lo acompaña. */
    private const val POP_SLIDE_DIVISOR = 6

    /** Encogido apenas perceptible; más que esto parece que la pantalla se cae hacia atrás. */
    private const val POP_SCALE = 0.94f

    /** Emphasized decelerate: arranca rápido y frena largo, la curva de Material para entradas. */
    private val OpenEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Arranca suave para que soltar el gesto atrás no dé un tirón. */
    private val PopEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    private fun openSpec() = tween<Float>(AppMotion.DurationShort, easing = OpenEasing)
    private fun openOffsetSpec() =
        tween<IntOffset>(AppMotion.DurationShort, easing = OpenEasing)

    private fun popSpec() = tween<Float>(AppMotion.DurationMedium, easing = PopEasing)
    private fun popOffsetSpec() =
        tween<IntOffset>(AppMotion.DurationMedium, easing = PopEasing)

    fun enter(): EnterTransition =
        slideInHorizontally(
            animationSpec = openOffsetSpec(),
            initialOffsetX = { it / OPEN_SLIDE_DIVISOR },
        ) + fadeIn(openSpec())

    fun exit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = openOffsetSpec(),
            targetOffsetX = { -it / OPEN_SLIDE_DIVISOR },
        ) + fadeOut(openSpec())

    fun popEnter(): EnterTransition =
        slideInHorizontally(
            animationSpec = popOffsetSpec(),
            initialOffsetX = { -it / POP_SLIDE_DIVISOR },
        ) + fadeIn(popSpec()) +
            scaleIn(animationSpec = popSpec(), initialScale = POP_SCALE)

    fun popExit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = popOffsetSpec(),
            targetOffsetX = { it / POP_SLIDE_DIVISOR },
        ) + fadeOut(popSpec()) +
            scaleOut(animationSpec = popSpec(), targetScale = POP_SCALE)
}
