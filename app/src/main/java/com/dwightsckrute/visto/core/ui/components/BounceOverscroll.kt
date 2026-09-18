package com.dwightsckrute.visto.core.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * El rebote al llegar al final de una página.
 *
 * Android trae de serie el estirado: el contenido se deforma contra el borde y vuelve. Esto es lo
 * otro —el contenido se separa del borde y vuelve con un muelle—, que con los muelles de Material 3
 * Expressive queda más contenido y menos "de goma".
 *
 * Lo que hace que no se sienta como un juguete es la resistencia: el desplazamiento no sigue al dedo
 * uno a uno, sino cada vez menos a medida que se aleja, y con tope. Sin eso, un arrastre largo separa
 * el contenido media pantalla y deja el fondo a la vista, que es justo lo contrario de sutil.
 *
 * La aritmética está en [BounceMath], aparte y probada: es donde viven los signos, y un signo
 * equivocado aquí se traduce en un desplazamiento que no vuelve nunca o que se va con el dedo.
 */
@OptIn(ExperimentalFoundationApi::class)
class BounceOverscrollEffect(
    private val maxOffset: Float,
    private val spec: AnimationSpec<Float>,
) : OverscrollEffect {

    /** Cuánto está separado el contenido de su sitio, en píxeles. Positivo hacia abajo. */
    private var offset by mutableFloatStateOf(0f)

    /** true mientras el muelle está devolviendo el contenido a su sitio. */
    private var settling by mutableStateOf(false)

    /**
     * Si el efecto está enseñando algo ahora mismo.
     *
     * **Esto era el fallo que hacía que a veces no se pudiera pasar de página.** Compose usa este
     * valor para decidir si el desplazamiento vertical se queda con el toque **en el momento de
     * bajar el dedo**, sin esperar a ver hacia dónde va —es lo que permite que un toque pare una
     * animación en curso—. Devolviendo `offset != 0f`, cualquier resto de un píxel dejaba esto en
     * true para siempre, la página se quedaba con todos los gestos y el carrusel no llegaba a ver el
     * arrastre horizontal.
     *
     * Dos cosas lo arreglan: una zona muerta, porque medio píxel no es un rebote que nadie esté
     * mirando; y que el desplazamiento vuelva a cero **exactamente**, también cuando la animación se
     * cancela a mitad. Ver [applyToFling].
     */
    override val isInProgress: Boolean
        get() = settling || abs(offset) > REST_TOLERANCE

    override val node: DelegatableNode = BounceNode { offset }

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        // Primero deshacer: con el contenido separado y el dedo yendo al revés, ese gesto devuelve el
        // contenido a su sitio antes de desplazar nada. Sin este paso, el contenido se queda separado
        // y el desplazamiento arranca por debajo del borde.
        val undo = BounceMath.undo(offset, delta.y)
        offset += undo
        val consumedByUndo = Offset(0f, undo)

        val leftForScroll = delta - consumedByUndo
        val consumedByScroll = performScroll(leftForScroll)
        val leftover = leftForScroll - consumedByScroll

        // Y sólo se separa con el dedo. Un `fling` que llega al borde no debe separar nada: de eso se
        // encarga [applyToFling], que sabe con cuánta velocidad ha llegado.
        if (abs(leftover.y) > EPSILON && source == NestedScrollSource.UserInput) {
            offset = BounceMath.stretch(offset, leftover.y, maxOffset)
        }

        return consumedByUndo + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        val consumed = performFling(velocity)
        val remaining = velocity.y - consumed.y

        // La velocidad que sobra se le entrega al muelle, **con tope**. Sin el tope, un arrastre
        // rápido contra el final entrega toda su velocidad y el rebote sale disparado: era lo que se
        // veía exagerado. Con tope, un gesto rápido rebasa un poco más que uno lento y ahí acaba la
        // diferencia.
        val launch = remaining.coerceIn(-MAX_LAUNCH_VELOCITY, MAX_LAUNCH_VELOCITY)

        settling = true
        try {
            animate(
                initialValue = offset,
                targetValue = 0f,
                initialVelocity = launch,
                animationSpec = spec,
            ) { value, _ -> offset = value }
        } finally {
            // A cero exactamente, y también si la animación se cancela porque empieza otro gesto.
            // Un resto de un píxel no se ve, pero deja `isInProgress` en true y con él la página
            // quedándose los toques que debería ver el carrusel.
            offset = 0f
            settling = false
        }
    }

    private companion object {
        /** Medio píxel: por debajo de eso no hay nada que separar y son sólo redondeos. */
        const val EPSILON = 0.5f

        /** La zona muerta de [isInProgress]. Un píxel de resto no es un rebote en curso. */
        const val REST_TOLERANCE = 1f

        /**
         * Tope de la velocidad que se le pasa al muelle, en píxeles por segundo.
         *
         * Un arrastre rápido puede llegar al final con varios miles, y entregarlos enteros es lo que
         * hacía que el rebote pareciera un muelle de juguete.
         */
        const val MAX_LAUNCH_VELOCITY = 1_200f
    }
}

/** Los signos y los topes del rebote, aparte para poder probarlos. */
object BounceMath {

    /**
     * Cuánto del gesto se gasta en devolver el contenido a su sitio.
     *
     * Devuelve 0 si no hay nada que deshacer o si el dedo va en el mismo sentido que la separación.
     * Nunca pasa de cero al otro lado: un gesto grande deshace lo que hay y el resto se va al
     * desplazamiento, no a separar por el lado contrario.
     */
    fun undo(offset: Float, delta: Float): Float {
        if (abs(offset) <= 0.5f) return 0f
        if (sign(delta) == sign(offset) || delta == 0f) return 0f
        return if (abs(delta) >= abs(offset)) -offset else delta
    }

    /**
     * La separación nueva, con resistencia y tope.
     *
     * La resistencia crece con lo ya separado: al principio el contenido sigue al dedo y al final casi
     * no se mueve. Es lo que hace que el gesto se sienta elástico en lugar de suelto.
     */
    fun stretch(offset: Float, delta: Float, maxOffset: Float): Float {
        if (maxOffset <= 0f) return 0f
        val used = (abs(offset) / maxOffset).coerceIn(0f, 1f)
        val resistance = 1f - used
        return (offset + delta * resistance).coerceIn(-maxOffset, maxOffset)
    }
}

/**
 * El nodo que separa el contenido de su sitio.
 *
 * Se lee en la fase de colocación, así que un fotograma del rebote no recompone ni vuelve a medir
 * nada: sólo se coloca en otro sitio y se dibuja.
 */
private class BounceNode(private val offset: () -> Float) : Modifier.Node(), LayoutModifierNode {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, offset().roundToInt())
        }
    }
}

/**
 * El rebote con las medidas de la aplicación.
 *
 * El tope es una fracción de la pantalla y no un número fijo de dp: en una tablet un tope de 48 dp no
 * se notaría y en un móvil pequeño 96 dp sería medio contenido fuera de sitio.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberBounceOverscrollFactory(): OverscrollFactory {
    val density = LocalDensity.current
    // El muelle rápido y no el lento: lo que se busca es acusar el final, no lucir el rebote. Con el
    // lento, el contenido tardaba casi medio segundo en asentarse y en todo ese rato el gesto
    // siguiente pillaba el efecto a media vuelta.
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    return remember(density, spec) { BounceOverscrollFactory(density, spec) }
}

@OptIn(ExperimentalFoundationApi::class)
private data class BounceOverscrollFactory(
    private val density: Density,
    private val spec: AnimationSpec<Float>,
) : OverscrollFactory {
    override fun createOverscrollEffect(): OverscrollEffect =
        BounceOverscrollEffect(
            maxOffset = with(density) { MAX_OFFSET.toPx() },
            spec = spec,
        )
}

/**
 * Hasta dónde se separa el contenido.
 *
 * 36 dp, la mitad de los 72 con los que empezó. A 72 el rebote se veía exagerado —el contenido se
 * despegaba tanto que dejaba a la vista el fondo— y además tardaba más en volver, que es el rato en
 * el que el gesto siguiente se lo encontraba a medio camino.
 */
private val MAX_OFFSET = 36.dp
