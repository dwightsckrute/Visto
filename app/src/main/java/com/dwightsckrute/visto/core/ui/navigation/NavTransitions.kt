package com.dwightsckrute.visto.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.unit.IntOffset

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

    /**
     * El muelle del tema, no una curva propia.
     *
     * Antes esto era un `tween` de 350 ms con una curva desacelerada: arranca rápido y frena
     * largo, así que el final se arrastraba y el conjunto se sentía pesado. Peor todavía, un
     * `tween` no se puede interrumpir bien —si tocas otra vez o empiezas el gesto atrás a mitad,
     * vuelve a empezar desde cero en vez de continuar desde donde iba y a la velocidad que
     * llevaba.
     *
     * Un muelle sí. Y ya lo usaba todo lo demás de la aplicación: la navegación era lo único que
     * se movía con reglas propias, que es justo lo que se nota aunque no se sepa nombrar.
     *
     * El **lento** de los tres, no el normal. El esquema trae tres velocidades justamente porque
     * cuanto mayor es la superficie que se mueve, más despacio tiene que ir para no leerse como
     * un salto: el muelle normal está pensado para un control, no para la pantalla entera, y a
     * tamaño completo llega y se planta de golpe.
     */
    private fun spec(scheme: MotionScheme): FiniteAnimationSpec<IntOffset> =
        scheme.slowSpatialSpec()

    /** Entrar: la ficha llega desde la derecha y empuja lo anterior hacia la izquierda. */
    fun enter(scheme: MotionScheme): EnterTransition =
        slideInHorizontally(animationSpec = spec(scheme)) { width -> width }

    fun exit(scheme: MotionScheme): ExitTransition =
        slideOutHorizontally(animationSpec = spec(scheme)) { width -> -width / BEHIND_TRAVEL }

    /** Volver: el espejo exacto, que es también lo que reproduce el gesto atrás bajo el dedo. */
    fun popEnter(scheme: MotionScheme): EnterTransition =
        slideInHorizontally(animationSpec = spec(scheme)) { width -> -width / BEHIND_TRAVEL }

    fun popExit(scheme: MotionScheme): ExitTransition =
        slideOutHorizontally(animationSpec = spec(scheme)) { width -> width }
}
