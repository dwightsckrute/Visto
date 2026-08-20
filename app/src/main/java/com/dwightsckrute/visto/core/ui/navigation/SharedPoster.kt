package com.dwightsckrute.visto.core.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect

/**
 * Los dos ámbitos que hacen falta para que un elemento viaje entre pantallas.
 *
 * Van por CompositionLocal y no por parámetro porque el póster está a seis o siete niveles de
 * profundidad —pantalla, andamio, contenido, lista, fila, caja— y hacerlos bajar de la mano
 * obligaría a tocar cada capa intermedia para algo que no es asunto de ninguna.
 *
 * Nulos por defecto: fuera de la navegación —una vista previa, un test— no hay transición y el
 * modificador no hace nada, en vez de fallar.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * El póster de la lista y el de la ficha son el mismo póster.
 *
 * Hasta ahora abrir una ficha era un fundido cruzado entre dos pantallas completas: durante
 * doscientos milisegundos había dos interfaces superpuestas a media opacidad, lo que da ese aire
 * turbio que no se arregla ajustando la curva —lo intenté media docena de veces— porque el
 * problema no es la curva sino que se está contando que una pantalla sustituye a otra.
 *
 * Lo que de verdad pasa es más simple: has tocado una cosa y esa cosa se abre. Así que la imagen
 * que tocas es la que crece hasta su sitio en la ficha, y el resto entra a su alrededor. Hay una
 * sola cosa moviéndose y el ojo la sigue, en vez de dos pantallas peleándose por el mismo hueco.
 *
 * Que la clave case en los dos extremos es todo el contrato. Si no case, no hay error: cada lado
 * aparece por su cuenta y volvemos al fundido.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPoster(key: Any?): Modifier {
    val transition = LocalSharedTransitionScope.current ?: return this
    val animated = LocalNavAnimatedScope.current ?: return this
    if (key == null) return this

    // El esquema se resuelve aquí, fuera de la lambda: `BoundsTransform` no es @Composable y se
    // consulta durante la animación, no durante la composición.
    val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()
    val bounds = remember(spec) { BoundsTransform { _, _ -> spec } }

    return with(transition) {
        this@sharedPoster.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animated,
            boundsTransform = bounds,
        )
    }
}

/** La clave del póster de una ficha. Un único sitio para que los dos extremos no puedan divergir. */
fun posterSharedKey(id: Long): String = "poster-$id"
