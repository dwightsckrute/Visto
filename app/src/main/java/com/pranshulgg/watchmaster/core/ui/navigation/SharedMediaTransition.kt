package com.pranshulgg.watchmaster.core.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Continuidad de la carátula entre la lista y la ficha.
 *
 * Los dos ámbitos que exige `sharedElement` viven en sitios distintos: el de transición envuelve
 * al NavHost entero y el de visibilidad lo aporta cada destino. Pasarlos como parámetros
 * obligaría a atravesar media docena de composables intermedios que no tienen nada que ver con
 * la animación, así que se publican como CompositionLocal.
 *
 * Ambos son nulos por defecto: si un composable se usa fuera de la navegación (por ejemplo en una
 * preview), el modifier no hace nada en lugar de fallar. Ese es el fallback sin movimiento.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Clave estable de una carátula. Debe coincidir exactamente entre origen y destino, y no puede
 * repetirse en pantalla: dos elementos compartidos con la misma clave visibles a la vez hacen
 * que la transición no sepa cuál emparejar.
 */
fun posterSharedKey(mediaType: String, id: Long): String = "poster-$mediaType-$id"

/**
 * Marca este composable como la carátula compartida [key]. Sin ámbitos disponibles se devuelve
 * el modifier intacto.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPoster(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this

    return with(sharedScope) {
        this@sharedPoster.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
        )
    }
}
