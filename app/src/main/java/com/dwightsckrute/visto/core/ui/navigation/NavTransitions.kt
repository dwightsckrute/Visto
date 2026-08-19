package com.dwightsckrute.visto.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.dwightsckrute.visto.core.ui.theme.AppMotion

/**
 * Transiciones entre pantallas: el eje Z de Material.
 *
 * Abrir y volver son el mismo movimiento en sentidos opuestos. Al abrir, la pantalla saliente se
 * aleja creciendo y la entrante llega desde algo más lejos; al volver, exactamente al revés. Es la
 * misma forma que Android da al gesto atrás predictivo, y esa es la razón de haberla elegido:
 * cuando el gesto y la animación coinciden, entrar y salir de una ficha se siente como un único
 * movimiento en vez de como dos efectos distintos.
 *
 * Antes esto era un desplazamiento lateral. El lateral cuenta "otra pantalla al lado", que no es
 * lo que pasa aquí: al tocar una carátula se entra *dentro* de ella. El eje Z cuenta esa jerarquía.
 *
 * Los fundidos no se solapan a propósito. Si las dos pantallas se atenúan a la vez se ven las dos
 * medio transparentes durante medio segundo y el resultado es turbio; encadenados —primero se va
 * una, luego llega la otra— el cambio se lee limpio aunque dure lo mismo.
 *
 * Ese encadenado tiene además un efecto práctico. Grabando la pantalla se veía que entre que la
 * lista desaparecía y la ficha tenía datos que dibujar quedaba un hueco en negro de unos cinco
 * frames. La pantalla que se va tarda ahora lo suficiente en irse como para tapar ese hueco.
 */
object NavTransitions {

    /** Desde dónde llega la pantalla entrante al abrir. Lejos, pero no tanto como para saltar. */
    private const val ENTER_SCALE = 0.90f

    /** Hasta dónde se aleja la saliente. Solo lo justo para insinuar que queda detrás. */
    private const val EXIT_SCALE = 1.06f

    /** Lo que tarda en marcharse la pantalla que se va. */
    private const val FadeOutDuration = 220

    /** Y lo que tarda en llegar la nueva. */
    private const val FadeInDuration = 260

    /**
     * Cuándo empieza a asomar la entrante.
     *
     * Antes esperaba a que la saliente terminara del todo, para no ver dos pantallas medio
     * transparentes a la vez. Grabando la apertura de una ficha se ve lo que costaba eso: la
     * lista se iba, y quedaban unos cien milisegundos de pantalla vacía antes de que la ficha
     * apareciera, porque componerla lleva su tiempo y el fundido de entrada ni había empezado.
     *
     * Solapadas no hay hueco, y no se ven turbias porque no comparten sitio: el escalado las
     * separa en profundidad, una alejándose y la otra llegando.
     */
    private const val FadeInDelay = 90

    private fun scaleSpec() =
        tween<Float>(AppMotion.DurationLong, easing = AppMotion.EmphasizedDecelerate)
    private fun fadeOutSpec() = tween<Float>(FadeOutDuration)
    private fun fadeInSpec() = tween<Float>(FadeInDuration, delayMillis = FadeInDelay)

    fun enter(): EnterTransition =
        scaleIn(animationSpec = scaleSpec(), initialScale = ENTER_SCALE) + fadeIn(fadeInSpec())

    fun exit(): ExitTransition =
        scaleOut(animationSpec = scaleSpec(), targetScale = EXIT_SCALE) + fadeOut(fadeOutSpec())

    fun popEnter(): EnterTransition =
        scaleIn(animationSpec = scaleSpec(), initialScale = EXIT_SCALE) + fadeIn(fadeInSpec())

    fun popExit(): ExitTransition =
        scaleOut(animationSpec = scaleSpec(), targetScale = ENTER_SCALE) + fadeOut(fadeOutSpec())
}
