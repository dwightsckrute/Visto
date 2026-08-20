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

    /**
     * Hasta dónde se encoge la pantalla que se queda detrás.
     *
     * Es la pieza que da el gesto: al abrir una ficha, lo que había **retrocede encogiéndose**,
     * como hace el launcher al abrir una aplicación. Antes crecía —el eje Z de Material propone
     * que la saliente se acerque y se desvanezca—, y aunque sobre el papel es igual de correcto,
     * en la mano cuenta otra cosa: que te la echan encima en lugar de que se aparta.
     *
     * Poco recorrido a propósito. Encogerla más la separa del borde y aparece el fondo, que
     * delata que son dos pantallas superpuestas en vez de una que cede el sitio a otra.
     */
    private const val BEHIND_SCALE = 0.93f

    /**
     * Desde dónde llega la que entra.
     *
     * Ligeramente por delante y no por detrás, para que el par se lea como una sola profundidad:
     * una se va hacia el fondo mientras la otra viene hacia ti. Con las dos entrando desde lejos
     * habría un momento en que ninguna ocupa la pantalla.
     */
    private const val ARRIVING_SCALE = 1.04f

    /**
     * Lo que tarda en marcharse la pantalla que se va, y en llegar la nueva.
     *
     * Cortos, y pueden serlo desde que la carátula viaja de la lista a la ficha. Antes el fundido
     * *era* la transición y tenía que durar lo suficiente para no leerse como un corte; ahora
     * solo acompaña, porque lo que sigue el ojo es la imagen que crece. Un fundido que acompaña
     * y se alarga es exactamente la parte turbia.
     */
    private const val FadeOutDuration = 150

    private const val FadeInDuration = 200

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
    private const val FadeInDelay = 50

    /**
     * El escalado, que es lo que marca la duración de todo el cambio.
     *
     * Bajó de 450 a 350 ms. Los 450 se eligieron cuando la transición era un fundido entre dos
     * pantallas completas y hacía falta tiempo para leer el relevo. Con la carátula haciendo de
     * hilo no hay nada que descifrar —ya sabes qué has tocado y a dónde va— y ese mismo tiempo
     * pasa a sentirse como espera.
     */
    private fun scaleSpec() =
        tween<Float>(AppMotion.DurationMedium, easing = AppMotion.EmphasizedDecelerate)
    private fun fadeOutSpec() = tween<Float>(FadeOutDuration)
    private fun fadeInSpec() = tween<Float>(FadeInDuration, delayMillis = FadeInDelay)

    /** Entrar: la ficha llega desde delante mientras lo anterior se encoge hacia el fondo. */
    fun enter(): EnterTransition =
        scaleIn(animationSpec = scaleSpec(), initialScale = ARRIVING_SCALE) + fadeIn(fadeInSpec())

    fun exit(): ExitTransition =
        scaleOut(animationSpec = scaleSpec(), targetScale = BEHIND_SCALE) + fadeOut(fadeOutSpec())

    /**
     * Volver: exactamente al revés.
     *
     * Importa más de lo que parece porque Navigation reproduce estas dos curvas siguiendo el dedo
     * en el gesto atrás predictivo. Con la ficha encogiéndose mientras arrastras y la lista
     * creciendo desde detrás, el gesto enseña a dónde vas mientras lo haces, que es lo que el
     * sistema hace con las aplicaciones.
     */
    fun popEnter(): EnterTransition =
        scaleIn(animationSpec = scaleSpec(), initialScale = BEHIND_SCALE) + fadeIn(fadeInSpec())

    fun popExit(): ExitTransition =
        scaleOut(animationSpec = scaleSpec(), targetScale = ARRIVING_SCALE) + fadeOut(fadeOutSpec())
}
