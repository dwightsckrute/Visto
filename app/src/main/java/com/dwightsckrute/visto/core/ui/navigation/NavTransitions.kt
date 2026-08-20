package com.dwightsckrute.visto.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import com.dwightsckrute.visto.core.ui.theme.AppMotion

/**
 * Transiciones entre pantallas: un empuje lateral.
 *
 * La ficha entra por la derecha y lo que había se va por la izquierda, como pasar de página.
 *
 * Esto ya estuvo así y se cambió por el eje Z de Material, con el argumento de que al tocar una
 * carátula se entra *dentro* de ella y el lateral cuenta "otra pantalla al lado". El argumento
 * sigue siendo cierto sobre el papel, y aun así ninguna de las variantes de profundidad —crecer,
 * encogerse, fundir encadenado, fundir solapado, la carátula viajando de la lista a la ficha—
 * llegó a sentirse bien en la mano. Elegido a la vista de todas ellas, así que vuelve.
 *
 * Y hay algo que el lateral hace mejor: **en ningún momento hay dos pantallas medio
 * transparentes**. De ahí venía el aspecto turbio, y no se arregla con la curva ni con la
 * duración, porque el problema es que se ven las dos a la vez. Aquí cada píxel pertenece a una
 * pantalla o a la otra.
 */
object NavTransitions {

    /**
     * Cuánto se desplaza la pantalla que se va, en fracción de ancho.
     *
     * Un tercio, no la pantalla entera. Con las dos recorriendo lo mismo el par se mueve como una
     * tira rígida; moviéndose menos la de detrás, la de delante parece pasar *por encima*, y eso
     * es lo que dice cuál manda y a cuál se vuelve. Es el mismo recurso que el paralaje.
     */
    private const val BEHIND_TRAVEL = 3

    private fun spec() =
        tween<IntOffset>(AppMotion.DurationMedium, easing = AppMotion.EmphasizedDecelerate)

    /** Entrar: la ficha llega desde la derecha y empuja lo anterior hacia la izquierda. */
    fun enter(): EnterTransition =
        slideInHorizontally(animationSpec = spec()) { width -> width }

    fun exit(): ExitTransition =
        slideOutHorizontally(animationSpec = spec()) { width -> -width / BEHIND_TRAVEL }

    /** Volver: el espejo exacto, que es también lo que reproduce el gesto atrás bajo el dedo. */
    fun popEnter(): EnterTransition =
        slideInHorizontally(animationSpec = spec()) { width -> -width / BEHIND_TRAVEL }

    fun popExit(): ExitTransition =
        slideOutHorizontally(animationSpec = spec()) { width -> width }
}
