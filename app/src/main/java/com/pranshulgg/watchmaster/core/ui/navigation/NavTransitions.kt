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
import com.pranshulgg.watchmaster.core.ui.theme.AppMotion

/**
 * Transiciones entre pantallas.
 *
 * La ida conserva el deslizamiento lateral de siempre. La vuelta es distinta a propósito: con el
 * gesto atrás predictivo, Navigation reproduce estas mismas curvas siguiendo el dedo, así que un
 * desplazamiento de pantalla completa se siente brusco cuando lo estás arrastrando tú. Al volver
 * la pantalla se desplaza poco y se encoge ligeramente, que es el gesto que el sistema ya insinúa
 * con su animación de retroceso, y así lo de dentro de la aplicación acompaña a lo de fuera.
 */
object NavTransitions {

    /** Recorrido corto al volver: el gesto ya aporta el movimiento, la animación solo lo acompaña. */
    private const val POP_SLIDE_DIVISOR = 6

    /** Encogido apenas perceptible; más que esto parece que la pantalla se cae hacia atrás. */
    private const val POP_SCALE = 0.94f

    // Entrada lenta al principio y suave al final: evita el tirón inicial al soltar el gesto.
    private val PopEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun enter(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { 1 * it }
        ) + fadeIn(tween(AppMotion.DurationMedium))

    fun exit(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { 1 * -it / 4 }
        ) + fadeOut(tween(AppMotion.DurationShort))

    fun popEnter(): EnterTransition =
        slideInHorizontally(
            animationSpec = tween(AppMotion.DurationMedium, easing = PopEasing),
            initialOffsetX = { -it / POP_SLIDE_DIVISOR },
        ) + fadeIn(tween(AppMotion.DurationMedium, easing = PopEasing)) +
            scaleIn(
                animationSpec = tween(AppMotion.DurationMedium, easing = PopEasing),
                initialScale = POP_SCALE,
            )

    fun popExit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = tween(AppMotion.DurationMedium, easing = PopEasing),
            targetOffsetX = { it / POP_SLIDE_DIVISOR },
        ) + fadeOut(tween(AppMotion.DurationMedium, easing = PopEasing)) +
            scaleOut(
                animationSpec = tween(AppMotion.DurationMedium, easing = PopEasing),
                targetScale = POP_SCALE,
            )
}
