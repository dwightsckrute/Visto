package com.dwightsckrute.visto.feature.tv

import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity

data class TvHomeState(
    val items: List<WatchlistItemEntity> = emptyList(),
    val isLoading: Boolean = false,
    val seasonWatching: List<SeasonEntity> = emptyList(),
    val seasonFinished: List<SeasonEntity> = emptyList(),
    val seasonWatchlist: List<SeasonEntity> = emptyList(),
    /** Todas las temporadas guardadas, sin filtrar por pestaña. */
    val allSeasons: List<SeasonEntity> = emptyList(),
)

fun filterTvItems(
    items: List<WatchlistItemEntity>,
    isLoading: Boolean,
    seasonItems: List<SeasonEntity>
): TvHomeState {
    return TvHomeState(
        items = items.filter { it.mediaType != "movie" },
        seasonWatchlist = seasonItems.filter { it.status == WatchStatus.WANT_TO_WATCH },
        seasonWatching = seasonItems.filter { (it.status == WatchStatus.WATCHING || it.status == WatchStatus.INTERRUPTED) },
        seasonFinished = seasonItems.filter { it.status == WatchStatus.FINISHED },
        allSeasons = seasonItems,
        isLoading = isLoading
    )
}