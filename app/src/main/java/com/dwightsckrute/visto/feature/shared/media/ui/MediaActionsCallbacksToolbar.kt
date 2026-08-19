package com.dwightsckrute.visto.feature.shared.media.ui

import java.time.Instant

data class FloatingToolbarMediaActionsParams(
    val startWatching: () -> Unit,
    val resetWatching: () -> Unit,
    val finishWatching: () -> Unit,
    val interruptWatching: () -> Unit,
    val delete: () -> Unit,
    val togglePin: () -> Unit,
    /**
     * Marcar o desmarcar como favorito.
     *
     * Opcional porque no todas las pantallas que usan esta barra tienen a mano el estado; las que
     * no lo pasen simplemente no ofrecen la opción, en vez de ofrecer una que no hace nada.
     */
    val toggleFavorite: (() -> Unit)? = null,
    val share: () -> Unit,
    /**
     * Marca como vista saltándose "en curso", registrando el día indicado.
     *
     * Existe aparte de [finishWatching] porque esa acción pasa por el diálogo de puntuación y
     * asume que ya la estabas viendo; esto es para lo que ya habías visto antes de anotarlo.
     */
    val markWatchedOn: (Instant) -> Unit,
)
