package com.dwightsckrute.visto.feature.movie

import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.core.model.WatchStatus

data class MovieHomeState(
    val watchlist: List<WatchlistItemEntity> = emptyList(),
    val watching: List<WatchlistItemEntity> = emptyList(),
    val finished: List<WatchlistItemEntity> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * El tipo con el que se guarda una película. Antes esta pantalla filtraba por "no es serie", que
 * funcionaba mientras solo hubiera dos tipos: al aparecer los libros, empezaban a salir aquí.
 */
private const val MEDIA_TYPE_MOVIE = "movie"

fun MovieHomeState(
    items: List<WatchlistItemEntity>,
    isLoading: Boolean
): MovieHomeState {
    return MovieHomeState(
        watchlist = items.filter {
            it.status == WatchStatus.WANT_TO_WATCH && it.mediaType == MEDIA_TYPE_MOVIE
        },
        watching = items.filter {
            (it.status == WatchStatus.WATCHING ||
                    it.status == WatchStatus.INTERRUPTED) &&
                    it.mediaType == MEDIA_TYPE_MOVIE
        },
        finished = items.filter {
            it.status == WatchStatus.FINISHED &&
                    it.mediaType == MEDIA_TYPE_MOVIE
        },
        isLoading = isLoading
    )
}